import matplotlib.pyplot as plt
import csv

directory = 'data/'

filenames = {'a_all':'ANVUR-BoxPlot - SVM_AREA_allPredictors.csv',
	'rf_all':'ANVUR-BoxPlot - SVM_RF_allPredictors.csv',
	'rf_sel':'ANVUR-BoxPlot - SVM_RF_selected.csv'}

'''
struttura data:
{
	'I': {
		'B': {
			'P': [...],
			'R': [...],
			'FM': [...]
		},
		'NB': {
			'P': [...],
			'R': [...],
			'FM': [...]
		}
	}
	'II': {COME SOPRA}data = {}
}
'''

data = {}
data['I'] = {'B':{'P': [],'R':[], 'FM':[]}, 'NB':{'P': [],'R':[], 'FM':[]}}
data['II'] = {'B':{'P': [],'R':[], 'FM':[]}, 'NB':{'P': [],'R':[], 'FM':[]}}

'''
label = {'I':{}, 'II':{}}
label['I']['B'] = '(a) Full Professor - bibliometric'
label['I']['NB'] = '(b) Full Professor - non-bibliometric'
label['II']['B'] = '(c) Associate Professor - bibliometric'
label['II']['NB'] = '(d) Associate Professor - non-bibliometric'
'''
label = {
	'a_all': {
		'I': {'B': '(a) Full Professor - bibliometric RFs',
			'NB': '(b) Full Professor - non-bibliometric RFs'},
		'II': {'B': '(c) Associate Professor - bibliometric RFs',
			'NB': '(d) Associate Professor - non-bibliometric RFs'}
	},
	'rf_all': {
		'I': {'B': '(a) Full Professor - bibliometric Areas',
			'NB': '(b) Full Professor - non-bibliometric Areas'},
		'II': {'B': '(c) Associate Professor - bibliometric Areas',
			'NB': '(d) Associate Professor - non-bibliometric Areas'}
	},
	'rf_sel': {
		'I': {'B': '(a) Full Professor - bibliometric RFs',
			'NB': '(b) Full Professor - non-bibliometric RFs'},
		'II': {'B': '(c) Associate Professor - bibliometric RFs',
			'NB': '(d) Associate Professor - non-bibliometric RFs'}
	}
}


title = {'I':{}, 'II':{}}
title['I']['B'] = 'Level I (bibliometric) - all areas - all predictors'
title['I']['NB'] = 'Level I (non bibliometric) - all areas - all predictors'
title['II']['B'] = 'Level II (bibliometric) - all areas - all predictors'
title['II']['NB'] = 'Level II (non bibliometric) - all areas - all predictors'

title_multicolumn = {
	'a_all': 'Performance of the SVM algorithm - 291 predictors',
	'rf_all': 'Performance of the SVM algorithm - 291 predictors',
	'rf_sel': 'Performance of the SVM algorithm - top 15 predictors'
}

limy = {'a_all': {
		'I': {'min':0.55, 'max':0.90}, 
		'II': {'min':0.55, 'max':0.90}},
	'rf_all': {
		'I': {'min':0.35, 'max':1.02}, 
		'II': {'min':0.35, 'max':1.02}},
	'rf_sel': {
		'I': {'min':0.3, 'max':1.02}, 
		'II': {'min':0.3, 'max':1.02}}
}


outfilename = {
	'a_all': {
		'I': {'B': 'svm_areas_levelI_bibliometric_allpredictors',
			'NB': 'svm_areas_levelI_nonbibliometric_allpredictors'},
		'II': {'B': 'svm_areas_levelII_bibliometric_allpredictors',
			'NB': 'svm_areas_levelII_nonbibliometric_allpredictors'}
	},
	'rf_all': {
		'I': {'B': 'svm_recrFields_levelI_bibliometric_allpredictors',
			'NB': 'svm_recrFields_levelI_nonbibliometric_allpredictors'},
		'II': {'B': 'svm_recrFields_levelII_bibliometric_allpredictors',
			'NB': 'svm_recrFields_levelII_nonbibliometric_allpredictors'}
	},
	'rf_sel': {
		'I': {'B': 'svm_recrFields_levelI_bibliometric_selectedpredictors',
			'NB': 'svm_recrFields_levelI_nonbibliometric_selectedpredictors'},
		'II': {'B': 'svm_recrFields_levelII_bibliometric_selectedpredictors',
			'NB': 'svm_recrFields_levelII_nonbibliometric_selectedpredictors'}
	}
}

outfilename_multicolumn = {
	'a_all': 'svm_areas_allpredictors',
	'rf_all': 'svm_recFields_allpredictors',
	'rf_sel': 'svm_recFields_selectedpredictors'
}


def drawplot(arrdata, label, title,outfilename,outformat):
	fig = plt.figure(figsize=(5,6))
	#plt.boxplot([x for x in [p, r, fm]], 0, 'rs', 1)
	#plt.xticks([y+1 for y in range(len([p,r,fm]))], ['Precision', 'Recall', 'F-measure'])
	#plt.boxplot([x for x in arrdata], 0, 'rs', 1)
	#boxprops = dict(linewidth=3, color='darkgoldenrod')
	plt.boxplot([x for x in arrdata], sym='x',widths=[0.7,0.7,0.7])
	plt.xticks([y+1 for y in range(len(arrdata))], ['Precision', 'Recall', 'F-measure'])
	plt.xlabel(label)#'measurement x')
	#t = plt.title(title)#'Box plot')
	plt.ylim(ymin=0)
	plt.savefig(outfilename+'.'+outformat)
	#plt.show()

