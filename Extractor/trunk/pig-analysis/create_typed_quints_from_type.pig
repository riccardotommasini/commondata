-- This PIG Latin script extracts all triple for a list of given type statements 
-- 
-- Created: Robert Meusel (robert@informatik.uni-mannheim.de) 2014-08-19
--
-- The script depends on custom a NQuads loader. 
-- That code is contained in the loddesc-core package (provided, hopefully).
-- 
-- Tested on Pig version 0.12.1
--
-- Run as follows: 
-- pig -p code=/path/to/loddesc-core-0.1-with-dependencies.jar \\
--     -p input=/path/to/some/nquads/files/or/directories.nq   \\
--	   -p list=/path/to/the/type/list/file					   \\
-- 	   -p results=/directory/to/which/outputs/are/written      \\
--     /path/to/this/file/create_typed_quints.pig

%default results './wdc-test-results';
%default code './loddesc-core-0.1-with-dependencies.jar'; -- our custom RDF loader
%default list './list';
%default input './wdc-example.nq';

-- divider for output csv files
%default divider '	'; -- \t

-- number of reducers, should be - say - four times the number of reducer slots in the Hadoop cluster
SET default_parallel 14;
SET job.priority VERY_LOW;

-- use gzip compression in result and intermediary files
SET mapred.output.compress true;
SET mapred.output.compression.codec org.apache.hadoop.io.compress.GzipCodec;
SET pig.tmpfilecompression true;
SET pig.tmpfilecompression.codec gz;

-- register our UDFs and NQuads loader
register '$code';

-- load data
quads = load '$input' using de.wbsg.loddesc.importer.QuadLoader() AS (subject:chararray,predicate:chararray,object:tuple(ntype:int,value:chararray,dtlang:chararray),graph:chararray);

-- get typedquads
typedquads = FILTER quads BY (predicate == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type');

-- get types from list
types = load '$list' using PigStorage('$divider') As (type:chararray);

-- filter typed quads with the list of types using replicated joins (as the list should be small)
filteredtypedquads = JOIN typedquads BY object.value, types BY type USING 'replicated';

-- get type subjects
typesubjects = FOREACH filteredtypedquads GENERATE subject as entity, type as type;
distincttypesubjects = DISTINCT typesubjects;

-- get only information from quads which belong to a type subjects
tmp = JOIN distincttypesubjects BY entity, quads by subject;
-- create the quint
cleantmp = FOREACH tmp GENERATE subject, predicate, TRIM((chararray)REPLACE((chararray)REPLACE((chararray)REPLACE((chararray)object.value, '\t', ' '), '\n', ' '),'\r',' ')) as objectvalue, graph, type;
-- sort the quint
final = ORDER cleantmp BY type, subject;
-- store
STORE final INTO '$results/quints' USING PigStorage('$divider');