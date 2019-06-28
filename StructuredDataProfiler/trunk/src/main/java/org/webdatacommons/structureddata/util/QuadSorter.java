package org.webdatacommons.structureddata.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;
import ldif.local.datasources.dump.QuadFileLoader;
import ldif.runtime.Quad;

/**
 * This class sortes the quads from input files for each web page based on the
 * subject. NOTE: This only works if (a) the quads of one website are grouped in
 * one block of lines and (b) if quads from website are not located in two
 * files.
 * 
 * @author Robert Meusel (robert@dwslab.de)
 * 
 */
@Parameters(commandDescription = "Sorts the quads within an file based on their subject and webpage/url.")
public class QuadSorter extends Processor<File> {

	@Parameter(names = { "-out",
			"-outputDir" }, required = true, description = "Folder where the outputfile(s) are written to.", converter = FileConverter.class)
	private File outputDirectory;

	@Parameter(names = { "-in",
			"-inputDir" }, required = true, description = "Folder where the input is read from.", converter = FileConverter.class)
	private File inputDirectory;

	@Parameter(names = { "-p",
			"-prefix" }, description = "Prefix of files in the input folder which will be processed.")
	private String filePrefix = "";

	@Parameter(names = "-threads", required = true, description = "Number of threads.")
	private Integer threads;

	@Parameter(names = { "-vocabFilter",
			"" }, required = false, description = "Filters quads we either do not contain the vocab in the predicate or object.")
	private String vocabFilter = null;

	@Parameter(names = "-debug", required = false, description = "Enables detailed debug messages.")
	private boolean debug = false;

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

	@Override
	protected int getNumberOfThreads() {
		return threads;
	}

	@Override
	protected void process(File object) throws Exception {
		BufferedWriter bw = OutputUtil.getGZIPBufferedWriter(new File(this.outputDirectory, object.getName()));
		QuadFileLoader qfl = new QuadFileLoader();

		BufferedReader br = InputUtil.getBufferedReader(object);
		String currentURL = "";
		Map<String, ArrayList<Quad>> quadList = new HashMap<String, ArrayList<Quad>>();
		String line = "";
		while (br.ready()) {
			Quad q;
			try {
				line = br.readLine();
				q = qfl.parseQuadLine(line);
			} catch (Exception e) {
				if (debug) {
					System.out.println("Could not load line: " + line);
				}
				continue;
			}

			// filter
			if (q == null || (vocabFilter != null && (!q.predicate().toLowerCase().contains(vocabFilter)
					&& !q.value().value().toLowerCase().contains(vocabFilter)))) {
				continue;
			}

			if (q.graph().equals(currentURL)) {
				if (!quadList.containsKey(q.subject().value())) {
					quadList.put(q.subject().value(), new ArrayList<Quad>());
				}
				quadList.get(q.subject().value()).add(q);
			} else {
				if (quadList.size() > 0) {
					for (String s : quadList.keySet()) {
						for (Quad quad : quadList.get(s)) {
							bw.write(quad.toLine());
						}
					}
				}
				// re-init list
				quadList.clear();
				quadList.put(q.subject().value(), new ArrayList<Quad>());
				quadList.get(q.subject().value()).add(q);
				currentURL = q.graph();
			}
		}
		// one final time:
		if (quadList.size() > 0) {
			for (String s : quadList.keySet()) {
				for (Quad quad : quadList.get(s)) {
					bw.write(quad.toLine());
				}
			}
		}

		br.close();
		bw.close();
	}

	public static void main(String[] args) {
		QuadSorter cal = new QuadSorter();
		try {
			new JCommander(cal, args);
			cal.process();
		} catch (ParameterException pe) {
			pe.printStackTrace();
			new JCommander(cal).usage();
		}
	}

}
