package org.webdatacommons.structureddata.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.webdatacommons.structureddata.util.DomainUtil;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.beust.jcommander.converters.FileConverter;

import de.dwslab.dwslib.collections.MapUtils;
import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.models.SortingOrderTypes;
import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;
import de.wbsg.loddesc.util.VocabularyUtils;
import ldif.local.datasources.dump.QuadFileLoader;
import ldif.runtime.Quad;

/**
 * This class calculates, for a given set of quads the basic deployment
 * statistics, namely:
 * <ul>
 * <li>Vocabularies by Number of Entities they are used for</li>
 * <li>Vocabularies by Number of URLs they are used by</li>
 * <li>Vocabularies by Number of Domains they are used by</li>
 * <li>Classes by Number of Entities they are used for</li>
 * <li>Classes by Number of URLs they are used by</li>
 * <li>Classes by Number of Domains they are used by</li>
 * <li>Properties by Number of Entities they are used for</li>
 * <li>Properties by Number of URLs they are used by</li>
 * <li>Properties by Number of Domains they are used by</li>
 * <li>Total number of typed entities</li>
 * </ul>
 * In order to run this class correctly, all quads of one URL need to be
 * arranged within the file together. It is not needed that all quads of one
 * entity are together.
 * 
 * @author Robert Meusel (robert@dwslab.de)
 *
 */
@Parameters(commandDescription = "Calculates the statistics from quad files of the WDC extraction.")
public class WDCQuadStatsCalculator extends Processor<File> {

	@Parameter(names = { "-out",
			"-outputDir" }, required = true, description = "Folder where the outputfile(s) are written to.", converter = FileConverter.class)
	private File outputDirectory;

	@Parameter(names = { "-in",
			"-inputDir" }, required = true, description = "Folder where the input is read from.", converter = FileConverter.class)
	private File inputDirectory;

	@Parameter(names = "-threads", required = true, description = "Number of threads.")
	private Integer threads;

	@Parameter(names = { "-p",
			"-prefix" }, description = "Prefix of files in the input folder which will be processed.")
	private String filePrefix = "";

	@Parameter(names = { "-tp",
			"-typeProperties" }, required = true, description = "Properties which are used to identify a type (comma separated).", splitter = CommaParameterSplitter.class)
	private List<String> typeProperties = new ArrayList<String>();

	@Parameter(names = { "-e",
			"-typeAsRegex" }, required = false, description = "Indicates if the type properties should be handled as regex.")
	private boolean useRegex = false;

	@Override
	protected List<File> fillListToProcess() {
		List<File> files = new ArrayList<File>();
		for (File f : inputDirectory.listFiles()) {
			if (!f.isDirectory()) {
				if (filePrefix.length() > 0) {
					if (!f.getName().startsWith(filePrefix)) {
						continue;
					}
				}
				files.add(f);
			}
		}
		return files;
	}

	/**
	 * Temporary wrapper for statistics to reduce number of {@link HashMap}s in
	 * RAM.
	 * 
	 * @author Robert Meusel (robert@dwslab.de)
	 *
	 */
	private class StatHolder implements Comparable<StatHolder> {
		int numEntities;
		int numUrls;
		HashSet<String> domains = new HashSet<String>();

		@Override
		public int compareTo(StatHolder o) {
			return  this.domains.size() - o.domains.size();
		}
	}

	@Override
	protected int getNumberOfThreads() {
		return this.threads;
	}

	private HashMap<String, StatHolder> vocabStatsMap = new HashMap<>();
	private HashMap<String, StatHolder> classStatsMap = new HashMap<>();
	private HashMap<String, StatHolder> propStatsMap = new HashMap<>();
	private long errorCount = 0;
	private long parsedLines = 0;

	@Override
	protected void process(File object) throws Exception {
		// maintain thread-internal maps to reduce waiting time for other
		// threads
		HashMap<String, StatHolder> vocabStatsMap = new HashMap<>();
		HashMap<String, StatHolder> classStatsMap = new HashMap<>();
		HashMap<String, StatHolder> propStatsMap = new HashMap<>();
		int errorCount = 0;
		int lineCount = 0;

		// quadloader
		QuadFileLoader qfl = new QuadFileLoader();
		String currentURL = "";
		List<Quad> quads = new ArrayList<Quad>();
		// read the file
		BufferedReader br = InputUtil.getBufferedReader(object);
		while (br.ready()) {
			try {
				Quad q = qfl.parseQuadLine(br.readLine());
				lineCount++;
				// read all quads of one url (it is not necessary to pack them
				// all
				// into entities)
				if (q.graph().equals(currentURL)) {
					quads.add(q);
				} else {
					if (quads.size() > 0) {
						processQuadsOfURL(quads, currentURL, vocabStatsMap, classStatsMap, propStatsMap);
					}
					quads.clear();
					quads.add(q);
					currentURL = q.graph();
				}
			} catch (Exception e) {
				errorCount++;
				// TODO make this an option
				// e.printStackTrace();
			}
		}
		// process once more for the last quads
		if (quads.size() > 0) {
			processQuadsOfURL(quads, currentURL, vocabStatsMap, classStatsMap, propStatsMap);
		}
		br.close();

		// integrate the data
		integrateVocabs(vocabStatsMap);
		integrateClasses(classStatsMap);
		integrateProperties(propStatsMap);
		updateErrorCount(errorCount);
		updateLineCount(lineCount);
	}

