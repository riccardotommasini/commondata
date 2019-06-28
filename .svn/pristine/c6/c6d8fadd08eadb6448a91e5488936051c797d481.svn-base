package org.webdatacommons.structureddata;

import org.webdatacommons.structureddata.stats.CCUrlStatsCalculator;
import org.webdatacommons.structureddata.stats.WDCQuadStatsCalculator;
import org.webdatacommons.structureddata.stats.WDCUrlStatsCalculator;

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
			}
		} catch (Exception pex) {
			pex.printStackTrace();
			if (jc.getParsedCommand() == null) {
				jc.usage();

			} else {
				switch (jc.getParsedCommand()) {
				case "urlstats":
					new JCommander(ccurls).usage();
					break;
				case "wdcurlstats":
					new JCommander(wdcurls).usage();
					break;
				case "wdcquadstats":
					new JCommander(wdcquads).usage();
					break;
				default:
					jc.usage();
				}
			}
		}

	}

}
