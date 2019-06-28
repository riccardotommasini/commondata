-- This Pig Latin script calculates several co-occurences on RDF quads:
-- 
-- - Outgoing properties that occur in the same entity (same subject)
-- - Outgoing properties that occur in the same named graph / context / document
-- - Outgoing properties that occur on the same pay-level domain (e.g. example.com)
-- - RDF classes used in the same entity (same subject)
-- - RDF classes used in the same named graph / context / document
-- - RDF classes used on the same pay-level domain (e.g. example.com)
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
--     /path/to/this/file/wdc-cooc.pig

%default results './wdc-test-results';
%default code './loddesc-core-0.1-with-dependencies.jar'; -- our custom RDF loader
%default input './wdc-example-input.nq';

-- divider for output csv files
%default divider '	'; -- \t

-- number of reducers, should be - say - four times the number of reducer slots in the Hadoop cluster
SET default_parallel 80;

-- use gzip compression in result and intermediary files
SET mapred.output.compress true;
SET mapred.output.compression.codec org.apache.hadoop.io.compress.GzipCodec;
SET pig.tmpfilecompression true;
SET pig.tmpfilecompression.codec gz;

-- register our UDFs and NQuads loader
register '$code';


-- Use ‘true’ for QuadLoader to have subject tuples
quads = load '$input' using de.wbsg.loddesc.importer.QuadLoader('true') AS (subject,predicate,object:tuple(ntype:int,value,dtlang),graph);

