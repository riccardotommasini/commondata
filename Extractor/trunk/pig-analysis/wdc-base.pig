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
-- typeQuads = FILTER quads BY predicate == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type';
typeQuads = FILTER quads BY predicate MATCHES 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' OR ( predicate MATCHES '.*type' AND ( predicate MATCHES '.*ogp.*' OR predicate MATCHES '.*opengraph.*' ) );

-- Top Vocabularies

--Vocabularies by Entities
vocabPredsSubjects = FOREACH quads GENERATE de.wbsg.loddesc.functions.Vocab(predicate) AS vocab, subject;
vocabClassesSubjects = FOREACH typeQuads GENERATE de.wbsg.loddesc.functions.Vocab(object.value) AS vocab, subject;
allVocabsEntities = UNION vocabPredsSubjects, vocabClassesSubjects;
vocabEntities = DISTINCT allVocabsEntities;
vocabSubjectGroup = GROUP vocabEntities BY vocab;
vocabSubjectCount = FOREACH vocabSubjectGroup GENERATE group, COUNT(vocabEntities) AS cnt;
--topVocabsEntities = ORDER vocabSubjectCount BY cnt DESC;
STORE vocabSubjectCount INTO '$results/vocabEntities' USING PigStorage('$divider');


--Vocabularies by URLs
vocabPredsUrls = FOREACH quads GENERATE de.wbsg.loddesc.functions.Vocab(predicate) AS vocab, graph;
vocabsClassesUrls = FOREACH typeQuads GENERATE de.wbsg.loddesc.functions.Vocab(object.value) AS vocab, graph;
allVocabsUrls = UNION vocabPredsUrls, vocabsClassesUrls;
vocabUrls = DISTINCT allVocabsUrls;
vocabUrlsGroups = GROUP vocabUrls BY vocab;
vocabUrlsCounts = FOREACH vocabUrlsGroups GENERATE group, COUNT(vocabUrls) AS cnt;
--topVocabsUrls = ORDER vocabUrlsCounts BY cnt DESC;
STORE vocabUrlsCounts INTO '$results/vocabUrls' USING PigStorage('$divider');


--Vocabularies by #PLD
vocabPredsPlds = FOREACH quads GENERATE de.wbsg.loddesc.functions.Vocab(predicate) AS vocab, de.wbsg.loddesc.functions.PayLevelDomain(graph) AS pld;
vocabClassesPlds = FOREACH typeQuads GENERATE de.wbsg.loddesc.functions.Vocab(object.value) AS vocab, de.wbsg.loddesc.functions.PayLevelDomain(graph) as pld;
allVocabsPlds = UNION vocabPredsPlds, vocabClassesPlds;
vocabsPlds = DISTINCT allVocabsPlds;
vocabPldsGroups = GROUP vocabsPlds BY vocab;
vocabPldsCounts = FOREACH vocabPldsGroups GENERATE group, COUNT(vocabsPlds) AS cnt;
--topVocabsPlds = ORDER vocabPldsCounts BY cnt DESC;
STORE vocabPldsCounts INTO '$results/vocabPlds' USING PigStorage('$divider');




-- Vocabularies by TLD
vocabTldGroups = GROUP vocabsPlds BY (de.wbsg.loddesc.functions.TopLevelDomain(pld),vocab);
vocabTldCounts = FOREACH vocabTldGroups GENERATE FLATTEN(group) AS (tld,vocab), COUNT(vocabsPlds) AS cnt;
--topVocabTlds = ORDER vocabTldCounts BY tld, cnt DESC;
STORE vocabTldCounts INTO '$results/vocabTlds' USING PigStorage('$divider');


-- Top Classes

-- Top Types/Classes by Entities
classEntitiesGroups = GROUP typeQuads BY object.value;
classEntitiesCounts = FOREACH classEntitiesGroups GENERATE group, COUNT(typeQuads) AS cnt;
--topClassesEntities = ORDER classEntitiesCounts BY cnt DESC;
STORE classEntitiesCounts INTO '$results/classesEntities' USING PigStorage('$divider');


-- Types by URLs
classUrlsAll = FOREACH typeQuads GENERATE object.value AS class, graph;
classUrls = DISTINCT classUrlsAll;
classUrlsGroups = GROUP classUrls BY class;
classUrlCounts = FOREACH classUrlsGroups GENERATE group, COUNT(classUrls) AS cnt;
--topClassesUrls = ORDER classUrlCounts BY cnt DESC;
STORE classUrlCounts INTO '$results/classesUrls' USING PigStorage('$divider');


-- Types by #PLD
classPldsAll = FOREACH typeQuads GENERATE object.value AS class,de.wbsg.loddesc.functions.PayLevelDomain(graph) AS domain;
classPlds = DISTINCT classPldsAll;
classPldsGroups = GROUP classPlds by class;
classPldsCounts = FOREACH classPldsGroups GENERATE group, COUNT(classPlds) AS cnt;
--topClassesPlds = ORDER classPldsCounts BY cnt DESC;
STORE classPldsCounts INTO '$results/classesPlds' USING PigStorage('$divider');


