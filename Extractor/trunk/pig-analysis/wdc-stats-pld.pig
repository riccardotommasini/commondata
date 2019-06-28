-- this scripts calculated the pld stats from the url stats file
--

%default results './wdc-test-results';
%default code './loddesc-core-0.1-with-dependencies.jar'; -- our custom RDF loader
%default input './wdc-example.nq';

-- divider for output csv files
%default divider '	'; -- \t

-- number of reducers, should be - say - four times the number of reducer slots in the Hadoop cluster
SET default_parallel 14;

-- use gzip compression in result and intermediary files
SET mapred.output.compress true;
SET mapred.output.compression.codec org.apache.hadoop.io.compress.GzipCodec;
SET pig.tmpfilecompression true;
SET pig.tmpfilecompression.codec gz;

-- register our UDFs and NQuads loader
register '$code';


-- load data
stats = LOAD '$input' USING PigStorage('$divider') as (uri:chararray,urlcnt:int,geo:int,hcal:int,hcard:int,hlisting:int,hrecipe:int,hresume:int,hreview:int,species:int,xfn:int,microdata:int,rdfa:int,sum:int);
-- convert into pld
pstats = FOREACH stats GENERATE de.wbsg.loddesc.functions.PayLevelDomain(uri) as pld,urlcnt,geo,hcal,hcard,hlisting,hrecipe,hresume,hreview,species,xfn,microdata,rdfa,sum;
-- group
pld_grouped = GROUP pstats BY pld;
-- aggregate stats
pld_stats = FOREACH pld_grouped GENERATE group as pld, SUM(pstats.urlcnt) as urlcnt, SUM(pstats.geo) as geo,SUM(pstats.hcal) as hcal,SUM(pstats.hcard) as hcard,SUM(pstats.hlisting) as hlisting,SUM(pstats.hrecipe) as hrecipe,SUM(pstats.hresume) as hresume,SUM(pstats.hreview) as hreview,SUM(pstats.species) as species,SUM(pstats.xfn) as xfn,SUM(pstats.microdata) as microdata,SUM(pstats.rdfa) as rdfa,SUM(pstats.sum) as sum;
-- store
STORE pld_stats INTO '$results/pldstats' USING PigStorage('$divider');

-- overall stats
all_group = GROUP pld_stats ALL;
all_stats = FOREACH all_group GENERATE group, COUNT(pld_stats) as pldcnt, SUM(pld_stats.geo) as geo,SUM(pld_stats.hcal) as hcal,SUM(pld_stats.hcard) as hcard,SUM(pld_stats.hlisting) as hlisting,SUM(pld_stats.hrecipe) as hrecipe,SUM(pld_stats.hresume) as hresume,SUM(pld_stats.hreview) as hreview,SUM(pld_stats.species) as species,SUM(pld_stats.xfn) as xfn,SUM(pld_stats.microdata) as microdata,SUM(pld_stats.rdfa) as rdfa,SUM(pld_stats.sum) as sum;
STORE all_stats INTO '$results/allstats' USING PigStorage('$divider');