	// process all quads of one URL and create the necessary aggregated stats.
	private void processQuadsOfURL(List<Quad> quads, String url, HashMap<String, StatHolder> vocabStatsMap,
			HashMap<String, StatHolder> classStatsMap, HashMap<String, StatHolder> propStatsMap) {
		String domain = DomainUtil.getPayLevelDomainFromWholeURL(url);
		if (domain == null) {
			// this should not happen
			return;
		}
		// save some space
		domain = domain.intern();

		// internal maps
		HashMap<String, HashSet<String>> vocabEntityMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> classEntityMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> propEntityMap = new HashMap<String, HashSet<String>>();

		// check all quads for one URL
		for (Quad q : quads) {
			// check if its a type quad
			String vocab;
			// if (typeProperties.contains(q.predicate())) {
			if (isType(q.predicate())) {
				// add class entities
				HashSet<String> entities = classEntityMap.get(q.value().value());
				if (entities == null) {
					classEntityMap.put(q.value().value(), new HashSet<String>());
				}
				classEntityMap.get(q.value().value()).add(q.subject().value());

				vocab = VocabularyUtils.getVocabularyUrl(q.value().value());
			} else {
				// add prop entities
				HashSet<String> entities = propEntityMap.get(q.predicate());
				if (entities == null) {
					propEntityMap.put(q.predicate(), new HashSet<String>());
				}
				propEntityMap.get(q.predicate()).add(q.subject().value());

				vocab = VocabularyUtils.getVocabularyUrl(q.predicate());
			}
			// add vocab entities
			HashSet<String> entities = vocabEntityMap.get(vocab);
			if (entities == null) {
				vocabEntityMap.put(vocab, new HashSet<String>());
			}
			vocabEntityMap.get(vocab).add(q.subject().value());
		}

		// summarize stats
		for (String vocab : vocabEntityMap.keySet()) {
			StatHolder stats = vocabStatsMap.get(vocab);
			if (stats == null) {
				stats = new StatHolder();
			}
			stats.numEntities += vocabEntityMap.get(vocab).size();
			stats.numUrls += 1;
			stats.domains.add(domain);
			vocabStatsMap.put(vocab, stats);
		}

		for (String c : classEntityMap.keySet()) {
			StatHolder stats = classStatsMap.get(c);
			if (stats == null) {
				stats = new StatHolder();
			}
			stats.numEntities += classEntityMap.get(c).size();
			stats.numUrls += 1;
			stats.domains.add(domain);
			classStatsMap.put(c, stats);
		}

		for (String prop : propEntityMap.keySet()) {
			StatHolder stats = propStatsMap.get(prop);
			if (stats == null) {
				stats = new StatHolder();
			}
			stats.numEntities += propEntityMap.get(prop).size();
			stats.numUrls += 1;
			stats.domains.add(domain);
			propStatsMap.put(prop, stats);
		}
	}

	/**
	 * Checks if the predicate is a type predicate
	 * 
	 * @param predicate
	 *            the predicate
	 * @return true if its a type predicate, false if not.
	 */
	private boolean isType(String predicate) {
		if (useRegex) {
			for (String pattern : typeProperties) {
				if (predicate.matches(pattern)) {
					return true;
				}
			}
		} else {
			if (typeProperties.contains(predicate)) {
				return true;
			}
		}
		return false;
	}

	// sync error count with global error count
	private synchronized void updateErrorCount(int errorCount) {
		this.errorCount += errorCount;
	}

	// sync line count with global line count
	private synchronized void updateLineCount(int lineCount) {
		this.parsedLines += lineCount;
	}

	// integrate vocab stats
	private synchronized void integrateVocabs(HashMap<String, StatHolder> vocabStatsMap) {
		for (String vocab : vocabStatsMap.keySet()) {
			StatHolder sh = this.vocabStatsMap.get(vocab);
			if (sh == null) {
				sh = vocabStatsMap.get(vocab);
			} else {
				sh.domains.addAll(vocabStatsMap.get(vocab).domains);
				sh.numEntities += vocabStatsMap.get(vocab).numEntities;
				sh.numUrls += vocabStatsMap.get(vocab).numUrls;
			}
			this.vocabStatsMap.put(vocab, sh);
		}
	}

