package it.unibo.disi.asnanalysis.mylib;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class DoLaTeXTable {

	static String input = "data/output/FeatureSel_Level2.csv"; //"data/asn/RF_L2.csv";
	static String[] bibl = {"01/A1","01/A2","01/A3","01/A4","01/A5","01/A6","01/B1","02/A1","02/A2","02/B1","02/B2","02/B3","02/C1","03/A1","03/A2","03/B1","03/B2","03/C1","03/C2","03/D1","03/D2","04/A1","04/A2","04/A3","04/A4","05/A1","05/A2","05/B1","05/B2","05/C1","05/D1","05/E1","05/E2","05/F1","05/G1","05/H1","05/H2","05/I1","06/A1","06/A2","06/A3","06/A4","06/B1","06/C1","06/D1","06/D2","06/D3","06/D4","06/D5","06/D6","06/E1","06/E2","06/E3","06/F1","06/F2","06/F3","06/F4","06/G1","06/H1","06/I1","06/L1","06/M1","06/M2","06/N1","07/A1","07/B1","07/B2","07/C1","07/D1","07/E1","07/F1","07/F2","07/G1","07/H1","07/H2","07/H3","07/H4","07/H5","08/A1","08/A2","08/A3","08/A4","08/B1","08/B2","08/B3","09/A1","09/A2","09/A3","09/B1","09/B2","09/B3","09/C1","09/C2","09/D1","09/D2","09/D3","09/E1","09/E2","09/E3","09/E4","09/F1","09/F2","09/G1","09/G2","09/H1","11/E1","11/E2","11/E3","11/E4"};
	static String[] nonBibl = {"08/C1","08/D1","08/E1","08/E2","08/F1","10/A1","10/B1","10/C1","10/D1","10/D2","10/D3","10/D4","10/E1","10/F1","10/F2","10/F3","10/G1","10/H1","10/I1","10/L1","10/M1","10/M2","10/N1","10/N3","11/A1","11/A2","11/A3","11/A4","11/A5","11/B1","11/C1","11/C2","11/C3","11/C4","11/C5","11/D1","11/D2","12/A1","12/B1","12/B2","12/C1","12/C2","12/D1","12/D2","12/E1","12/E2","12/E3","12/F1","12/G1","12/G2","12/H1","12/H2","12/H3","13/A1","13/A2","13/A3","13/A4","13/A5","13/B1","13/B2","13/B3","13/B4","13/B5","13/C1","13/D1","13/D2","13/D3","13/D4","14/A1","14/A2","14/B1","14/B2","14/C1","14/C2","14/D1"};
	
	static String grayed = "\\cellcolor[gray]{0.8}";
	
	public static void main(String[] args) throws IOException {
		/*
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(input));
	    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
	    		.withQuoteMode(QuoteMode.MINIMAL)
                .withHeader("Recruitment Field", "Precision", "Recall", "F-Measure"));		
		*/
		Reader in = new FileReader(input);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
		int i = -1;
		ArrayList<String> tableRows = new ArrayList<String>();
		for (CSVRecord record : records) {
		    if (i==-1) {
		    	i=0;
		    	continue;
		    }
			int col = (i/46) + 1;
		    String rf = record.get(0);
		    String p = record.get(1);
		    String r = record.get(2);
		    String fm = record.get(3);
		    String line = "";
		    if (Arrays.asList(nonBibl).contains(rf)) {
			    // NON-Bibl.
		    	//System.out.println(col + ") " + rf + " - " + fm + " (NON-Bibl.)");
		    	line = rf + "\\cellcolor[gray]{0.8} & " + p + "\\cellcolor[gray]{0.8} & " + r + "\\cellcolor[gray]{0.8} & " + fm + "\\cellcolor[gray]{0.8}";
		    } else {
		    	// Bibl.
		    	//System.out.println(col + ") " + rf + " - " + fm);
		    	line = rf + " & " + p + " & " + r + " & " + fm;
		    }
		    
    		//System.out.println(i%46);
		    if (col == 1) {
	    		tableRows.add(line + " & ");
	    	} else if (col == 4) {
	    		String temp = tableRows.get(i%46);
	    		tableRows.set(i%46, temp + line + " \\\\\n");
	    	} else {
	    		String temp = tableRows.get(i%46);
	    		tableRows.set(i%46, temp + line + " & ");
	    	}
		    i++;
		}
		
		for (String row : tableRows) {
			System.out.print(row);
		}
	}

}
