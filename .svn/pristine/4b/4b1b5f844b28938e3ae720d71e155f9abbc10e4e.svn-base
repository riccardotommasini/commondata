package org.webdatacommons.structureddata.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipException;

import org.webdatacommons.structureddata.util.DomainUtil;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.beust.jcommander.converters.FileConverter;

import au.com.bytecode.opencsv.CSVReader;
import de.dwslab.dwslib.collections.MapUtils;
import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.models.SortingOrderTypes;
import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;

/**
 * This class calculates all necessary stats for WDC based on the URLs
 * containing structured data e.g. Microdata, RDFa, or Microformats. The input
 * of this class are the stats files (CSV) generated during the extraction.
 * 
 * @author Robert Meusel (robert@dwslab.de)
 *
 */
@Parameters(commandDescription = "Calculates the statistics from stats files of the WDC extraction.")
public class WDCUrlStatsCalculator extends Processor<File> {

	@Parameter(names = { "-out",
			"-outputDir" }, required = true, description = "Folder where the outputfile(s) are written to.", converter = FileConverter.class)
	private File outputDirectory;

	@Parameter(names = { "-in",
			"-inputDir" }, required = true, description = "Folder where the input is read from.", converter = FileConverter.class)
	private File inputDirectory;

	@Parameter(names = "-threads", required = true, description = "Number of threads.")
	private Integer threads;

	@Parameter(names = { "-formatHeaderIds",
			"-fh" }, required = true, description = "Ids of the format headers (starting at 0, separated by \",\").", splitter = CommaParameterSplitter.class)
	private List<Integer> formatHeaderIds = new ArrayList<Integer>();

	@Parameter(names = { "-urlHeaderId", "-uh" }, required = true, description = "Id of the url (starting at 0).")
	private int urlHeaderId;

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

	// TODO combine both maps
	private HashMap<String, HashMap<String, Integer>> formatDomainUrlWTripleMap = new HashMap<String, HashMap<String, Integer>>();
	private HashMap<String, HashMap<String, Integer>> formatDomainTripleMap = new HashMap<String, HashMap<String, Integer>>();
	private HashMap<String, Integer> domainUrlMap = new HashMap<>();
	private int urlCount = 0;

	private synchronized void integrateDomainUrlResult(HashMap<String, Integer> domainUrlMap) {
		for (String domain : domainUrlMap.keySet()) {
			Integer c = this.domainUrlMap.get(domain);
			if (c == null) {
				c = 0;
			}
			c += domainUrlMap.get(domain);
			this.domainUrlMap.put(domain, c);
		}
	}

	private synchronized void integrateUrlWTripleResults(
			HashMap<String, HashMap<String, Integer>> formatDomainUrlWTripleMap) {
		for (String format : formatDomainUrlWTripleMap.keySet()) {
			if (!this.formatDomainUrlWTripleMap.containsKey(format)) {
				this.formatDomainUrlWTripleMap.put(format, new HashMap<String, Integer>());
			}
			for (String domain : formatDomainUrlWTripleMap.get(format).keySet()) {
				Integer count = this.formatDomainUrlWTripleMap.get(format).get(domain);
				if (count == null) {
					count = 0;
				}
				count += formatDomainUrlWTripleMap.get(format).get(domain);
				this.formatDomainUrlWTripleMap.get(format).put(domain, count);
			}
		}
	}

	private synchronized void integrateTripleResults(HashMap<String, HashMap<String, Integer>> formatDomainTripleMap) {
		for (String format : formatDomainTripleMap.keySet()) {
			if (!this.formatDomainTripleMap.containsKey(format)) {
				this.formatDomainTripleMap.put(format, new HashMap<String, Integer>());
			}
			for (String domain : formatDomainTripleMap.get(format).keySet()) {
				Integer count = this.formatDomainTripleMap.get(format).get(domain);
				if (count == null) {
					count = 0;
				}
				count += formatDomainTripleMap.get(format).get(domain);
				this.formatDomainTripleMap.get(format).put(domain, count);
			}
		}
	}

	// update triple count
	private synchronized void integrateUrlWTripleCount(int urlWTriple) {
		this.urlCount += urlWTriple;
	}

