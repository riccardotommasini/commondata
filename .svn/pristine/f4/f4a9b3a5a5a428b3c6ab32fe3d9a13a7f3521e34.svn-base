-- This Pig Latin script calculates several metrics for RDF quads.
--
--
-- The script depends on custom Pig functions and a NQuads loader. 
-- That code is contained in the loddesc-core package (provided, hopefully).
-- 
-- Created: Petar Petrovski <petar@uni-mannheim.de>, 2012-06-28
-- Tested on Pig version 0.11.0
--
-- Run as follows: 
-- pig -p code=/path/to/loddesc-core-0.1-with-dependencies.jar \\
--     -p input=/path/to/some/nquads/files/or/directories.nq   \\
-- 	   -p results=/directory/to/which/outputs/are/written      \\
--     /path/to/this/file/wdc-base.pig

%default divider '	'; -- \t

%default INPUT 's3://aws-publicdatasets/common-crawl/crawl-data/CC-MAIN-2013-48/segments/*/wat';
%default OUTPUT 's3://cc2013extraction/urls';
%default code './archive-commons-jar-with-dependencies.jar';
 
SET pig.splitCombination 'false';
 
-- Load Internet Archive Pig utility jar:
REGISTER '$code';
 
-- alias short-hand for IA 'resolve()' UDF:
DEFINE resolve org.archive.hadoop.func.URLResolverFunc();
 
-- load data from SRC_SPEC:
urls = LOAD '$INPUT' USING
org.archive.hadoop.ArchiveJSONViewLoader('Envelope.WARC-Header-Metadata.WARC-Target-URI',
'Envelope.Payload-Metadata.HTTP-Response-Metadata.HTML-Metadata.Head.Base')
AS (page_url,html_base);

 
STORE urls INTO '$OUTPUT' USING PigStorage($divider);
