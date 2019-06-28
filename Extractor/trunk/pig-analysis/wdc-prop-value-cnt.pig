-- This Pig Latin script calculates several metrics for RDF quads on PLD level. For basic statistics use wdc-base.pig.
--
-- Number of different classes used by PLD
-- Number of different properties used by PLD
--
-- The script depends on custom Pig functions and a NQuads loader. 
-- That code is contained in the loddesc-core package (provided, hopefully).
-- 
-- Created: Robert Meusel <robert@informatik.uni-mannheim.de>, 2013-07-30
-- Tested on Pig version 0.9.2
--
-- Run as follows: 
-- pig -p code=/path/to/loddesc-core-0.1-with-dependencies.jar \\
--		-p input=/path/to/some/nquads/files/or/directories.nq   \\
-- 	   -p results=/directory/to/which/outputs/are/written      \\
--     /path/to/this/file/wdc-pld-base.pig

%default results './wdc-test-results';
%default input './wdc-example.nq';
%default code './loddesc-core-0.1-with-dependencies.jar'; -- our custom RDF loader

-- divider for output csv files
%default divider '	'; -- \t

-- register our UDFs and NQuads loader
register '$code';

-- number of reducers, should be - say - four times the number of reducer slots in the Hadoop cluster
SET default_parallel 14;

-- use gzip compression in result and intermediary files
SET mapred.output.compress true;
SET mapred.output.compression.codec org.apache.hadoop.io.compress.GzipCodec;
SET pig.tmpfilecompression true;
SET pig.tmpfilecompression.codec gz;

-- load data
quads = load '$input' using de.wbsg.loddesc.importer.QuadLoader() AS (subject:chararray,predicate:chararray,object:tuple(ntype:int,value:chararray,dtlang:chararray),graph:chararray);
props = FOREACH quads GENERATE predicate, object.value as value, de.wbsg.loddesc.functions.PayLevelDomain(graph) AS domain;
fprops = FILTER props BY predicate MATCHES '.*schema.org.*';

group_props_pld = GROUP fprops BY (domain, predicate, value);
cnt = FOREACH group_props_pld GENERATE group.domain, group.predicate, group.value, COUNT(fprops) as count;
ord = ORDER cnt BY domain, predicate, value;
STORE ord INTO '$results/propvalocc';