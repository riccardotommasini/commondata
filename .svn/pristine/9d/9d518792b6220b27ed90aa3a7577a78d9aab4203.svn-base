package org.webdatacommons.structureddata.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

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
import ldif.local.datasources.dump.QuadFileLoader;
import ldif.runtime.Quad;

/**
 * This class calculates, for a given set of quads concerning a class subset the required deployment
 * statistics for the WDC website, namely:
 * <ul>
 * <li>Related Classes by Number of Entities they are used for</li>
 * <li>Total number of nquads</li>
 * <li>Total number of typed entities</li>
 * <li>Total number of URLs</li>
 * <li>Total number of Domains (Hosts)</li>
 * </ul>
 * The required input is a set of zipped files located in one directory and the created output is one folder per file which includes the 
 * created stats and a txt file containing the relevant html script for the wdc website 
 * @author Anna Primpeli
 *
 */
@Parameters(commandDescription = "Calculates the statistics from class specific quad files of the WDC extraction.")
public class WDCSubsetStatsCalculator extends Processor<File> {

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
	
	private PrintWriter htmlScript;

	@Override
	protected List<File> fillListToProcess() {
		List<File> files = new ArrayList<File>();
		System.out.println("List files from:"+inputDirectory.getPath());
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
		int numQuads=0;
		HashSet<String> domains = new HashSet<String>();
		HashSet<String> urls = new HashSet<String>();
		
		@Override
		public int compareTo(StatHolder o) {
			return  this.domains.size() - o.domains.size();
		}
	}

	@Override
	protected int getNumberOfThreads() {
		return this.threads;
	}


	@Override
	protected void process(File object) throws Exception {
		htmlScript.println("Processing:"+object.getName());
		htmlScript.flush();
		String currentSubsetName = "http://schema.org/"+object.getName().replaceAll("schema_", "").replaceAll(".gz", "");
		if(object.getName().contains("part")) currentSubsetName = "http://schema.org/Product";

		HashMap<String, StatHolder> classStatsMap = new HashMap<>();
		int errorCount = 0;

		// quadloader
		QuadFileLoader qfl = new QuadFileLoader();
		String currentURL = "";
		List<Quad> quads = new ArrayList<Quad>();
		// read the file
		BufferedReader br = InputUtil.getBufferedReader(object);
		int quadsCount=0;
		while (br.ready()) {
			try {
				Quad q = qfl.parseQuadLine(br.readLine());
				quadsCount++;
				// read all quads of one url (it is not necessary to pack them
				// all
				// into entities)
				if (q.graph().equals(currentURL)) {
					quads.add(q);
				} else {
					if (quads.size() > 0) {
						processQuadsOfURL(quads, currentURL, classStatsMap, currentSubsetName);
					}
					quads.clear();
					quads.add(q);
					currentURL = q.graph();
				}
			} catch (Exception e) {
				errorCount++;
				System.out.println(e.toString());
				// TODO make this an option
				// e.printStackTrace();
			}
		}
		// process once more for the last quads
		if (quads.size() > 0) {
			processQuadsOfURL(quads, currentURL, classStatsMap, currentSubsetName);
		}
		br.close();

		// write the collected statistics to file
		try {
			
			// class stats
			BufferedWriter classWriter = OutputUtil.getGZIPBufferedWriter(new File(outputDirectory,
					(filePrefix.length() > 0 ? (filePrefix + ".") : ("")) + "class.stats."+object.getName()));
			long numTypedEntities = 0;
			classWriter.write("class\tnumEntities\tnumUrls\tnumDomains\n");
			classStatsMap = (HashMap<String, StatHolder>) MapUtils.sortByValue(classStatsMap, SortingOrderTypes.DESCENDING);
			for (String c : classStatsMap.keySet()) {
				classWriter.write(c + "\t" + classStatsMap.get(c).numEntities + "\t" + classStatsMap.get(c).urls.size()
						+ "\t" + classStatsMap.get(c).domains.size() + "\n");
				numTypedEntities += classStatsMap.get(c).numEntities;
			}
			classWriter.close();
			
						
			// general info 
			BufferedWriter classGeneralWriter = OutputUtil.getGZIPBufferedWriter(new File(outputDirectory,
					(filePrefix.length() > 0 ? (filePrefix + ".") : ("")) + "class.general."+object.getName()));
			classGeneralWriter.write("Parsed " + quadsCount + " quads. \n");
			classGeneralWriter.write("Could not parse " + errorCount + " quads. \n");
			classGeneralWriter.write("Overall found: " + numTypedEntities + " typed entities in the data. \n");
			
			HashSet<String> distinctDomains = new HashSet<String>();
			for (String c : classStatsMap.keySet()) {
				for (String domain : classStatsMap.get(c).domains) {
					distinctDomains.add(domain);
				}
			}
			classGeneralWriter.write("Distinct Domains: " + distinctDomains.size() + " \n");
			
			HashSet<String> distinctUrls = new HashSet<String>();
			for (String c : classStatsMap.keySet()) {
				for (String url : classStatsMap.get(c).urls) {
					distinctUrls.add(url);
				}
			}
			classGeneralWriter.write("Distinct URLs: " + distinctUrls.size() + " \n");

			classGeneralWriter.close();
			
			
			String toAppend = "<tr><th><a href=\""+currentSubsetName+"\">"+currentSubsetName+"</a></th><td> Quads: "+NumberFormat.getNumberInstance(Locale.US).format(quadsCount)+
					"</br> URLs: ?"+
					"</br> Hosts:  ?" +"</br></td><td>" ;
			
			Map<String, Integer> classesPerEntities = new HashMap<String, Integer>();
			
			for (String c : classStatsMap.keySet()) {
				classesPerEntities.put(c, classStatsMap.get(c).numEntities)	;				
			}
			
			List<Entry<String, Integer>> topClasses;
			List<Entry<String, Integer>> sortedTopClasses;
			
			if (classesPerEntities.size()>= 5)
				topClasses = findGreatest(classesPerEntities, 5);
			else 
				topClasses = findGreatest(classesPerEntities, classesPerEntities.size());
			
			sortedTopClasses = entriesSortedByValues(topClasses);
			
			
			for (Entry<String, Integer> entry : sortedTopClasses) {
				toAppend+=entry.getKey() + " ("+ NumberFormat.getNumberInstance(Locale.US).format(entry.getValue()) +")</br>";							
			}

			toAppend +=  "</td><td>" +readableFileSize(object.length())+"</td><td><a href=\"http://data.dws.informatik.uni-mannheim.de/structureddata/2016-12/quads/classspecific/"
					+object.getName()+"\">"+object.getName()+"</a> (<a href=\"http://data.dws.informatik.uni-mannheim.de/structureddata/2016-12/quads/classspecific/"
					+object.getName().replace(".gz", "")+".txt\">sample</a>)</td></tr>";
			
			htmlScript.println(toAppend);
			htmlScript.flush();

		} catch (Exception e) {
			System.out.println("Error while processing the file:"+object.getName());
			e.printStackTrace();
		}

	}
	
