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

%default results './wdc-test-results';
%default code './loddesc-core-0.1-with-dependencies.jar'; -- our custom RDF loader
%default input './wdc-example.nq';

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
quads = load '$input' using de.wbsg.loddesc.importer.QuadLoader() AS (subject:chararray,predicate:chararray,object:tuple(ntype:int,value:chararray,dtlang:chararray),graph:chararray);

-- often-used set: all rdf:type statements
typeQuads = FILTER quads BY predicate MATCHES 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' OR ( predicate MATCHES '.*type' AND ( predicate MATCHES '.*ogp.*' OR predicate MATCHES '.*opengraph.*' ) );
typedSubjects = FOREACH typeQuads GENERATE subject;
distinctTypedSubjects = DISTINCT typedSubjects;

-- all subjects distinct
allSubjects = FOREACH quads GENERATE subject;
distinctAllSubjects = DISTINCT allSubjects;

-- find quads whose subjects have no type
subjectQuads = GROUP quads BY subject; -- {group:subject, quads:{...}}

grp = COGROUP distinctAllSubjects BY subject, distinctTypedSubjects BY subject;
filteredGrp = FILTER grp by IsEmpty(distinctTypedSubjects);
-- grp = {group:subject,subjectquads:{{group:subject, quads:{...}}}, distinctTypedSubjects:{}}
--DESCRIBE filteredGrp;
--untypedQuads = FOREACH filteredGrp GENERATE FLATTEN(subjectQuads);
--DESCRIBE untypedQuads;
--untypedQuads = FOREACH untypedQuads GENERATE FLATTEN(quads);
--DESCRIBE untypedQuads;

untypedSubjects = FOREACH filteredGrp GENERATE group as subject;
--DESCRIBE untypedSubjects;
untypedQuads = JOIN untypedSubjects BY subject, quads BY subject;
untypedQuads = FOREACH untypedQuads GENERATE quads::subject as subject,predicate as predicate,object as object,graph as graph;

DESCRIBE untypedQuads; 

-- Top Properties

--Properties by #Entities
propEntitiesAll = FOREACH untypedQuads GENERATE predicate, subject;
propEntities = DISTINCT propEntitiesAll;
propEntitiesGroup = GROUP propEntities BY predicate;
propEntitiesCounts = FOREACH propEntitiesGroup GENERATE group AS predicate, COUNT(propEntities) AS cnt;
--topPropertiesEntities = ORDER propEntitiesCounts BY cnt DESC;
--topPropertiesEntitiesSorted = FILTER topPropertiesEntities BY cnt > 1;
STORE propEntitiesCounts INTO '$results/propertiesEntities' USING PigStorage('$divider');


--Properties by #URLs
propUrlsAll = FOREACH untypedQuads GENERATE predicate, graph;
propUrls = DISTINCT propUrlsAll;
propUrlsGroups = GROUP propUrls BY predicate;
propUrlsCounts = FOREACH propUrlsGroups GENERATE group AS predicate, COUNT(propUrls) AS cnt;
--topPropertiesUrls = ORDER propUrlsCounts BY cnt DESC;
--topPropertiesUrlsSorted = FILTER topPropertiesUrls BY cnt > 1;
STORE propUrlsCounts INTO '$results/propertiesUrls' USING PigStorage('$divider');


-- Properties by #PLD
propPldsAll = FOREACH untypedQuads GENERATE predicate,de.wbsg.loddesc.functions.PayLevelDomain(graph) AS tld;
propPlds = DISTINCT propPldsAll;
propPldsGroups = GROUP propPlds BY predicate;
propPldsCounts = FOREACH propPldsGroups GENERATE group AS predicate, COUNT(propPlds) AS cnt;
STORE propPldsCounts INTO '$results/propertiesPlds' USING PigStorage('$divider');

--Properties by TLD
propTlds = GROUP propPlds BY (de.wbsg.loddesc.functions.TopLevelDomain(tld),predicate);

propTldsCounts = FOREACH propTlds GENERATE FLATTEN(group) AS (tld,predicate), COUNT(propPlds) AS cnt;
topPropertiesTlds = ORDER propTldsCounts BY tld, cnt DESC;
STORE topPropertiesTlds INTO '$results/propertiesTlds' USING PigStorage('$divider');


-- number entities
subjects = FOREACH untypedQuads GENERATE subject;
subjectsDistinct = DISTINCT subjects;
subjectsGroup = GROUP subjectsDistinct ALL;
subjectsCount = FOREACH subjectsGroup GENERATE COUNT(subjectsDistinct);
STORE subjectsCount INTO '$results/subjectsCount' USING PigStorage('$divider');

-- number urls
urls = FOREACH untypedQuads GENERATE graph AS url;
urlsDistinct = DISTINCT urls;
urlsGroup = GROUP urlsDistinct ALL;
urlsCount = FOREACH urlsGroup GENERATE COUNT(urlsDistinct);
STORE urlsCount INTO '$results/urlsCount' USING PigStorage('$divider');

-- number plds
plds = FOREACH untypedQuads GENERATE de.wbsg.loddesc.functions.PayLevelDomain(graph) AS pld;
pldsDistinct = DISTINCT plds;
pldsGroup = GROUP pldsDistinct ALL;
pldsCount = FOREACH pldsGroup GENERATE COUNT(pldsDistinct);
STORE pldsCount INTO '$results/pldsCount' USING PigStorage('$divider');

-- number plds per tld
tldPlds = FOREACH untypedQuads GENERATE de.wbsg.loddesc.functions.TopLevelDomain(graph) AS tld, de.wbsg.loddesc.functions.PayLevelDomain(graph) AS pld;
tldPldsDistinct = DISTINCT tldPlds;
tldPldsGroup = GROUP tldPldsDistinct BY tld;
tldPldsCount = FOREACH tldPldsGroup GENERATE group AS tld, COUNT(tldPldsDistinct) as tlds;
STORE tldPldsCount INTO '$results/tldPldsCount' USING PigStorage('$divider');

-- control
untypedTypeQuads = FILTER untypedQuads BY predicate MATCHES 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' OR ( predicate MATCHES '.*type' AND ( predicate MATCHES '.*ogp.*' OR predicate MATCHES '.*opengraph.*' ) );
untypedTypeQuadsGroup = GROUP untypedTypeQuads ALL;
untypedTypeQuadsGroupCount = FOREACH untypedTypeQuadsGroup GENERATE COUNT(untypedTypeQuads);
STORE untypedTypeQuadsGroupCount INTO '$results/checkCount' USING PigStorage('$divider');