-- generate some common relations
rdftypes = FILTER quads BY (predicate == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'); 

-- Outgoing properties cooc by #entities
outProps = FOREACH quads GENERATE subject As entity, predicate;
outPropsGroup = GROUP outProps BY entity;

propOutCoocEntities = FOREACH outPropsGroup {
	outPropsDistinct = DISTINCT outProps.predicate;
	GENERATE FLATTEN(outPropsDistinct) As p1, FLATTEN(outPropsDistinct) As p2;
}

propOutCoocEntitiesNeq = FILTER propOutCoocEntities BY p1 < p2;
propOutCoocEntitiesGroup = GROUP propOutCoocEntitiesNeq BY *;
propOutCoocEntitiesCounts = FOREACH propOutCoocEntitiesGroup GENERATE FLATTEN(group) AS (p1,p2), COUNT(propOutCoocEntitiesNeq) AS cnt;

propOutCoocEntitiesSorted = ORDER propOutCoocEntitiesCounts BY p1, cnt DESC;
propOutCoocEntitiesTop = FILTER propOutCoocEntitiesSorted BY cnt > 1;

STORE propOutCoocEntitiesTop INTO '$results/propOutCoocEntities' USING PigStorage('$divider');


-- Outgoing properties cooc by #urls
outPropsUrls = FOREACH quads GENERATE subject AS entity, predicate, graph;
outPropsUrlsGroup = GROUP outPropsUrls BY graph;

propOutCoocUrls = FOREACH outPropsUrlsGroup {
	outPropsDistinct = DISTINCT outPropsUrls.predicate;
	GENERATE FLATTEN(outPropsDistinct) As p1, FLATTEN(outPropsDistinct) As p2;
}

propOutCoocUrlsNeq = FILTER propOutCoocUrls BY p1 < p2;
propOutCoocUrlsGroup = GROUP propOutCoocUrlsNeq BY *;
propOutCoocUrlsCounts = FOREACH propOutCoocUrlsGroup GENERATE FLATTEN(group) AS (p1,p2), COUNT(propOutCoocUrlsNeq) AS cnt;

propOutCoocUrlsSorted = ORDER propOutCoocUrlsCounts BY p1, cnt DESC;
propOutCoocUrlsTop = FILTER propOutCoocUrlsSorted BY cnt > 1;

STORE propOutCoocUrlsTop INTO '$results/propOutCoocUrls' USING PigStorage('$divider');


-- Outgoing properties cooc by #plds
outPropsPlds = FOREACH quads GENERATE subject AS entity, predicate, de.wbsg.loddesc.functions.PayLevelDomain(graph) AS domain;
outPropsPldsGroup = GROUP outPropsPlds BY domain;

propOutCoocPlds = FOREACH outPropsPldsGroup {
	outPropsDistinct = DISTINCT outPropsPlds.predicate;
	GENERATE FLATTEN(outPropsDistinct) As p1, FLATTEN(outPropsDistinct) As p2;
}

propOutCoocPldsNeq = FILTER propOutCoocPlds BY p1 < p2;
propOutCoocPldsGroup = GROUP propOutCoocPldsNeq BY *;
propOutCoocPldsCounts = FOREACH propOutCoocPldsGroup GENERATE FLATTEN(group) AS (p1,p2), COUNT(propOutCoocPldsNeq) AS cnt;

propOutCoocPldsSorted = ORDER propOutCoocPldsCounts BY p1, cnt DESC;
propOutCoocPldsTop = FILTER propOutCoocPldsSorted BY cnt > 1;

STORE propOutCoocPldsTop INTO '$results/propOutCoocPlds' USING PigStorage('$divider');



types = FILTER quads BY (predicate == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'); 

-- classes cooc by #entities
classesEntities = FOREACH types GENERATE subject AS entity, object.value AS class;
classesEntitiesGroup = GROUP classesEntities BY entity;

classCoocEntities = FOREACH classesEntitiesGroup {
	classesDistinct = DISTINCT classesEntities.class;
	GENERATE FLATTEN(classesDistinct) AS c1, FLATTEN(classesDistinct) AS c2;
}

classCoocEntitiesNeq = FILTER classCoocEntities BY c1 < c2;
classCoocEntitiesGroup = GROUP classCoocEntitiesNeq BY *;
classCoocEntitiesCounts = FOREACH classCoocEntitiesGroup GENERATE FLATTEN(group) AS (c1,c2), COUNT(classCoocEntitiesNeq) AS cnt;

classCoocEntitiesSorted = ORDER classCoocEntitiesCounts BY c1, cnt DESC;
classCoocEntitiesTop = FILTER classCoocEntitiesSorted BY cnt > 1;

STORE classCoocEntitiesTop INTO '$results/classCoocEntities' USING PigStorage('$divider');

-- classes cooc by #urls
classesUrls = FOREACH types GENERATE graph AS url, object.value AS class;
classesUrlsGroup = GROUP classesUrls BY url;

classCoocUrls = FOREACH classesUrlsGroup {
	classesDistinct = DISTINCT classesUrls.class;
	GENERATE FLATTEN(classesDistinct) AS c1, FLATTEN(classesDistinct) AS c2;
}

classCoocUrlsNeq = FILTER classCoocUrls BY c1 < c2;
classCoocUrlsGroup = GROUP classCoocUrlsNeq BY *;
classCoocUrlsCounts = FOREACH classCoocUrlsGroup GENERATE FLATTEN(group) AS (c1,c2), COUNT(classCoocUrlsNeq) AS cnt;

classCoocUrlsSorted = ORDER classCoocUrlsCounts BY c1, cnt DESC;
classCoocUrlsTop = FILTER classCoocUrlsSorted BY cnt > 1;

STORE classCoocUrlsTop INTO '$results/classCoocUrls' USING PigStorage('$divider');


-- classes cooc by #plds
classesPlds = FOREACH types GENERATE de.wbsg.loddesc.functions.PayLevelDomain(graph) AS pld, object.value AS class;
classesPldsGroup = GROUP classesPlds BY pld;

classCoocPlds = FOREACH classesPldsGroup {
	classesDistinct = DISTINCT classesPlds.class;
	GENERATE FLATTEN(classesDistinct) AS c1, FLATTEN(classesDistinct) AS c2;
}

classCoocPldsNeq = FILTER classCoocPlds BY c1 < c2;
classCoocPldsGroup = GROUP classCoocPldsNeq BY *;
classCoocPldsCounts = FOREACH classCoocPldsGroup GENERATE FLATTEN(group) AS (c1,c2), COUNT(classCoocPldsNeq) AS cnt;

classCoocPldsSorted = ORDER classCoocPldsCounts BY c1, cnt DESC;
classCoocPldsTop = FILTER classCoocPldsSorted BY cnt > 1;

STORE classCoocPldsTop INTO '$results/classCoocPlds' USING PigStorage('$divider');

