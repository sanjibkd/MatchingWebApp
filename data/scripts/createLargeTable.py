import os
import sys
import csv
import copy

infile = "../books/walmart.csv"

times = sys.argv[1]
times = int(times)

outfile = "../books/largewalmart" + str(times*100) + ".csv"

outfile = open(outfile, 'wb')

count = 0;
with open(infile, 'rb') as csvfile:
	reader = csv.reader(csvfile)
	writer = csv.writer(outfile)
	header = True
	for row in reader:
		count += 1
		if (header):
			writer.writerow(row)
			header = False
			continue
		for time in range(times):
			id = row[0]
			newid = id + "_" + str(time)
			newrows = copy.deepcopy(row)
			newrows[0] = newid
			writer.writerow(newrows)
		if (not (count % 1000)):
			print "processed: " + str(count)
outfile.close()
			








