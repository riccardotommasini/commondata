# Hannes Muehleisen <hannes@muehleisen.org>, 2012-06-29

import sys,csv,os;

infile = sys.argv[1]
dataset = sys.argv[2]
group = sys.argv[3]
dimension = sys.argv[4]
reportType = sys.argv[5]

outfile = infile.replace(".csv",".html")

data = csv.reader(open(infile), delimiter='\t',quoting=csv.QUOTE_NONE)

title = group + " by " + dimension + " for " + dataset

out = "<!DOCTYPE html>\n<html><head><title>"+title+" | Web Data Commons Analysis Results</title><link rel='stylesheet' href='http://www4.wiwiss.fu-berlin.de/bizer/sieve/style.css' type='text/css' media='screen'/></head><body><a name='top'></a><h1>"+title+"</h1>"

if reportType == "cooc":
	out += "<h2>Co-Occurrence Groups</h2><br>"
	out += "<table>"
	out += "<tr><th>"+group+" Groups</th><th>"+dimension+" / Total Support</th><th>"+dimension+" / Percentage Support</th></tr>"
	for line in data:
		supportCount = long(line[0])
		supportPercent = round(float(line[1])*100,2)
		groups = ""
		for elem in line[2:]:
			groups += elem + "<br>"
		out += "<tr style='font-family:monospace;'><td>"+groups+"</td><td style='text-align:right;'>"+'{0:,}'.format(supportCount)+"</td><td style='text-align:right;'>"+str(supportPercent)+"%</td></tr>\n"
	out += "</table>"
	
if reportType == "top":
	out += "<h2>Top Occurrences</h2><br>"
	out += "<table>"
	out += "<tr><th>"+group+"</th><th>"+dimension+" / Total</th><th>"+dimension+" / Percentage</th></tr>"
	for line in data:
		out += "<tr style='font-family:monospace;'><td>"+line[0]+"</td><td style='text-align:right;'>"+'{0:,}'.format(long(line[1]))+"</td><td style='text-align:right;'>"+str(round(float(line[2])*100,2))+"%</td></tr>\n"
	out += "</table>"

out += "<br><br><a href='"+os.path.basename(infile)+"'>CSV version</a><br><br><a href='index.html'>Back</a>"
out += "</body></html>"

f = open(outfile, 'w')
f.write(out)

