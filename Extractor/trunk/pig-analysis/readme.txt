# Combine results from PIG, for each folder:
zcat part-*.gz > all.csv

# Aggregate co-occurence groups into groups with more than two elements with a support limit of 0.001:
java -cp ./target/ccrdf*.jar -Xmx1G org.webdatacommons.analysis.CoocGroupAggregator someCoocCsvFile.csv 0.001 > someAggregatedCsvFile.csv

# to throw out small values from the csv files, you can pass them through sortFilter.py. First argument is the file, second the minimum value.
# python sortFilter.py someCsvFile 42

