-- This Pig Latin script combines rdfa and other stats on url level
--
-- Run as follows:
-- pig -p code=/path/to/loddesc-core-0.1-with-dependencies.jar \\
--     -p input=/path/to/some/nquads/files/or/directories.nq   \\
--         -p results=/directory/to/which/outputs/are/written      \\
--     /path/to/this/file/wdc-base.pig

%default results './wdc-test-results';
%default input1 './wdc-example.nq';
%default input2 './wdc-example2.nq';
%default input3 './wdc-example3.nq';

-- divider for output csv files
%default divider '	'; -- \t

-- number of reducers, should be - say - four times the number of reducer slots in the Hadoop cluster
SET default_parallel 28;

-- use gzip compression in result and intermediary files
SET mapred.output.compress true;
SET mapred.output.compression.codec org.apache.hadoop.io.compress.GzipCodec;
SET pig.tmpfilecompression true;
SET pig.tmpfilecompression.codec gz;


-- load data
stats = load '$input1' using PigStorage('$divider') as (uri:chararray,geo:int,hcal:int,hcard:int,hlisting:int,hrecipe:int,hresume:int,hreview:int,species:int,xfn:int,microdata:int);
-- group possible doublicates
stats_group = GROUP stats BY uri;
stats_group_flat = FOREACH stats_group GENERATE group as uri, COUNT(stats) as groupcnt, SUM(stats.geo) as geo,SUM(stats.hcal) as hcal,SUM(stats.hcard) as hcard,SUM(stats.hlisting) as hlisting,SUM(stats.hrecipe) as hrecipe,SUM(stats.hresume) as hresume,SUM(stats.hreview) as hreview,SUM(stats.species) as species,SUM(stats.xfn) as xfn;
DESCRIBE stats_group_flat;
rstats = load '$input2' using PigStorage('\t') as (uri:chararray, rdfa:int);
rstats_group = GROUP rstats BY uri;
rstats_group_flat = FOREACH rstats_group GENERATE group as uri, SUM(rstats.rdfa) as rdfa;
DESCRIBE rstats_group_flat;
mdstats = load '$input3' using PigStorage('\t') as (uri:chararray, md:int);
mdstats_group = GROUP mdstats BY uri;
mdstats_group_flat = FOREACH mdstats_group GENERATE group as uri, SUM(mdstats.md) as md;
-- join data
comb1 = JOIN stats_group_flat BY uri LEFT OUTER, rstats_group_flat BY uri;
DESCRIBE comb1;
comb = JOIN comb1 BY stats_group_flat::uri LEFT OUTER, mdstats_group_flat BY uri;
DESCRIBE comb;
STORE comb INTO '$results/comb' USING PigStorage('$divider');
comb_clean = FOREACH comb GENERATE stats_group_flat::uri as uri, stats_group_flat::groupcnt as groupcnt, stats_group_flat::geo as geo, stats_group_flat::hcal as hcal, stats_group_flat::hcard as hcard, stats_group_flat::hlisting as hlisting, stats_group_flat::hrecipe as hrecipe, stats_group_flat::hresume as hresume, stats_group_flat::hreview as hreview, stats_group_flat::species as species, stats_group_flat::xfn as xfn, (mdstats_group_flat::uri is null ? 0 : mdstats_group_flat::md) as microdata, (rstats_group_flat::uri is null ? 0 : rstats_group_flat::rdfa) as rdfa;
comb_sum = FOREACH comb_clean GENERATE uri, groupcnt, geo, hcal, hcard, hlisting, hrecipe, hresume, hreview, species, xfn, microdata, rdfa ,(geo + hcal + hcard + hlisting + hrecipe + hresume + hreview + species + xfn + microdata + rdfa) as sum;
comb_filter = FILTER comb_sum BY sum > 0;
STORE comb_filter INTO '$results/combined' USING PigStorage('$divider');
-- overall stats
allgroup = GROUP comb_filter ALL;
allstats = FOREACH allgroup GENERATE group, SUM(comb_filter.groupcnt) as urlcount, SUM(comb_filter.geo) as geo,SUM(comb_filter.hcal) as hcal,SUM(comb_filter.hcard) as hcard,SUM(comb_filter.hlisting) as hlisting,SUM(comb_filter.hrecipe) as hrecipe,SUM(comb_filter.hresume) as hresume,SUM(comb_filter.hreview) as hreview,SUM(comb_filter.species) as species,SUM(comb_filter.xfn) as xfn,SUM(comb_filter.microdata) as microdata, SUM(comb_filter.rdfa) as rdfa, SUM(comb_filter.sum) as sum;
STORE allstats INTO '$results/triplestats' USING PigStorage('$divider');
