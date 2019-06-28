-- Reads a sparse representation of a graph <OriginURL>\t<TargetURL1>\t ....
-- Extracts the OriginURL (which is the crawled URL)
-- Sorts and dedups the URLs
--
-- INPUT: Sparse Graph <OriginURL>\t<TargetURL1>\t ....
-- OUTPUt: Sorted, unique URL list
-- Created by Robert Meusel - 2014-06-06
--

-- use gzip compression in result and intermediary files
SET mapred.task.timeout 900000;
SET mapred.output.compress true;
SET mapred.output.compression.codec org.apache.hadoop.io.compress.GzipCodec;
SET pig.tmpfilecompression true;
SET pig.tmpfilecompression.codec gz;

%default results '/unique-url-list';
%default input 's3n://cc2014graphdata/data/'
%default divider '	'; -- \t

-- load the data (but we only read till the fist tab)
sparsenetwork = LOAD '$input' USING PigStorage('$divider') as (origin:chararray, rest:chararray);
-- reduce data
crawledurls = FOREACH sparsenetwork GENERATE origin;

-- distinct
distincturls = DISTINCT crawledurls;
-- sort
sortedurls = ORDER distincturls BY origin;
-- store
STORE sortedurls INTO '$results/sortedurllist';
