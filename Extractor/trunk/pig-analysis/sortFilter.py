# Sort and filter a tab-separated CSV file (technically a TSV), with a long value in the last column
# Usage: python sortFilter.py [tab-separated-file.csv] [min value for long]

# Hannes Muehleisen <hannes@muehleisen.org>, 2012-06-28

import sys,csv, os;
#test if folder or file was input
path = sys.argv[1]
fileList = []
if os.path.isfile(path):
	fileList.append(path)
else:
	# go through all directories - result
	for name in os.listdir(path):
		subPath = os.path.join(path, name)
		if os.path.isfile(subPath):
			continue
		# e.g. classesEntites
		allFilePath = os.path.join(path, name + '.csv')
		allFile = open(allFilePath , "w")
		print("Scanning: " + subPath)
		for subName in os.listdir(subPath):
			if (subName.startswith('part')):
				subFile = os.path.join(subPath, subName)
				print("Reading file: " + str(subFile))
				if os.path.isfile(subFile): 
					for line in open(subFile):
						allFile.write(line)
		allFile.close()
		fileList.append(allFilePath)	
for file in fileList:
	data = csv.reader(open(file), delimiter='\t',quoting=csv.QUOTE_NONE)
	limit = long(sys.argv[2])
	try:
		outputDelimiter = sys.argv[3]
	except:
		sys.stderr.write('Using default delimiter for output file \\t \n')
		outputDelimiter = '\t'
	filteredData = []
	for line in data:
		try:
			if long(line[len(line)-1]) < limit:
				continue
	
			filteredData.append(line)
			prefix = ''
		except: 
			sys.stderr.write('Error in '+sys.argv[1]+': '+str(line)+'\n')
			continue
	
	
	sortedData = sorted(filteredData, key=lambda line: long(line[len(line)-1]), reverse=True)
	
	#resWriter = csv.writer(sys.stdout, delimiter='\t',escapechar='\\',quoting=csv.QUOTE_NONE)
	outputFileName = file.replace('.csv', '_sorted.csv')
	outputFile = open(outputFileName, 'w')
	resWriter = csv.writer(outputFile, delimiter=outputDelimiter ,escapechar='\\',quoting=csv.QUOTE_NONE)
	for line in sortedData:
		resWriter.writerow(line)