	// integrate class stats
	private synchronized void integrateClasses(HashMap<String, StatHolder> classStatsMap) {
		for (String c : classStatsMap.keySet()) {
			StatHolder sh = this.classStatsMap.get(c);
			if (sh == null) {
				sh = classStatsMap.get(c);
			} else {
				sh.domains.addAll(classStatsMap.get(c).domains);
				sh.numEntities += classStatsMap.get(c).numEntities;
				sh.numUrls += classStatsMap.get(c).numUrls;
			}
			this.classStatsMap.put(c, sh);
		}
	}

	// integrate prop stats
	private synchronized void integrateProperties(HashMap<String, StatHolder> propStatsMap) {
		for (String prop : propStatsMap.keySet()) {
			StatHolder sh = this.propStatsMap.get(prop);
			if (sh == null) {
				sh = propStatsMap.get(prop);
			} else {
				sh.domains.addAll(propStatsMap.get(prop).domains);
				sh.numEntities += propStatsMap.get(prop).numEntities;
				sh.numUrls += propStatsMap.get(prop).numUrls;
			}
			this.propStatsMap.put(prop, sh);
		}
	}

	@Override
	protected void afterProcess() {
		// write the collected statistics to file
		try {
			// vocab stats
			BufferedWriter vocabWriter = OutputUtil.getGZIPBufferedWriter(new File(outputDirectory,
					(filePrefix.length() > 0 ? (filePrefix + ".") : ("")) + "vocab.stats.gz"));
			vocabWriter.write("vocab\tnumEntities\tnumUrls\tnumDomains\n");
			vocabStatsMap = (HashMap<String, StatHolder>) MapUtils.sortByValue(vocabStatsMap, SortingOrderTypes.DESCENDING);
			for (String vocab : vocabStatsMap.keySet()) {
				vocabWriter.write(vocab + "\t" + vocabStatsMap.get(vocab).numEntities + "\t"
						+ vocabStatsMap.get(vocab).numUrls + "\t" + vocabStatsMap.get(vocab).domains.size() + "\n");
			}
			vocabWriter.close();

			// class stats
			BufferedWriter classWriter = OutputUtil.getGZIPBufferedWriter(new File(outputDirectory,
					(filePrefix.length() > 0 ? (filePrefix + ".") : ("")) + "class.stats.gz"));
			long numTypedEntities = 0;
			classWriter.write("class\tnumEntities\tnumUrls\tnumDomains\n");
			classStatsMap = (HashMap<String, StatHolder>) MapUtils.sortByValue(classStatsMap, SortingOrderTypes.DESCENDING);
			for (String c : classStatsMap.keySet()) {
				classWriter.write(c + "\t" + classStatsMap.get(c).numEntities + "\t" + classStatsMap.get(c).numUrls
						+ "\t" + classStatsMap.get(c).domains.size() + "\n");
				numTypedEntities += classStatsMap.get(c).numEntities;
			}
			classWriter.close();

			// property stats
			BufferedWriter propWriter = OutputUtil.getGZIPBufferedWriter(
					new File(outputDirectory, (filePrefix.length() > 0 ? (filePrefix + ".") : ("")) + "prop.stats.gz"));
			propWriter.write("prop\tnumEntities\tnumUrls\tnumDomains\n");
			propStatsMap = (HashMap<String, StatHolder>) MapUtils.sortByValue(propStatsMap, SortingOrderTypes.DESCENDING);
			for (String c : propStatsMap.keySet()) {
				propWriter.write(c + "\t" + propStatsMap.get(c).numEntities + "\t" + propStatsMap.get(c).numUrls + "\t"
						+ propStatsMap.get(c).domains.size() + "\n");
			}
			propWriter.close();

			// domains per class
			BufferedWriter classDomainWriter = OutputUtil.getGZIPBufferedWriter(new File(outputDirectory,
					(filePrefix.length() > 0 ? (filePrefix + ".") : ("")) + "class.domains.gz"));
			for (String c : classStatsMap.keySet()) {
				classDomainWriter.write(c);
				for (String domain : classStatsMap.get(c).domains) {
					classDomainWriter.write("\t" + domain);
				}
				classDomainWriter.write("\n");
			}
			classDomainWriter.close();

			System.out.println("Parsed " + parsedLines + " lines.");
			System.out.println("Could not parse " + errorCount + " lines (quads).");
			System.out.println("Overall found: " + numTypedEntities + " typed entities in the data.");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		WDCQuadStatsCalculator cal = new WDCQuadStatsCalculator();
		try {
			new JCommander(cal, args);
			cal.process();
		} catch (ParameterException pe) {
			pe.printStackTrace();
			new JCommander(cal).usage();
		}
	}

}
