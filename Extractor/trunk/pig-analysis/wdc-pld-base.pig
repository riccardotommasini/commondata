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
--     -p input=/path/to/some/nquads/files/or/directories.nq   \\
-- 	   -p results=/directory/to/which/outputs/are/written      \\
--     /path/to/this/file/wdc-pld-base.pig

%default results './wdc-test-results';
%default code './loddesc-core-0.1-with-dependencies.jar'; -- our custom RDF loader
%default input './wdc-example.nq';

-- divider for output csv files
%default divider '	'; -- \t

-- number of reducers, should be - say - four times the number of reducer slots in the Hadoop cluster
SET default_parallel 28;

-- use gzip compression in result and intermediary files
SET mapred.output.compress true;
SET mapred.output.compression.codec org.apache.hadoop.io.compress.GzipCodec;
SET pig.tmpfilecompression true;
SET pig.tmpfilecompression.codec gz;

-- register our UDFs and NQuads loader
register '$code';


-- load data
quads = load '$input' using de.wbsg.loddesc.importer.QuadLoader() AS (subject:chararray,predicate:chararray,object:tuple(ntype:int,value:chararray,dtlang:chararray),graph:chararray);

-- often-used set: all rdf:type statements
typeQuads = FILTER quads BY predicate MATCHES 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' OR ( predicate MATCHES '.*type' AND ( predicate MATCHES '.*ogp.*' OR predicate MATCHES '.*opengraph.*' ) );
classes = FOREACH typeQuads GENERATE object.value as class, de.wbsg.loddesc.functions.PayLevelDomain(graph) AS domain;
dist_classes = DISTINCT classes;
group_classes = GROUP dist_classes BY domain;
count_classes = FOREACH group_classes GENERATE group, COUNT(dist_classes);
STORE count_classes INTO '$results/pldClassesCnt' USING PigStorage('$divider');

properties = FOREACH quads GENERATE predicate, de.wbsg.loddesc.functions.PayLevelDomain(graph) AS domain;
dist_props = DISTINCT properties;
group_props = GROUP dist_props BY domain;
count_props = FOREACH group_props GENERATE group, COUNT(dist_props);
STORE count_props INTO '$results/pldPropsCnt' USING PigStorage('$divider');