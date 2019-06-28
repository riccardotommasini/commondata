package org.webdatacommons.structureddata;

import org.webdatacommons.structureddata.stats.CCUrlStatsCalculator;
import org.webdatacommons.structureddata.stats.WDCQuadStatsCalculator;
import org.webdatacommons.structureddata.stats.WDCSubsetStatsCalculator;
import org.webdatacommons.structureddata.stats.WDCSubsetStatsCleaner;
import org.webdatacommons.structureddata.stats.WDCUrlStatsCalculator;
import org.webdatacommons.structureddata.util.QuadSorter;
import org.webdatacommons.structureddata.util.SubsetCreator;

import com.beust.jcommander.JCommander;

public class Master {

	public static void main(String[] args) {
		// init
		Master master = new Master();
		JCommander jc = new JCommander(master);

		// add URL Stats
		CCUrlStatsCalculator ccurls = new CCUrlStatsCalculator();
		jc.addCommand("ccurlstats", ccurls);

		WDCUrlStatsCalculator wdcurls = new WDCUrlStatsCalculator();
		jc.addCommand("wdcurlstats", wdcurls);

		WDCQuadStatsCalculator wdcquads = new WDCQuadStatsCalculator();
		jc.addCommand("wdcquadstats", wdcquads);

		QuadSorter sort = new QuadSorter();
		jc.addCommand("sortquads", sort);

		SubsetCreator subset = new SubsetCreator();
		jc.addCommand("subset", subset);

		WDCSubsetStatsCalculator subsetStats = new WDCSubsetStatsCalculator();
		jc.addCommand("subsetstats", subsetStats);
		
		WDCSubsetStatsCleaner subsetClean = new WDCSubsetStatsCleaner();
		jc.addCommand("cleansubset", subsetClean);
		


		try {
			jc.parse(args);
			switch (jc.getParsedCommand()) {
			case "ccurlstats":
				ccurls.process();
				break;
			case "wdcurlstats":
				wdcurls.process();
				break;
			case "wdcquadstats":
				wdcquads.process();
				break;
			case "sortquads":
				sort.process();
				break;
			case "subset":
				subset.process();
				break;
			case "subsetstats":
				subsetStats.process();
				break;
			case "cleansubset":
				subsetClean.process();
				break;

			}
		} catch (Exception pex) {
			if (jc.getParsedCommand() == null) {
				jc.usage();
			} else {
				switch (jc.getParsedCommand()) {
				case "ccurlstats":
					new JCommander(ccurls).usage();
					break;
				case "wdcurlstats":
					new JCommander(wdcurls).usage();
					break;
				case "wdcquadstats":
					new JCommander(wdcquads).usage();
					break;
				case "sortquads":
					new JCommander(sort).usage();
					break;
				case "subset":
					new JCommander(subset).usage();
					break;
				case "cleansubset":
					new JCommander(subsetClean).usage();
					break;
				case "subsetstats":
					new JCommander(subsetStats).usage();
					break;

				default:
					jc.usage();
				}
			}
		}

	}

}