	@Override
	protected void process(File object) throws Exception {

		HashMap<String, HashMap<String, Integer>> formatDomainUrlWTripleMap = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, HashMap<String, Integer>> formatDomainTripleMap = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> domainUrlMap = new HashMap<>();
		int urlCount = 0;

		CSVReader reader = new CSVReader(new InputStreamReader(InputUtil.getInputStream(object)), ',');
		String[] nextLine;
		String[] header = null;
		try {
			while ((nextLine = reader.readNext()) != null) {
				try {
					if (header == null) {
						// set header
						header = nextLine;
						// init maps
						for (Integer id : formatHeaderIds) {
							formatDomainUrlWTripleMap.put(header[id], new HashMap<String, Integer>());
							formatDomainTripleMap.put(header[id], new HashMap<String, Integer>());
						}
					} else {
						String url = nextLine[urlHeaderId];
						String domain = DomainUtil.getPayLevelDomainFromWholeURL(url);
						if (domain == null) {
							continue;
						}
						boolean hasTriples = false;
						for (Integer id : formatHeaderIds) {
							// if we got some triples for this format
							
							if (nextLine[id] != null && Integer.parseInt(nextLine[id]) > 0) {
								hasTriples = true;
								Integer urlWTripleCount = formatDomainUrlWTripleMap.get(header[id]).get(domain);
								if (urlWTripleCount == null) {
									urlWTripleCount = 0;
								}
								urlWTripleCount++;
								formatDomainUrlWTripleMap.get(header[id]).put(domain, urlWTripleCount);

								Integer triples = formatDomainTripleMap.get(header[id]).get(domain);
								if (triples == null) {
									triples = 0;
								}
								triples += Integer.parseInt(nextLine[id]);
								formatDomainTripleMap.get(header[id]).put(domain, triples);
							}
						}
						if (hasTriples) {
							urlCount++;
							Integer c = domainUrlMap.get(domain);
							if (c == null) {
								c = 0;
							}
							c++;
							domainUrlMap.put(domain, c);
						}
					}
				} catch (ArrayIndexOutOfBoundsException ex) {
					// skip line
					continue;
				}
			}
		} catch (ZipException zex) {
			System.out.println("File broken, please fix: " + object.getName());
		}
		reader.close();

		// push collected data to global maps
		integrateTripleResults(formatDomainTripleMap);
		integrateUrlWTripleResults(formatDomainUrlWTripleMap);
		integrateUrlWTripleCount(urlCount);
		integrateDomainUrlResult(domainUrlMap);
	}

	@Override
	protected void afterProcess() {
		HashMap<String, Long> domainTripleMap = new HashMap<>();
		HashSet<String> domains = new HashSet<String>();
		try {
			BufferedWriter bwMatrix = OutputUtil
					.getGZIPBufferedWriter(new File(outputDirectory, "aggMatrixPerFormat.stats.gz"));
			bwMatrix.write("Format\tDomains\tURLs\tTriples\n");
			long numAllTriples = 0l;
			for (String format : this.formatDomainTripleMap.keySet()) {
				Integer numDomains = this.formatDomainTripleMap.get(format).size();
				Long numTriples = 0l;

				HashMap<String, Integer> domainTripleMapTmp = this.formatDomainTripleMap.get(format);
				domainTripleMapTmp = (HashMap<String, Integer>) MapUtils.sortByValue(domainTripleMapTmp,
						SortingOrderTypes.DESCENDING);
				BufferedWriter formatDomainTripleWriter = OutputUtil
						.getGZIPBufferedWriter(new File(outputDirectory, format + ".domaintriple.stats.gz"));
				for (String domain : domainTripleMapTmp.keySet()) {
					domains.add(domain);
					formatDomainTripleWriter.write(domain + "\t" + domainTripleMapTmp.get(domain) + "\n");
					Long triple = domainTripleMap.get(domain);
					if (triple == null) {
						triple = 0l;
					}
					triple += domainTripleMapTmp.get(domain);
					domainTripleMap.put(domain, triple);

					numTriples += this.formatDomainTripleMap.get(format).get(domain);
				}
				numAllTriples += numTriples;
				formatDomainTripleWriter.close();
				Integer numUrls = 0;

				BufferedWriter formatDomainUrlWTripleWriter = OutputUtil
						.getGZIPBufferedWriter(new File(outputDirectory, format + ".domainurlwtriple.stats.gz"));
				HashMap<String, Integer> domainUrlWTripleMap = this.formatDomainUrlWTripleMap.get(format);
				domainUrlWTripleMap = (HashMap<String, Integer>) MapUtils.sortByValue(domainUrlWTripleMap,
						SortingOrderTypes.DESCENDING);
				for (String domain : domainUrlWTripleMap.keySet()) {
					int urlCount = domainUrlWTripleMap.get(domain);
					numUrls += urlCount;
					formatDomainUrlWTripleWriter.write(domain + "\t" + urlCount + "\n");
				}
				formatDomainUrlWTripleWriter.close();
				bwMatrix.write(format + "\t" + numDomains + "\t" + numUrls + "\t" + numTriples + "\n");
			}
			bwMatrix.write("overall\t" + domains.size() + "\t" + urlCount + "\t" + numAllTriples + "\n");
			bwMatrix.close();

			domainTripleMap = (HashMap<String, Long>) MapUtils.sortByValue(domainTripleMap,
					SortingOrderTypes.DESCENDING);
			BufferedWriter domainTripleMapWriter = OutputUtil
					.getGZIPBufferedWriter(new File(outputDirectory, "domaintriple.stats.gz"));
			for (String domain : domainTripleMap.keySet()) {
				domainTripleMapWriter.write(domain + "\t" + domainTripleMap.get(domain) + "\n");
			}
			domainTripleMapWriter.close();

			domainUrlMap = (HashMap<String, Integer>) MapUtils.sortByValue(domainUrlMap, SortingOrderTypes.DESCENDING);
			BufferedWriter domainUrlMappWriter = OutputUtil
					.getGZIPBufferedWriter(new File(outputDirectory, "domainurlwtriple.stats.gz"));
			for (String domain : domainUrlMap.keySet()) {
				domainUrlMappWriter.write(domain + "\t" + domainUrlMap.get(domain) + "\n");
			}
			domainUrlMappWriter.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		WDCUrlStatsCalculator cal = new WDCUrlStatsCalculator();
		try {
			new JCommander(cal, args);
			cal.process();
		} catch (ParameterException pe) {
			pe.printStackTrace();
			new JCommander(cal).usage();
		}
	}

}
