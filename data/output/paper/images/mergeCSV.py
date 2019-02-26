import csv
import sys

rf = ['csv-input/RFs-Level1.csv','csv-input/RFs-Level2.csv']
a = ['csv-input/Areas-Level1.csv','csv-input/Areas-Level2.csv']
fs = ['csv-input/FeatureSel-Level1.csv', 'csv-input/FeatureSel-Level2.csv']

rfOut = 'data/ANVUR-BoxPlot - SVM_RF_allPredictors.csv'
aOut = 'data/ANVUR-BoxPlot - SVM_AREA_allPredictors.csv'
fsOut = 'data/ANVUR-BoxPlot - SVM_RF_selected.csv'

rfBibl = ['01/A1','01/A2','01/A3','01/A4','01/A5','01/A6','01/B1','02/A1','02/A2','02/B1','02/B2','02/B3','02/C1','03/A1','03/A2','03/B1','03/B2','03/C1','03/C2','03/D1','03/D2','04/A1','04/A2','04/A3','04/A4','05/A1','05/A2','05/B1','05/B2','05/C1','05/D1','05/E1','05/E2','05/F1','05/G1','05/H1','05/H2','05/I1','06/A1','06/A2','06/A3','06/A4','06/B1','06/C1','06/D1','06/D2','06/D3','06/D4','06/D5','06/D6','06/E1','06/E2','06/E3','06/F1','06/F2','06/F3','06/F4','06/G1','06/H1','06/I1','06/L1','06/M1','06/M2','06/N1','07/A1','07/B1','07/B2','07/C1','07/D1','07/E1','07/F1','07/F2','07/G1','07/H1','07/H2','07/H3','07/H4','07/H5','08/A1','08/A2','08/A3','08/A4','08/B1','08/B2','08/B3','09/A1','09/A2','09/A3','09/B1','09/B2','09/B3','09/C1','09/C2','09/D1','09/D2','09/D3','09/E1','09/E2','09/E3','09/E4','09/F1','09/F2','09/G1','09/G2','09/H1','11/E1','11/E2','11/E3','11/E4']
rfNonBibl = ['08/C1','08/D1','08/E1','08/E2','08/F1','10/A1','10/B1','10/C1','10/D1','10/D2','10/D3','10/D4','10/E1','10/F1','10/F2','10/F3','10/G1','10/H1','10/I1','10/L1','10/M1','10/M2','10/N1','10/N3','11/A1','11/A2','11/A3','11/A4','11/A5','11/B1','11/C1','11/C2','11/C3','11/C4','11/C5','11/D1','11/D2','12/A1','12/B1','12/B2','12/C1','12/C2','12/D1','12/D2','12/E1','12/E2','12/E3','12/F1','12/G1','12/G2','12/H1','12/H2','12/H3','13/A1','13/A2','13/A3','13/A4','13/A5','13/B1','13/B2','13/B3','13/B4','13/B5','13/C1','13/D1','13/D2','13/D3','13/D4','14/A1','14/A2','14/B1','14/B2','14/C1','14/C2','14/D1']
areaBibl = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '11/E']
areaNonBibl = ['10', '11', '12', '13', '14', '08-NB']

# RECRUITMENT FIELD
res = 'Recr. Field,Bibliometric?,Level,Precision,Recall,F-measure\n'
with open(rf[0], 'rb') as csvfile:
	spamreader = csv.reader(csvfile, delimiter=',') #, quotechar='|')
	next(spamreader)
	for row in spamreader:
		if row[0] in rfBibl:
			res += '%s,B,I,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		elif row[0] in rfNonBibl:
			res += '%s,NB,I,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		else:
			print "ERRORE!" + row[0]
			sys.exit()

with open(rf[1], 'rb') as csvfile:
	spamreader = csv.reader(csvfile, delimiter=',') #, quotechar='|')
	next(spamreader)
	for row in spamreader:
		if row[0] in rfBibl:
			res += '%s,B,II,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		elif row[0] in rfNonBibl:
			res += '%s,NB,II,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		else:
			print "ERRORE: - " + row[0]
			sys.exit()

with open(rfOut, "w") as text_file:
    text_file.write(res)

# AREA
res = 'Area,Bibliometric?,Level,Precision,Recall,F-measure\n'
with open(a[0], 'rb') as csvfile:
	spamreader = csv.reader(csvfile, delimiter=',') #, quotechar='|')
	next(spamreader)
	for row in spamreader:
		if row[0] in areaBibl:
			res += '%s,B,I,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		elif row[0] in areaNonBibl:
			res += '%s,NB,I,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		else:
			print "ERRORE!" + row[0] + " - " + a[0]
			sys.exit()

with open(a[1], 'rb') as csvfile:
	spamreader = csv.reader(csvfile, delimiter=',') #, quotechar='|')
	next(spamreader)
	for row in spamreader:
		if row[0] in areaBibl:
			res += '%s,B,II,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		elif row[0] in areaNonBibl:
			res += '%s,NB,II,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		else:
			print "ERRORE: - " + row[0] + " - " + a[1]
			sys.exit()

with open(aOut, "w") as text_file:
    text_file.write(res)


# RECRUITMENT FIELD - Feature Selection
res = 'Recr. Field,Bibliometric?,Level,Precision,Recall,F-measure\n'
with open(fs[0], 'rb') as csvfile:
	spamreader = csv.reader(csvfile, delimiter=',') #, quotechar='|')
	next(spamreader)
	for row in spamreader:
		if row[0] in rfBibl:
			res += '%s,B,I,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		elif row[0] in rfNonBibl:
			res += '%s,NB,I,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		else:
			print "ERRORE!" + row[0]
			sys.exit()

with open(fs[1], 'rb') as csvfile:
	spamreader = csv.reader(csvfile, delimiter=',') #, quotechar='|')
	next(spamreader)
	for row in spamreader:
		if row[0] in rfBibl:
			res += '%s,B,II,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		elif row[0] in rfNonBibl:
			res += '%s,NB,II,%s,%s,%s\n' % (row[0], row[1], row[2], row[3])
		else:
			print "ERRORE: - " + row[0]
			sys.exit()

with open(fsOut, "w") as text_file:
    text_file.write(res)

