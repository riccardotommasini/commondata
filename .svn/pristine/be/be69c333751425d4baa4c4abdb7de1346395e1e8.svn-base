#!/bin/sh

for dataset in 2010-rdfa 2012-rdfa 2010-microdata 2012-microdata
do
	python htmlize.py data/$dataset-classes-entities.csv $dataset Class Entities top
	python htmlize.py data/$dataset-classes-urls.csv $dataset Class URLs top
	python htmlize.py data/$dataset-classes-plds.csv $dataset Class PLDs top

	python htmlize.py data/$dataset-properties-entities.csv $dataset Property Entities top
	python htmlize.py data/$dataset-properties-urls.csv $dataset Property URLs top
	python htmlize.py data/$dataset-properties-plds.csv $dataset Property PLDs top

	python htmlize.py data/$dataset-vocabs-entities.csv $dataset Vocabulary Entities top
	python htmlize.py data/$dataset-vocabs-urls.csv $dataset Vocabulary URLs top
	python htmlize.py data/$dataset-vocabs-plds.csv $dataset Vocabulary PLDs top

	python htmlize.py data/$dataset-cooc-properties-entities.csv $dataset Property Entities cooc
	python htmlize.py data/$dataset-cooc-properties-urls.csv $dataset Property URLs cooc
	python htmlize.py data/$dataset-cooc-properties-plds.csv $dataset Property PLDs cooc
done


