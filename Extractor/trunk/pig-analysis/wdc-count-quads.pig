-- This Pig Latin script calculates several metrics for RDF quads.
--
-- - Top RDF vocabularies by number of entities they occur in
-- - Top RDF vocabularies by number of graphs / contexts they occur on
-- - Top RDF vocabularies by number of pay-level-domains they occur on
-- - Top RDF vocabularies by number of pay-level domains they occur on by top-level domains
-- - Top RDF classes by number of entities using them
-- - Top RDF classes by number of graphs / contexts they occur on
-- - Top RDF classes by number of pay-level-domains they occur on
-- - Top RDF classes by number of pay-level domains they occur on by top-level domains
-- - Top RDF properties by number of entities using them
-- - Top RDF properties by number of graphs / contexts they occur on
-- - Top RDF properties by number of pay-level-domains they occur on
-- - Top RDF properties by number of pay-level domains they occur on by top-level domains
-- - Total amount of entities (distinct subjects)
-- - Total amount of graphs / contexts
-- - Total amount of pay-level domains
-- - Total amount of pay-level domains per top-level domain
--
-- The script depends on custom Pig functions and a NQuads loader. 
-- That code is contained in the loddesc-core package (provided, hopefully).
-- 
-- Created: Hannes Muehleisen <hannes@muehleisen.org>, 2012-06-28
-- Tested on Pig version 0.9.2
--
-- Run as follows: 
-- pig -p code=/path/to/loddesc-core-0.1-with-dependencies.jar \\
--     -p input=/path/to/some/nquads/files/or/directories.nq   \\
-- 	   -p results=/directory/to/which/outputs/are/written      \\
--     /path/to/this/file/wdc-base.pig

%default OUTPUT './wdc-test-results';
%default code './loddesc-core-0.1-with-dependencies.jar'; -- our custom RDF loader
%default INPUT './wdc-example.nq';

-- divider for output csv files
%default divider '	'; -- \t

-- number of reducers, should be - say - four times the number of reducer slots in the Hadoop cluster
SET default_parallel 12;

-- use gzip compression in result and intermediary files
SET mapred.output.compress true;
SET mapred.output.compression.codec org.apache.hadoop.io.compress.GzipCodec;
SET pig.tmpfilecompression true;
SET pig.tmpfilecompression.codec gz;

-- register our UDFs and NQuads loader
register '$code';


-- load data
quads = load '$INPUT' using de.wbsg.loddesc.importer.QuadLoader() AS (subject:chararray,predicate:chararray,object:tuple(ntype:int,value:chararray,dtlang:chararray),graph:chararray);

-- often-used set: all rdf:type statements
-- typeQuads = FILTER quads BY predicate == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type';
typeQuads = FILTER quads BY predicate MATCHES 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' OR ( predicate MATCHES '.*type' AND ( predicate MATCHES '.*ogp.*' OR predicate MATCHES '.*opengraph.*' ) );




--number of quads
quadGroup = GROUP quads ALL;
quadCount = FOREACH quadGroup GENERATE COUNT(quads);
STORE quadCount INTO '$OUTPUT/quadCount' USING PigStorage('$divider');

