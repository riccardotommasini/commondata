-- script creates from an input of the format <PLD> <SD1> <SD>
-- a sublist for each aggregation level: <PLD> <SD1> <SD>
-- and aggregates and sorts each list
-- output will be <aggregation> <count>
--
-- by Robert (2013-07-16)

SET mapred.output.compress true;
SET mapred.output.compression.codec org.apache.hadoop.io.compress.GzipCodec;
SET pig.tmpfilecompression true;
SET pig.tmpfilecompression.codec gz;
SET mapred.task.timeout 900000;
SET default_parallel 14;

-- loading data
domainlist = LOAD '$input' USING PigStorage() as (pld:chararray,sd1:chararray,sd:chararray);

-- splitting data
pldlist = FOREACH domainlist GENERATE pld;
sdlist = FOREACH domainlist GENERATE sd;
sd1list = FOREACH domainlist GENERATE sd1;

--group
grouppld = GROUP pldlist BY pld;
groupsd = GROUP sdlist BY sd;
groupsd1 = GROUP sd1list BY sd1;

--count
pldcnt = FOREACH grouppld GENERATE group as pld, COUNT(pldlist);
sdcnt = FOREACH groupsd GENERATE group as sd, COUNT(sdlist);
sd1cnt = FOREACH groupsd1 GENERATE group as sd1, COUNT(sd1list);

--order
pld = ORDER pldcnt BY pld;
sd = ORDER sdcnt BY sd;
sd1 = ORDER sd1cnt BY sd1;

--save
STORE pld INTO '$results/pld' USING PigStorage();
STORE sd INTO '$results/sd' USING PigStorage();
STORE sd1 INTO '$results/sd1' USING PigStorage();