def drawplot_multiline(arrdata, label, title,outfilename,outformat,limy):
	fig = plt.figure(figsize=(8,10))
	plt.subplot(2,2,1)
	plt.boxplot([x for x in arrdata['I']['B']], sym='',widths=[0.6,0.6,0.6])
	plt.xticks([y+1 for y in range(len(arrdata['I']['B']))], ['Precision', 'Recall', 'F-measure'])
	plt.xlabel(label['I']['B'])#'measurement x')
	t = plt.title(title,loc='center')
	plt.ylim(ymin=limy['I']['min'])
	plt.ylim(ymax=limy['I']['max'])
	
	plt.subplot(2,2,2)
	plt.boxplot([x for x in arrdata['I']['NB']], sym='',widths=[0.6,0.6,0.6])
	plt.xticks([y+1 for y in range(len(arrdata['I']['NB']))], ['Precision', 'Recall', 'F-measure'])
	plt.xlabel(label['I']['NB'])#'measurement x')
	plt.ylim(ymin=limy['I']['min'])
	plt.ylim(ymax=limy['I']['max'])
	
	plt.subplot(2,2,3)
	plt.boxplot([x for x in arrdata['II']['B']], sym='',widths=[0.6,0.6,0.6])
	plt.xticks([y+1 for y in range(len(arrdata['II']['B']))], ['Precision', 'Recall', 'F-measure'])
	plt.xlabel(label['II']['B'])#'measurement x')
	plt.ylim(ymin=limy['II']['min'])
	plt.ylim(ymax=limy['II']['max'])

	plt.subplot(2,2,4)
	plt.boxplot([x for x in arrdata['II']['NB']], sym='',widths=[0.6,0.6,0.6])
	plt.xticks([y+1 for y in range(len(arrdata['II']['NB']))], ['Precision', 'Recall', 'F-measure'])
	plt.xlabel(label['II']['NB'])#'measurement x')
	plt.ylim(ymin=limy['II']['min'])
	plt.ylim(ymax=limy['II']['max'])

	plt.savefig(outfilename+'.'+outformat)

for keyword in filenames.keys():
	filename = filenames[keyword]
	with open(directory+filename, 'rb') as csvfile:
		reader = csv.DictReader(csvfile)
		for row in reader:
			if row['Level'] == 'I':
				if row['Bibliometric?'] == 'B':
					data['I']['B']['P'].append(float(row['Precision']))
					data['I']['B']['R'].append(float(row['Recall']))
					data['I']['B']['FM'].append(float(row['F-measure']))
				else:
					data['I']['NB']['P'].append(float(row['Precision']))
					data['I']['NB']['R'].append(float(row['Recall']))
					data['I']['NB']['FM'].append(float(row['F-measure']))
			elif row['Level'] == 'II':
				if row['Bibliometric?'] == 'B':
					data['II']['B']['P'].append(float(row['Precision']))
					data['II']['B']['R'].append(float(row['Recall']))
					data['II']['B']['FM'].append(float(row['F-measure']))
				else:
					data['II']['NB']['P'].append(float(row['Precision']))
					data['II']['NB']['R'].append(float(row['Recall']))
					data['II']['NB']['FM'].append(float(row['F-measure']))

		res = {'I':{}, 'II':{}}
		res['I']['B'] = [data['I']['B']['P'], data['I']['B']['R'], data['I']['B']['FM']]
		res['I']['NB'] = [data['I']['NB']['P'], data['I']['NB']['R'], data['I']['NB']['FM']]
		res['II']['B'] = [data['II']['B']['P'], data['II']['B']['R'], data['II']['B']['FM']]
		res['II']['NB'] = [data['II']['NB']['P'], data['II']['NB']['R'], data['II']['NB']['FM']]


		#drawplot(res['I']['B'],label['I']['B'],title['I']['B'],outfilename[keyword]['I']['B'],'pdf')
		#drawplot(res['I']['NB'],label['I']['NB'],title['I']['NB'],outfilename[keyword]['I']['NB'],'pdf')
		#drawplot(res['II']['B'],label['II']['B'],title['II']['B'],outfilename[keyword]['II']['B'],'pdf')
		#drawplot(res['II']['NB'],label['II']['NB'],title['II']['NB'],outfilename[keyword]['II']['NB'],'pdf')
		
		drawplot_multiline(res, label[keyword], title_multicolumn[keyword], outfilename_multicolumn[keyword],'pdf', limy[keyword])
		
		'''
		fig = plt.figure(figsize=(5,6))
		plt.xticks([y+1 for y in range(len(res['I']['B']))], ['Precision', 'Recall', 'F-measure'])
		f, (ax1, ax2) = plt.subplots(1,2,sharey=True)
		ax1.boxplot([x for x in res['I']['B']], sym='x',widths=[0.7,0.7,0.7])
		ax1.set_title(label)#'measurement x')
		#t = plt.title(title)#'Box plot')
		#plt.ylim(ymin=0)
		#plt.savefig(outfilename+'.'+outformat)
		
		#fig = plt.figure(figsize=(5,6))
		#plt.subplot(2,1,2)
		ax2.boxplot([x for x in res['I']['NB']], sym='x',widths=[0.7,0.7,0.7])
		ax2.xticks([y+1 for y in range(len(res['I']['NB']))], ['Precision', 'Recall', 'F-measure'])
		ax2.set_title(label)#'measurement x')
		plt.show()
		'''
		