--Top Types by TLD
classTldsGroups = GROUP classPlds BY (de.wbsg.loddesc.functions.TopLevelDomain(domain),class);
classTldCounts = FOREACH classTldsGroups GENERATE FLATTEN(group) AS (tld,class), COUNT(classPlds) AS cnt;
--topClassesTld = ORDER classTldCounts BY tld, cnt DESC;
STORE classTldCounts INTO '$results/classesTlds' USING PigStorage('$divider');


-- Top Properties


--Properties by #Entities
propEntitiesAll = FOREACH quads GENERATE predicate, subject;
propEntities = DISTINCT propEntitiesAll;
propEntitiesGroup = GROUP propEntities BY predicate;
propEntitiesCounts = FOREACH propEntitiesGroup GENERATE group AS predicate, COUNT(propEntities) AS cnt;
--topPropertiesEntities = ORDER propEntitiesCounts BY cnt DESC;
--topPropertiesEntitiesSorted = FILTER topPropertiesEntities BY cnt > 1;
STORE propEntitiesCounts INTO '$results/propertiesEntities' USING PigStorage('$divider');


--Properties by #URLs
propUrlsAll = FOREACH quads GENERATE predicate, graph;
propUrls = DISTINCT propUrlsAll;
propUrlsGroups = GROUP propUrls BY predicate;
propUrlsCounts = FOREACH propUrlsGroups GENERATE group AS predicate, COUNT(propUrls) AS cnt;
--topPropertiesUrls = ORDER propUrlsCounts BY cnt DESC;
--topPropertiesUrlsSorted = FILTER topPropertiesUrls BY cnt > 1;
STORE propUrlsCounts INTO '$results/propertiesUrls' USING PigStorage('$divider');


-- Properties by #PLD
propPldsAll = FOREACH quads GENERATE predicate,de.wbsg.loddesc.functions.PayLevelDomain(graph) AS tld;
propPlds = DISTINCT propPldsAll;

propPldsGroups = GROUP propPlds BY predicate;
propPldsCounts = FOREACH propPldsGroups GENERATE group AS predicate, COUNT(propPlds) AS cnt;

--topPropertiesPlds = FILTER propPldsCounts BY cnt > 1;
--topPropertiesPldsSorted = ORDER topPropertiesPlds BY cnt DESC;
STORE propPldsCounts INTO '$results/propertiesPlds' USING PigStorage('$divider');


--propPldsCounts,propPldsGroups,propTlds,propTldsCounts,topPropertiesPldsSorted

--Properties by TLD
propTlds = GROUP propPlds BY (de.wbsg.loddesc.functions.TopLevelDomain(tld),predicate);

propTldsCounts = FOREACH propTlds GENERATE FLATTEN(group) AS (tld,predicate), COUNT(propPlds) AS cnt;
topPropertiesTlds = ORDER propTldsCounts BY tld, cnt DESC;
STORE topPropertiesTlds INTO '$results/propertiesTlds' USING PigStorage('$divider');


--number of quads
quadGroup = GROUP quads ALL;
quadCount = FOREACH quadGroup GENERATE COUNT(quads);
STORE quadCount INTO '$results/quadCount' USING PigStorage('$divider');


-- number entities
subjects = FOREACH quads GENERATE subject;
subjectsDistinct = DISTINCT subjects;
subjectsGroup = GROUP subjectsDistinct ALL;
subjectsCount = FOREACH subjectsGroup GENERATE COUNT(subjectsDistinct);
STORE subjectsCount INTO '$results/subjectsCount' USING PigStorage('$divider');

-- number urls
urls = FOREACH quads GENERATE graph AS url;
urlsDistinct = DISTINCT urls;
urlsGroup = GROUP urlsDistinct ALL;
urlsCount = FOREACH urlsGroup GENERATE COUNT(urlsDistinct);
STORE urlsCount INTO '$results/urlsCount' USING PigStorage('$divider');

-- number plds
plds = FOREACH quads GENERATE de.wbsg.loddesc.functions.PayLevelDomain(graph) AS pld;
pldsDistinct = DISTINCT plds;
pldsGroup = GROUP pldsDistinct ALL;
pldsCount = FOREACH pldsGroup GENERATE COUNT(pldsDistinct);
STORE pldsCount INTO '$results/pldsCount' USING PigStorage('$divider');

-- number plds per tld
tldPlds = FOREACH quads GENERATE de.wbsg.loddesc.functions.TopLevelDomain(graph) AS tld, de.wbsg.loddesc.functions.PayLevelDomain(graph) AS pld;
tldPldsDistinct = DISTINCT tldPlds;
tldPldsGroup = GROUP tldPldsDistinct BY tld;
tldPldsCount = FOREACH tldPldsGroup GENERATE group AS tld, COUNT(tldPldsDistinct) as tlds;
STORE tldPldsCount INTO '$results/tldPldsCount' USING PigStorage('$divider');

