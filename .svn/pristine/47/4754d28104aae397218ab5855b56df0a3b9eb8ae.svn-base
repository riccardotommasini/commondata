package org.webdatacommons.structureddata.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import de.dwslab.dwslib.collections.MapUtils;
import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.models.SortingOrderTypes;
import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;
import de.dwslab.dwslib.util.uri.DomainUtil;

/**
 * This class calculates the statistics which are necessary for the WDC
 * Structured Data release. The number of URLs, the number of Domains (PLDs) as
 * well as a list of Domains + URL count is created. The input for this class is
 * a set of files with an URL in each line.
 * Those statistics are for the whole CC corpus, not only for those files containing
 * structured data using Microdata, Microformats or RDFa.
 * 
 * @author Robert Meusel (robert@dwslab.de)
 *
 */
@Parameters(commandDescription = "Calculates the statistics from a list of URLs.")
public class CCUrlStatsCalculator extends Processor<File> {

	//TODO think about more useful stats for CC (e.g., TLD distribution)
	private HashMap<String, Integer> domainUrlCountMap = new HashMap<String, Integer>();
	private long urlCount = 0;

	@Parameter(names = { "-out",
			"-outputDir" }, required = true, description = "Folder where the outputfile(s) are written to.", converter = FileConverter.class)
	private File outputDirectory;

	@Parameter(names = { "-in",
			"-inputDir" }, required = true, description = "Folder where the input is read from.", converter = FileConverter.class)
	private File inputDirectory;

	@Parameter(names = "-threads", required = true, description = "Number of threads.")
	private Integer threads;

	@Override
	protected List<File> fillListToProcess() {
		List<File> files = new ArrayList<File>();
		for (File f : inputDirectory.listFiles()) {
			if (!f.isDirectory()) {
				files.add(f);
			}
		}
		return files;
	}

	@Override
	protected int getNumberOfThreads() {
		return threads;
	}

	@Override
	protected void afterProcess() {
		System.out.println("Writing output ...");
		File outputFile = new File(outputDirectory, "ccDomainUrl.stats.gz");
		try {
			BufferedWriter bw = OutputUtil.getGZIPBufferedWriter(outputFile);
			Map<String, Integer> sortedMap = MapUtils.sortByValue(domainUrlCountMap, SortingOrderTypes.DESCENDING);
			for (String domain : sortedMap.keySet()) {
				bw.write(domain + "\t" + sortedMap.get(domain) + "\n");
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("... done writing.");
		System.out.println("Found " + urlCount + " URLs within the files.");
	}

	@Override
	protected void process(File object) throws Exception {
		BufferedReader br = InputUtil.getBufferedReader(object);
		HashMap<String, Integer> domainUrlCountMap = new HashMap<String, Integer>();
		long urlCount = 0;
		while (br.ready()) {
			String line = br.readLine();
			if (line == null){
				continue;
			}
			String url = line.trim();
			urlCount++;
			String domain = DomainUtil.getPayLevelDomainFromWholeURL(url);
			// domain should never be null, as during the extraction this is
			// already handled.
			if (domain != null) {
				Integer cnt = domainUrlCountMap.get(domain);
				if (cnt == null) {
					cnt = 0;
				}
				cnt++;
				domainUrlCountMap.put(domain, cnt);
			}
		}
		br.close();
		// now we add the local stats to the global stats
		increaseUrlCount(urlCount);
		addDomainCounts(domainUrlCountMap);
	}

	/**
	 * Increases the URL Count by the given count. Makes sure that this is
	 * synchronized over all threads.
	 * 
	 * @param count
	 *            which should be added.
	 */
	private synchronized void increaseUrlCount(long count) {
		this.urlCount += count;
	}

	/**
	 * Adds the domains and counts from a thread to the global domain URL count.
	 * 
	 * @param domainUrlCountMap
	 *            the local/thread-based domain url counts
	 */
	private synchronized void addDomainCounts(HashMap<String, Integer> domainUrlCountMap) {
		for (String domain : domainUrlCountMap.keySet()) {
			Integer count = this.domainUrlCountMap.get(domain);
			if (count == null) {
				count = 0;
			}
			count += domainUrlCountMap.get(domain);
			this.domainUrlCountMap.put(domain, count);
		}
	}

	public static void main(String[] args) {
		CCUrlStatsCalculator cal = new CCUrlStatsCalculator();
		try {
			new JCommander(cal, args);
			cal.process();
		} catch (ParameterException pe) {
			pe.printStackTrace();
			new JCommander(cal).usage();
		}
	}

}