	private static <K,V extends Comparable<? super V>> 
	    List<Entry<K, V>> entriesSortedByValues(List<Entry<K, V>> list) {
	
		List<Entry<K,V>> sortedEntries = list;
		
		Collections.sort(sortedEntries, 
		    new Comparator<Entry<K,V>>() {
		        @Override
		        public int compare(Entry<K,V> e1, Entry<K,V> e2) {
		            return e2.getValue().compareTo(e1.getValue());
		        }
		    }
				);

		return sortedEntries;
	}
	
	 private static <K, V extends Comparable<? super V>> List<Entry<K, V>> 
     findGreatest(Map<K, V> map, int n)
	 {
	     Comparator<? super Entry<K, V>> comparator = 
	         new Comparator<Entry<K, V>>()
	     {
	         @Override
	         public int compare(Entry<K, V> e0, Entry<K, V> e1)
	         {
	             V v0 = e0.getValue();
	             V v1 = e1.getValue();
	             return v0.compareTo(v1);
	         }
	     };
	     PriorityQueue<Entry<K, V>> highest = 
	         new PriorityQueue<Entry<K,V>>(n, comparator);
	     for (Entry<K, V> entry : map.entrySet())
	     {
	         highest.offer(entry);
	         while (highest.size() > n)
	         {
	             highest.poll();
	         }
	     }
	
	     List<Entry<K, V>> result = new ArrayList<Map.Entry<K,V>>();
	     while (highest.size() > 0)
	     {
	         result.add(highest.poll());
	     }
	     return result;
	}
	 

	
	private static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	// process all quads of one URL and create the necessary aggregated stats.
	private void processQuadsOfURL(List<Quad> quads, String url, 
			HashMap<String, StatHolder> classStatsMap, String currentClass) {
		String domain = DomainUtil.getPayLevelDomainFromWholeURL(url);
		if (domain == null) {
			// this should not happen
			return;
		}
		// save some space
		domain = domain.intern();

		// internal maps
		HashMap<String, HashSet<String>> classEntityMap = new HashMap<String, HashSet<String>>();

		// check all quads for one URL
		for (Quad q : quads) {
			// check if its a type quad
			// if (typeProperties.contains(q.predicate())) {
			if (isType(q.predicate())) {
				// add class entities
				HashSet<String> entities = classEntityMap.get(q.value().value());
				if (entities == null) {
					classEntityMap.put(q.value().value(), new HashSet<String>());
				}
				classEntityMap.get(q.value().value()).add(q.subject().value());

			}
		}

		// summarize stats		
		for (String c : classEntityMap.keySet()) {
			StatHolder relevantstats = classStatsMap.get(c);
			if (relevantstats == null) {
				relevantstats = new StatHolder();
			}
			relevantstats.numEntities += classEntityMap.get(c).size();
			
			classStatsMap.put(c, relevantstats);
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
	
	@Override
	protected void beforeProcess(){
		try {
			htmlScript = new PrintWriter ( new BufferedWriter( new FileWriter(outputDirectory.getPath()+"/htmlScript.txt", true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Problem while creating HTML script:"+e.getMessage());
		}
	}
	

	@Override
	protected void afterProcess() {
		htmlScript.flush();
		htmlScript.close();
		
	}

	public static void main(String[] args)  {
		WDCSubsetStatsCalculator cal = null;
		try {
			cal = new WDCSubsetStatsCalculator();
			new JCommander(cal, args);
			cal.process();
		} catch (ParameterException pe) {
			pe.printStackTrace();
			new JCommander(cal).usage();
		}
	}

}
