package org.webdatacommons.structureddata.cleanser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ldif.local.datasources.dump.QuadFileLoader;
import ldif.runtime.Quad;

import org.webdatacommons.structureddata.util.ParsingUtil;

import scala.actors.threadpool.Arrays;
import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;

/**
 * This class simply sorts all quads belonging to one webpage (not site) in
 * order to later process the entities (containing 1+ quads). Allows in addition
 * the filtering of quads (e.g. "schema.org").
 * 
 * @author Robert Meusel (robert@dwslab.de)
 * 
 */
public class SortEntityByWebPage extends Processor<File> {

	protected File folderWithFilesToProcess;
	protected File outputFolder;
	protected static String filter = "";

	public SortEntityByWebPage(String inputFolder, String outputFolder,
			String filter, int threads) throws Exception {
		super(threads);
		if (filter != null) {
			this.filter = filter;
		}
		this.folderWithFilesToProcess = new File(inputFolder);
		if (this.folderWithFilesToProcess == null
				|| this.folderWithFilesToProcess.isFile()) {
			throw new Exception(
					"Folder with files to process is not a folder or null.");
		}
		this.outputFolder = new File(outputFolder);
		if (this.outputFolder == null || this.outputFolder.isFile()) {
			throw new Exception("Output folder is not a folder or null.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<File> fillListToProcess() {
		return Arrays.asList(folderWithFilesToProcess.listFiles());
	}

	@Override
	protected void process(File object) throws Exception {
		BufferedWriter bw = OutputUtil.getGZIPBufferedWriter(new File(
				this.outputFolder, object.getName()));
		QuadFileLoader qfl = new QuadFileLoader();

		BufferedReader br = InputUtil.getBufferedReader(object);
		String currentURL = "";
		Map<String, ArrayList<Quad>> quadList = new HashMap<String, ArrayList<Quad>>();
		String line = "";
		while (br.ready()) {
			Quad q;
			try {
				line = br.readLine();
				q = qfl.parseQuadLine(ParsingUtil.correctLanguageTag(line));
			} catch (Exception e) {
				System.out.println("Could not load line: "
						+ ParsingUtil.correctLanguageTag(line));
				continue;
			}

			// filter
			if (q == null
					|| (!q.predicate().toLowerCase().contains(filter) && !q
							.value().value().toLowerCase().contains(filter))) {
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

}
