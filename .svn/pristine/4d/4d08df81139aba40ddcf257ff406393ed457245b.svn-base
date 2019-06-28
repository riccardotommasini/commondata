-- This PIG Latin script extracts data specific for schema:JobPostings
----
-- The script depends on custom Pig functions and a NQuads loader. 
-- That code is contained in the loddesc-core package (provided, hopefully).
-- 
-- Created: Robert Meusel (robert@informatik.uni-mannheim.de) 25-Feb-2013
-- Tested on Pig version 0.10.0
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
DESCRIBE quads;

-- get rdf:type statements for products
typeJobPostingQuads = FILTER quads BY (predicate == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type') AND (object.value == 'http://schema.org/JobPosting');

-- get JobPosting subjects
jobPostingSubjects = FOREACH typeJobPostingQuads GENERATE subject as entity;
destinctSubjects = DISTINCT jobPostingSubjects;

-- get only information from quads which belong to a job posting
jobPostingQuadsTmp = JOIN destinctSubjects BY entity, quads by subject;
DESCRIBE jobPostingQuadsTmp;
jobPostingQuads = FOREACH jobPostingQuadsTmp GENERATE subject, predicate, TRIM((chararray)REPLACE((chararray)REPLACE((chararray)REPLACE((chararray)object.value, '\t', ' '), '\n', ' '),'\r',' ')) as objectvalue, graph;

-- get job title
jobTitleQuads = FILTER jobPostingQuads BY predicate == 'http://schema.org/JobPosting/title';
jobTitles = FOREACH jobTitleQuads GENERATE subject as s1, objectvalue as jobtitle, graph;
distinctJobTitles = DISTINCT jobTitles;
STORE distinctJobTitles INTO '$results/distinctJobTitles' USING PigStorage('$divider');

-- get dc title (might be empty)
dcTitleQuads = FILTER jobPostingQuads BY predicate == 'http://purl.org/dc/terms/title';
dcTitles = FOREACH dcTitleQuads GENERATE subject as s2, objectvalue as dctitle;
distinctDcTitles = DISTINCT dcTitles;
STORE distinctDcTitles INTO '$results/distinctDcTitles' USING PigStorage('$divider');

-- get location
jobLocationQuads = FILTER jobPostingQuads BY predicate == 'http://schema.org/JobPosting/jobLocation';
jobLocation = FOREACH jobLocationQuads GENERATE subject as s3, objectvalue as joblocation;
distinctJobLocation = DISTINCT jobLocation;
STORE distinctJobLocation INTO '$results/distinctJobLocation' USING PigStorage('$divider');

-- get description
jobDescriptionQuads = FILTER jobPostingQuads BY predicate == 'http://schema.org/JobPosting/description';
jobDescription = FOREACH jobDescriptionQuads GENERATE subject as s4, objectvalue as jobdescription;
distinctJobDescription = DISTINCT jobDescription;
STORE distinctJobDescription INTO '$results/distinctJobDescription' USING PigStorage('$divider');

-- get posting date
jobDateQuads = FILTER jobPostingQuads BY predicate == 'http://schema.org/JobPosting/datePosted';
jobDate = FOREACH jobDateQuads GENERATE subject as s5, objectvalue as jobdate;
distinctJobDate = DISTINCT jobDate;
STORE distinctJobDate INTO '$results/distinctJobDate' USING PigStorage('$divider');

-- get hiring orga
organizationQuads = FILTER jobPostingQuads BY predicate == 'http://schema.org/JobPosting/hiringOrganization';
organization = FOREACH organizationQuads GENERATE subject as s6, objectvalue as organization;
distinctOrganization = DISTINCT organization;
STORE distinctOrganization INTO '$results/distinctOrganization' USING PigStorage('$divider');

-- get employment type
employmentTypeQuads = FILTER jobPostingQuads BY predicate == 'http://schema.org/JobPosting/employmentType';
employmentType = FOREACH employmentTypeQuads GENERATE subject as s7, objectvalue as employmentType;
distinctEmploymentType = DISTINCT employmentType;
STORE distinctEmploymentType INTO '$results/distinctEmploymentType' USING PigStorage('$divider');

-- get industry
industryQuads = FILTER jobPostingQuads BY predicate == 'http://schema.org/JobPosting/industry';
industry = FOREACH industryQuads GENERATE subject as s8, objectvalue as industry;
distinctIndustry = DISTINCT industry;
STORE distinctIndustry INTO '$results/distinctIndustry' USING PigStorage('$divider');

-- get orga name (might be empty)
orgaNameQuads = FILTER jobPostingQuads BY predicate == 'http://schema.org/Organization/name';
orgaName = FOREACH orgaNameQuads GENERATE subject as s9, objectvalue as orgaName;
distinctOrgaName = DISTINCT orgaName;
STORE distinctOrgaName INTO '$results/distinctOrgaName' USING PigStorage('$divider');

-- get place address (mitght be empty)
placeAddressQuads = FILTER jobPostingQuads BY predicate == 'http://schema.org/Place/address';
placeAddress = FOREACH placeAddressQuads GENERATE subject as s10, objectvalue as placeAddress;
distinctPlaceAddress = DISTINCT placeAddress;
STORE distinctPlaceAddress INTO '$results/distinctPlaceAddress' USING PigStorage('$divider');



-- join all (may cause duplicates)
join1 = JOIN destinctSubjects BY entity LEFT, distinctJobTitles by s1;
join2 = JOIN join1 BY entity LEFT, distinctJobLocation by s3;
join3 = JOIN join2 BY entity LEFT, distinctJobDescription by s4;
join4 = JOIN join3 BY entity LEFT, distinctJobDate by s5;
join5 = JOIN join4 BY entity LEFT, distinctOrganization by s6;
join6 = JOIN join5 BY entity LEFT, distinctEmploymentType by s7;
join7 = JOIN join6 BY entity LEFT, distinctIndustry by s8;
join8 = JOIN join7 BY entity LEFT, distinctOrgaName by s9;
join9 = JOIN join8 BY entity LEFT, distinctPlaceAddress by s10;

allJobInformation = FOREACH join9 GENERATE entity as subject, jobtitle, joblocation, jobdescription, jobdate, organization, employmentType, industry, orgaName, placeAddress, graph;
distinctAllJobInformation = DISTINCT allJobInformation;
STORE distinctAllJobInformation INTO '$results/distinctAllJobInformation' USING PigStorage('$divider');