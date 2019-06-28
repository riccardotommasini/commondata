-- This Pig Latin script calculates the parsing errors for URLs for RDF quads.
--
--
-- The script depends on custom Pig functions and a NQuads loader. 
-- That code is contained in the loddesc-core package (provided, hopefully).
-- 
-- Created: Robert Meusel <robert@informatik.uni-mannheim.de>, 2012-06-27
-- Tested on Pig version 0.9.2
--
-- Run as follows: 
-- pig -p code=/path/to/loddesc-core-0.1-with-dependencies.jar \\
--     -p input=/path/to/some/nquads/files/or/directories.nq   \\
-- 	   -p results=/directory/to/which/outputs/are/written      \\
--     /path/to/this/file/wdc-base.pig

%default results './wdc-test-results';
%default code './loddesc-core-0.1-with-dependencies.jar'; -- our custom RDF loader
%default input './wdc-example.nq';

-- divider for output csv files
%default divider '	'; -- \t

-- number of reducers, should be - say - four times the number of reducer slots in the Hadoop cluster
SET default_parallel 2;

-- use gzip compression in result and intermediary files
SET mapred.output.compress true;
SET mapred.output.compression.codec org.apache.hadoop.io.compress.GzipCodec;
SET pig.tmpfilecompression true;
SET pig.tmpfilecompression.codec gz;

-- register our UDFs and NQuads loader
register '$code';


-- load data
quads = load '$input' using de.wbsg.loddesc.importer.QuadLoader() AS (subject:chararray,predicate:chararray,object:tuple(ntype:int,value:chararray,dtlang:chararray),graph:chararray);

-- generate pure error data
domains = FOREACH quads GENERATE graph;
domains = DISTINCT domains;
domainInfos = FOREACH domains GENERATE graph as url, de.wbsg.loddesc.functions.Domain(graph) as domain, de.wbsg.loddesc.functions.PayLevelDomain(graph) as pld, de.wbsg.loddesc.functions.TopLevelDomain(graph) as tld;
errors = FILTER domainInfos BY (domain == tld);
errors = ORDER errors by tld, pld, domain;
STORE errors INTO '$results/csv' USING PigStorage('$divider');

-- count urls which are parsed wrong
errorUrls = FOREACH errors GENERATE url;
errorUrlsDist = DISTINCT errorUrls;
errorUrlsGroup = GROUP errorUrlsDist ALL;
errorUrlsCount = FOREACH errorUrlsGroup GENERATE COUNT(errorUrlsDist);
STORE errorUrlsCount INTO '$results/errorUrlsCount' USING PigStorage('$divider');

-- count plds which come from wrong parsed urls
errorPlds = FOREACH errors GENERATE pld;
errorPldsDist = DISTINCT errorPlds;
errorPldsGroup = GROUP errorPldsDist ALL;
errorPldsCount = FOREACH errorPldsGroup GENERATE COUNT(errorPldsDist);
STORE errorPldsCount INTO '$results/errorPldsCount' USING PigStorage('$divider');

-- count tlds which come from wrong parsed urls
errorTlds = FOREACH errors GENERATE tld;
errorTldsDist = DISTINCT errorTlds;
errorTldsGroup = GROUP errorTldsDist ALL;
errorTldsCount = FOREACH errorTldsGroup GENERATE COUNT(errorTldsDist);
STORE errorTldsCount INTO '$results/errorTldsCount' USING PigStorage('$divider');

