package it.unibo.disi.asnanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FileUtils;

public class MyRecordList {

	private List<String[]> records;
	private String[] header;
	final static int SortASC = 1;
	final static int SortDESC = -1;

	
	public MyRecordList() {
		records = new ArrayList<String[]>();
	}
		
	public void setHeader(String... args) {
		this.header = args;
	}
	
	public void addRecord(String... args) {
		records.add(args);
	}

	public void saveToFile(String filename) throws IOException {
		FileUtils.forceMkdirParent(new File(filename));
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
	    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
	    		.withQuoteMode(QuoteMode.MINIMAL)
                .withHeader(header));
	    for (String[] record : records) {
	    	csvPrinter.printRecord(record);
	    }
	    csvPrinter.flush();
		csvPrinter.close();
	}
	
	public String toString() {
		String res = "";
		for (String[] record : records) {
	    	res += String.join(" - ", record) + "\n";
	    }
		return res;
	}
	
	public void sortByCol(int i, int sortDirection){
		
		//comparator by specific col
		Comparator<String[]> comp = new Comparator<String[]>(){
			public int compare(String[] a, String[] b){
				//reverse result if DESC (sortDirection = -1)
				return sortDirection * a[i].compareTo(b[i]);
			}
		};
		
		Collections.sort(records, comp);
	}
	
}
