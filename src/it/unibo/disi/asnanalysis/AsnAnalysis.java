package it.unibo.disi.asnanalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class AsnAnalysis {

	static final Logger LOG = Logger.getLogger("eu.emc2.bugs.weka.AsnAnalysis");

	public static void main(String[] args) throws Exception {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%6$s%n");

		String input = "dataset-ASN-2012.arff";
		String output = "output.csv";
		String operation = "RF";
		int level = 1; 
		
		// -optfile optfilename || -i input -o output -op operation -l numLevel
		
		if(args.length > 0 && args[0].equals("-optfile")) {
			List<String> lines = new ArrayList<>();
			try(BufferedReader bufferedReader = new BufferedReader(new FileReader(args[1]))) {
				String line;
				while((line = bufferedReader.readLine()) != null) {
					if(!line.trim().equals("") && !line.startsWith("#")) {
						if(line.startsWith("-") && line.indexOf(" ") != -1) {
							int spaceIdx = line.indexOf(" ");
							lines.add(line.substring(0, spaceIdx));
							lines.add(line.substring(spaceIdx+1));
						} else {
							lines.add(line);
						}
					}
				}
			}
			args = lines.toArray(new String[0]);
		}
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("-i")) { //classifier
				i++;
				input = args[i];
			} else if(args[i].equals("-o")) { //pre-filters
				i++;
				output = args[i];
			} else if(args[i].equals("-op")) { //loop filters
				i++;
				operation = args[i];
			} else if(args[i].equals("-l")) { //boot filters
				i++;
				String levelArg = args[i];
				if (levelArg.equals("1") || levelArg.equals("2")) {
					level = Integer.parseInt(levelArg);
				} else {
					LOG.info("Error: only levels \"1\" and \"2\" are allowed.");
				}
			}
		}
		
		switch (operation) {
			case "RF":
				{
					DataSource dataSource = new DataSource(input);
					Instances dataSet = dataSource.getDataSet();
					svmRFsLevel(dataSet, output, level);
					break;
				}
			case "Area":
				{
					DataSource dataSource = new DataSource(input);
					Instances dataSet = dataSource.getDataSet();
					svmAreas(dataSet, output, level);
					break;
				}
			case "FeatureSel":
				{
					DataSource dataSource = new DataSource(input);
					Instances dataSet = dataSource.getDataSet();
					featureSel(dataSet, output, level);
					break;
				}
			case "Experiment1":
				{
					File folder = new File(input);
					if ( !(folder.exists() && folder.isDirectory()) ) {
					   LOG.info("Error: the input folder does not exist.");
					   System.exit(-1);
					}
					
					LOG.info("Evaluation - Experiment #1");
					experiment1(input, output);
					break;
				}
			case "Experiment2":
			{
				File folder = new File(input);
				if ( !(folder.exists() && folder.isDirectory()) ) {
				   LOG.info("Error: the input folder does not exist.");
				   System.exit(-1);
				}
				
				LOG.info("Evaluation - Experiment #2");
				experiment2(input, output);	
				break;
			}
		}		
	}


	static String roundDouble(double d) {
		return String.format("%.3f", d).replace(",", ".");
	}

	
	private static Evaluation doLogisticClassification(Instances dataset, String className) throws Exception {
		dataset.setClass(dataset.attribute(className));
		
		// other options
		int seed  = 1; //87452;
		int folds = 10;
		
		// randomize data
		Random rand = new Random(seed);
		Instances randDataLI = new Instances(dataset);
		randDataLI.randomize(rand);
		if (randDataLI.classAttribute().isNominal())
			randDataLI.stratify(folds);
		
		// perform cross-validation
		Evaluation eval = new Evaluation(randDataLI);
		
		for (int n = 0; n < folds; n++) {
			Instances test = randDataLI.testCV(folds, n);
			// the above code is used by the StratifiedRemoveFolds filter, the
			// code below by the Explorer/Experimenter:
			Instances train = randDataLI.trainCV(folds, n, rand);
			
			String optsLogistic = "-R 1.0E-8 -M -1 -num-decimal-places 4";
			Logistic log = new Logistic();
			log.setOptions(Utils.splitOptions(optsLogistic));
			
			log.buildClassifier(train);
			eval.evaluateModel(log, test);
		}
		
		return eval;
	}
	
	
	private static Evaluation doSvmClassification(Instances dataset, String className) throws Exception {
		dataset.setClass(dataset.attribute(className));
		
		// other options
		int seed  = 1; //87452;
		int folds = 10;
		
		// randomize data
		Random rand = new Random(seed);
		Instances randDataLI = new Instances(dataset);
		randDataLI.randomize(rand);
		if (randDataLI.classAttribute().isNominal())
			randDataLI.stratify(folds);
		
		// perform cross-validation
		Evaluation eval = new Evaluation(randDataLI);
		
		for (int n = 0; n < folds; n++) {
			Instances test = randDataLI.testCV(folds, n);
			// the above code is used by the StratifiedRemoveFolds filter, the
			// code below by the Explorer/Experimenter:
			Instances train = randDataLI.trainCV(folds, n, rand);
			
			String optsSVM = "-C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007\" -calibrator \"weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4\""; // -x 10 -v -o -c";
			SMO svm = new SMO();
			svm.setOptions(Utils.splitOptions(optsSVM));
			
			svm.buildClassifier(train);
			eval.evaluateModel(svm, test);
		}
		
		return eval;
	}
	
	
	private static void svmRFsLevel(Instances dataSet, String csvOutput, int level) throws Exception {
		LOG.info("Recruitment Field Analysis (SVM) - Level " + level);
		
		// CSV OUTPUT
		MyRecordList rl = new MyRecordList();
		rl.setHeader("Recruitment Field", "Precision", "Recall", "F-Measure");
		
		Enumeration<Object> enumRFs = dataSet.attribute(1).enumerateValues();
		
		int indexRF = 1;
		while (enumRFs.hasMoreElements()) {
			String rf = (String)enumRFs.nextElement();
			
			Filter filterRWV = new RemoveWithValues();
			
			/**
			 * Filtering SDs
			 */
			String optsFilterSD = "-S 0.0 -C 2 -L " + indexRF + " -V -M";
			filterRWV.setOptions(Utils.splitOptions(optsFilterSD));
			filterRWV.setInputFormat(dataSet);
			Instances dataSetFiltered = Filter.useFilter(dataSet, filterRWV);
			
			/**
			 * Filtering Levels
			 */
			String optsFilterLevel = "-S 0.0 -C 3 -L " + level + " -V -M";
			filterRWV.setOptions(Utils.splitOptions(optsFilterLevel));
			filterRWV.setInputFormat(dataSetFiltered);
			Instances dataSetFilteredLevel = Filter.useFilter(dataSetFiltered, filterRWV);
			
			// SAVE .ARFF
	     	//try (PrintWriter out = new PrintWriter("data/output/RFs/" + rf.replace("/", "-") + "_L" + level + ".arff")) {
	     	//	out.println(dataSetFilteredLevel.toString());
	     	//}

	     	/**
			 * Removing Attributes
			 */
			Filter filterRm = new Remove();
			String optsFilterRm = "-R 2,3,4";
			filterRm.setOptions(Utils.splitOptions(optsFilterRm));
			filterRm.setInputFormat(dataSet);
			Instances dataSetFilteredLevelRm = Filter.useFilter(dataSetFilteredLevel, filterRm);
			
			// SAVE .ARFF
	     	//try (PrintWriter out = new PrintWriter("data/output/RFs/" + rf.replace("/", "-") + "_L" + level + "_removed.arff")) {
	     	//	out.println(dataSetFilteredLevelRm.toString());
	     	//}
	     	
			/**
			 * Classification with SVM
			 **/
			
	     	Evaluation eval = doSvmClassification(dataSetFilteredLevelRm, "Abilitato");
			
			String precision = roundDouble(eval.precision(0));
			String recall = roundDouble(eval.recall(0));
			String fMeasure = roundDouble(eval.fMeasure(0));
			rl.addRecord(rf, precision, recall, fMeasure);
			
			LOG.info(rf + " - Precision: " + precision + " - Recall: " + recall + " - F-Measure: " + fMeasure);
			indexRF += 1;
			//System.exit(0);
		}

		rl.sortByCol(3, MyRecordList.SortDESC);
		rl.saveToFile(csvOutput);
	}
	
	
	private static void svmAreas(Instances dataSet, String csvOutput, int level) throws Exception {
		LOG.info("Area Analysis (SVM) - Level " + level);
		
		// CSV OUTPUT
		MyRecordList rl = new MyRecordList();
		rl.setHeader("Area", "Precision", "Recall", "F-Measure");
		
		Enumeration<Object> enumRFs = dataSet.attribute(1).enumerateValues();
		
		// Organize RFs in Areas
		HashMap<String, ArrayList<String>> areaMapIndices = new HashMap<String, ArrayList<String>>();
		
		String[] arrAreas = {"01","02","03","04","05","06","07","08","09","10","11","12","13","14"};
		String[] elevenBibl = {"11/E1", "11/E2", "11/E3", "11/E4"};
		String[] eightNbibl = {"08/C1", "08/D1", "08/E1", "08/E2", "08/F1"};
		for (String area : arrAreas) {
			ArrayList<String> rfInArea = new ArrayList<String>();
			areaMapIndices.put(area, rfInArea);
			if (area.equals("11")) {
				ArrayList<String> rf11EInArea = new ArrayList<String>();
				areaMapIndices.put("11/E", rf11EInArea);
			}
			if (area.equals("08")) {
				ArrayList<String> rf08NBInArea = new ArrayList<String>();
				areaMapIndices.put("08-NB", rf08NBInArea);
			}
		}
		
		int indexRF = 1;
		while (enumRFs.hasMoreElements()) {
			String rf = (String)enumRFs.nextElement();
			
			if (Arrays.asList(elevenBibl).contains(rf)) {
				ArrayList<String> rfList = areaMapIndices.get("11/E");
				rfList.add(Integer.toString(indexRF));
				indexRF += 1;
				continue;
			}
			
			if (Arrays.asList(eightNbibl).contains(rf)) {
				ArrayList<String> rfList = areaMapIndices.get("08-NB");
				rfList.add(Integer.toString(indexRF));
				indexRF += 1;
				continue;
			}
			
			String area = rf.substring(0,2);
			ArrayList<String> rfList = areaMapIndices.get(area);
			rfList.add(Integer.toString(indexRF));
			
			indexRF += 1;
		}
		
		for (String area : areaMapIndices.keySet()) {
			ArrayList<String> rfList = areaMapIndices.get(area);
			String rfString = String.join(",", rfList);
			
			Filter filterRWV = new RemoveWithValues();
			
			/**
			 * Filtering SDs
			 */
			String optsFilterSD = "-S 0.0 -C 2 -L " + rfString + " -V -M";
			filterRWV.setOptions(Utils.splitOptions(optsFilterSD));
			filterRWV.setInputFormat(dataSet);
			Instances dataSetFiltered = Filter.useFilter(dataSet, filterRWV);
			
			/**
			 * Filtering Levels
			 */
			String optsFilterLevel = "-S 0.0 -C 3 -L " + level + " -V -M";
			filterRWV.setOptions(Utils.splitOptions(optsFilterLevel));
			filterRWV.setInputFormat(dataSetFiltered);
			Instances dataSetFilteredLevel = Filter.useFilter(dataSetFiltered, filterRWV);
			
			// SAVE .ARFF
	     	//try (PrintWriter out = new PrintWriter("data/output/Areas/" + area.replace("/", "-") + "_L" + level + ".arff")) {
	     	//	out.println(dataSetFilteredLevel.toString());
	     	//}
	     	
			/**
			 * Removing Attributes
			 */
			Filter filterRm = new Remove();
			String optsFilterRm = "-R 2,3,4";
			filterRm.setOptions(Utils.splitOptions(optsFilterRm));
			filterRm.setInputFormat(dataSet);
			Instances dataSetFilteredLevelRm = Filter.useFilter(dataSetFilteredLevel, filterRm);
			
			// SAVE .ARFF
	     	//try (PrintWriter out = new PrintWriter("data/output/Areas/" + area.replace("/", "-") + "_L" + level + "_removed.arff")) {
	     	//	out.println(dataSetFilteredLevelRm.toString());
	     	//}
	     	
			/**
			 * Classification with SVM
			 **/
			Evaluation eval = doSvmClassification(dataSetFilteredLevelRm, "Abilitato");
			
			String precision = roundDouble(eval.precision(0));
			String recall = roundDouble(eval.recall(0));
			String fMeasure = roundDouble(eval.fMeasure(0));
			rl.addRecord(area, precision, recall, fMeasure);
			LOG.info(area + " - Precision: " + precision + " - Recall: " + recall + " - F-Measure: " + fMeasure);
			
		}
		
		rl.sortByCol(0, MyRecordList.SortASC);
		rl.saveToFile(csvOutput);
	}

	
	private static void featureSel(Instances dataSet, String csvOutput, int level) throws Exception {
		LOG.info("Analysis of the top 15 features - Level " + level);
		
		// CSV OUTPUT
		MyRecordList rl = new MyRecordList();
		rl.setHeader("Recruitment Field", "Precision", "Recall", "F-Measure");

		LOG.info("Selection of the top 15 features...");
		Enumeration<Object> enumRFs = dataSet.attribute(1).enumerateValues();
		
		int indexRF = 1;
		HashMap<Integer, Integer> featureSelCounter = new HashMap<Integer, Integer>();
		HashMap<String, Instances> datasetMap = new HashMap<String, Instances>();
		while (enumRFs.hasMoreElements()) {
			String rf = (String)enumRFs.nextElement();
			
			Filter filterRWV = new RemoveWithValues();
			
			/**
			 * Filtering SDs
			 */
			String optsFilterSD = "-S 0.0 -C 2 -L " + indexRF + " -V -M";
			filterRWV.setOptions(Utils.splitOptions(optsFilterSD));
			filterRWV.setInputFormat(dataSet);
			Instances dataSetFiltered = Filter.useFilter(dataSet, filterRWV);
			
			/**
			 * Filtering Levels
			 */
			String optsFilterLevelI = "-S 0.0 -C 3 -L " + level + " -V -M";
			filterRWV.setOptions(Utils.splitOptions(optsFilterLevelI));
			filterRWV.setInputFormat(dataSetFiltered);
			Instances dataSetFilteredLevel = Filter.useFilter(dataSetFiltered, filterRWV);
			
			/**
			 * Removing Attributes
			 */
			Filter filterRm = new Remove();
			String optsFilterRm = "-R 2";
			filterRm.setOptions(Utils.splitOptions(optsFilterRm));
			filterRm.setInputFormat(dataSet);
			Instances dataSetFilteredLevelRm = Filter.useFilter(dataSetFilteredLevel, filterRm);
			datasetMap.put(rf, dataSetFilteredLevelRm);
			
			/**
			 * CFS
			 */
			dataSetFilteredLevelRm.setClass(dataSetFilteredLevelRm.attribute("Abilitato"));
						
			weka.filters.supervised.attribute.AttributeSelection filter = new weka.filters.supervised.attribute.AttributeSelection();
			//weka.attributeSelection.AttributeSelection filter = new weka.attributeSelection.AttributeSelection();
			
			CfsSubsetEval eval = new CfsSubsetEval();
			eval.setOptions(Utils.splitOptions("-P 1 -E 1 -c last"));
			filter.setEvaluator(eval);
			
			BestFirst search = new BestFirst();
			search.setOptions(Utils.splitOptions("-D 1 -N 5"));
			filter.setSearch(search);
			
			filter.setInputFormat(dataSetFilteredLevelRm);
			
			Instances newData = Filter.useFilter(dataSetFilteredLevelRm, filter);
			
			ArrayList<Integer> arrSelected = new ArrayList<Integer>();
			for (int i=0; i<newData.numAttributes() -1; i++) {
				String selAttrName = newData.attribute(i).name();
				Enumeration<Attribute> attrs = dataSetFilteredLevelRm.enumerateAttributes();
				int j = 0;
				while (attrs.hasMoreElements()) {
					Attribute currAttr = attrs.nextElement();
					if (currAttr.name().equals(selAttrName)) {
						arrSelected.add(j);
						int count = featureSelCounter.containsKey(j) ? featureSelCounter.get(j) : 0;
						featureSelCounter.put(j, count + 1);
					}
					j++;
				}
			}
			indexRF += 1;
		}

		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (Integer key : featureSelCounter.keySet() ) {
			temp.add(featureSelCounter.get(key));
		}
		Collections.sort(temp);
		Collections.reverse(temp);
		
		LinkedHashSet<Integer> top15Counter = new LinkedHashSet<Integer>(temp.stream().limit(15).collect(Collectors.toList()));
		
		LinkedHashSet<Integer> top15Index = new LinkedHashSet<Integer>();
		LOG.info("Selected Features:");
		int numFound = 0;
		for (int currVal : top15Counter) {
			for (Integer attrInd : featureSelCounter.keySet()) {
				if (featureSelCounter.get(attrInd) == currVal && numFound < 15) {
					top15Index.add(attrInd + 1);
					LOG.info("\t* " + datasetMap.get("06/M1").attribute(attrInd).name() + " (#" + (attrInd+2) + " - selected " + featureSelCounter.get(attrInd) + " times)");
					numFound++;
				}
			}
		}
		
		for (String ssd : datasetMap.keySet()) {
		   	Instances ds = datasetMap.get(ssd);
			
			/**
			 * Removing Attributes
			 */
			Filter filterRm = new Remove();
			String optsFilterRm = StringUtils.join(top15Index.stream().limit(15).collect(Collectors.toList()), ",");
			filterRm.setOptions(Utils.splitOptions("-V -R " + optsFilterRm + ",last"));
			filterRm.setInputFormat(ds);
			Instances dsTop15 = Filter.useFilter(ds, filterRm);
			
			/**
			 * Classification with SVM
			 */
			Evaluation eval = doSvmClassification(dsTop15, "Abilitato");
			String precision = roundDouble(eval.precision(0));
			String recall = roundDouble(eval.recall(0));
			String fMeasure = roundDouble(eval.fMeasure(0));
			rl.addRecord(ssd, precision, recall, fMeasure);
			LOG.info(ssd + " - Precision: " + precision + " - Recall: " + recall + " - F-Measure: " + fMeasure);
			
		}
		rl.sortByCol(3, MyRecordList.SortDESC);
		rl.saveToFile(csvOutput);
	}

	
	private static void experiment1(String input, String output) throws Exception {
		// CSV OUTPUT
		MyRecordList rl = new MyRecordList();
		rl.setHeader("Recruitment Field/Area", "Approach", "Precision", "Recall", "F-Measure");
		
		String fJ1_01B1 = "01B1_jensen1.arff";
		String fJ8_01B1 = "01B1_jensen8.arff";
		String fSVM_01B1 = "01B1_svm.arff";
		String fJ1_13A1 = "13A1_jensen1.arff";
		String fJ8_13A1 = "13A1_jensen8.arff";
		String fSVM_13A1 = "13A1_svm.arff";
		String fJ1_01 = "01_jensen1.arff";
		String fJ8_01 = "01_jensen8.arff";
		String fSVM_01 = "01_svm.arff";
		String fJ1_13 = "13_jensen1.arff";
		String fJ8_13 = "13_jensen8.arff";
		String fSVM_13 = "13_svm.arff";
		
		/*
		 * RF: 01/B1 (Informatics)
		 */
		DataSource dataSource = new DataSource(input + File.separator + fJ1_01B1);
		Instances dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		Evaluation eval = doLogisticClassification(dataSet, "Abilitato");
		rl.addRecord("01/B1", "JLOG-1", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("01/B1 (JLOG-1): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));
		
		dataSource = new DataSource(input + File.separator + fJ8_01B1);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doLogisticClassification(dataSet, "Abilitato");
		rl.addRecord("01/B1", "JLOG-8", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("01/B1 (JLOG-8): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));

		dataSource = new DataSource(input + File.separator + fSVM_01B1);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doSvmClassification(dataSet, "Abilitato");
		rl.addRecord("01/B1", "SVM", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("01/B1 (SVM): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));
		
				
		/*
		 * RF: 13/A1 (Economics)
		 */
		dataSource = new DataSource(input + File.separator + fJ1_13A1);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doLogisticClassification(dataSet, "Abilitato");
		rl.addRecord("13/A1", "JLOG-1", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("13/A1 (JLOG-1): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));
		
		dataSource = new DataSource(input + File.separator + fJ8_13A1);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doLogisticClassification(dataSet, "Abilitato");
		rl.addRecord("13/A1", "JLOG-8", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("13/A1 (JLOG-8): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));

		dataSource = new DataSource(input + File.separator + fSVM_13A1);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doSvmClassification(dataSet, "Abilitato");
		rl.addRecord("13/A1", "SVM", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("13/A1 (SVM): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));

		/*
		 * Area: 01 (Mathematics and Computer Science) 
		 */
		dataSource = new DataSource(input + File.separator + fJ1_01);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doLogisticClassification(dataSet, "Abilitato");
		rl.addRecord("01", "JLOG-1", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("01 (JLOG-1): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));
		
		dataSource = new DataSource(input + File.separator + fJ8_01);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doLogisticClassification(dataSet, "Abilitato");
		rl.addRecord("01", "JLOG-8", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("01 (JLOG-8): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));

		dataSource = new DataSource(input + File.separator + fSVM_01);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doSvmClassification(dataSet, "Abilitato");
		rl.addRecord("01", "SVM", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("01 (SVM): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));

		/*
		 * Area: 13 (Economics and Statistics)
		 */
		dataSource = new DataSource(input + File.separator + fJ1_13);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doLogisticClassification(dataSet, "Abilitato");
		rl.addRecord("13", "JLOG-1", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("13 (JLOG-1): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));
		
		dataSource = new DataSource(input + File.separator + fJ8_13);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doLogisticClassification(dataSet, "Abilitato");
		rl.addRecord("13", "JLOG-8", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("13 (JLOG-8): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));

		dataSource = new DataSource(input + File.separator + fSVM_13);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doSvmClassification(dataSet, "Abilitato");
		rl.addRecord("13", "SVM", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("13 (SVM): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));

		rl.saveToFile(output);
	}
	

	private static void experiment2(String input, String output) throws Exception {
		// CSV OUTPUT
		MyRecordList rl = new MyRecordList();
		rl.setHeader("Recruitment Field", "Approach", "Precision", "Recall", "F-Measure");
		
		String fTregella_05E2 = "05E2_tregella.arff";
		String fSVM_05E2 = "05E2_svm.arff";
		String fTregella_13A1 = "13A1_tregella.arff";
		String fSVM_13A1 = "13A1_svm.arff";
		
		/*
		 * RF: 05/E2 (Molecular biology)
		 */
		DataSource dataSource = new DataSource(input + File.separator + fTregella_05E2);
		Instances dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		Evaluation eval = doLogisticClassification(dataSet, "Abilitato");
		rl.addRecord("05/E2", "T-LR", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("05/E2 (T-LR): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));
		
		eval = doSvmClassification(dataSet, "Abilitato");
		rl.addRecord("05/E2", "T-SVM", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("05/E2 (T-SVM): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));
		
		dataSource = new DataSource(input + File.separator + fSVM_05E2);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doSvmClassification(dataSet, "Abilitato");
		rl.addRecord("05/E2", "OUR-SVM", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("05/E2 (OUR-SVM): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));


		/*
		 * RF: 13/A1 (Economics)
		 */
		dataSource = new DataSource(input + File.separator + fTregella_13A1);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doLogisticClassification(dataSet, "Abilitato");
		rl.addRecord("13/A1", "T-LR", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("13/A1 (T-LR): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));
		
		eval = doSvmClassification(dataSet, "Abilitato");
		rl.addRecord("13/A1", "T-SVM", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("13/A1 (T-SVM): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));

		dataSource = new DataSource(input + File.separator + fSVM_13A1);
		dataSet = dataSource.getDataSet();
		dataSet.setClass(dataSet.attribute("Abilitato"));
		eval = doSvmClassification(dataSet, "Abilitato");
		rl.addRecord("13/A1", "OUR-SVM", roundDouble(eval.precision(0)), roundDouble(eval.recall(0)), roundDouble(eval.fMeasure(0)));
		LOG.info("13/A1 (OUR-SVM): " + roundDouble(eval.precision(0)) + " - " + roundDouble(eval.recall(0)) + " - " + roundDouble(eval.fMeasure(0)));

		rl.saveToFile(output);
	}
}
