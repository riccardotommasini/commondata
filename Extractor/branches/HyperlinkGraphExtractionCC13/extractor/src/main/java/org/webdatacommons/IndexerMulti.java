package org.webdatacommons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.fuberlin.wbsg.ccrdf.Statistics;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

/**
 * Class to create an index from sorted quads comming from quad_sort.pig
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 */
public class IndexerMulti {

	private static Logger log = Logger.getLogger(IndexerMulti.class);

	public static void main(String[] args) throws IOException, JSAPException {
		JSAP jsap = new JSAP();
		FlaggedOption quadDir = new FlaggedOption("quaddir")
				.setStringParser(JSAP.STRING_PARSER).setRequired(true)
				.setLongFlag("quaddir").setShortFlag('i');
		quadDir.setHelp("Extraction statistics directory");
		jsap.registerParameter(quadDir);

		FlaggedOption outDir = new FlaggedOption("outdir")
				.setStringParser(JSAP.STRING_PARSER).setRequired(true)
				.setLongFlag("outdir").setShortFlag('o');
		outDir.setHelp("Output directory");
		jsap.registerParameter(outDir);

		JSAPResult config = jsap.parse(args);

		if (!config.success()) {
			System.err.println("Usage: " + Statistics.class.getName() + " "
					+ jsap.getUsage());
			System.err.println(jsap.getHelp());
			System.exit(1);
		}

		// get files in folder
		List<File> quadFiles = getQuadFiles(config.getString("quaddir"));
		// output dir
		File outputDir = new File(config.getString("outdir"));

		for (File f : quadFiles) {
			processQuadFile(f, outputDir);
		}
	}

	/**
	 * Process the document and cleans the triple. Writes an Index for the
	 * files.
	 * 
	 * @param quadFile
	 *            input quad file with tld and pld from quad_sort.pig
	 * @param outputDir
	 *            output directory
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void processQuadFile(File quadFile, File outputDir)
			throws FileNotFoundException, IOException {
		log.info("Processing " + quadFile.getName());

		// reader
		Reader reader = new InputStreamReader(new GZIPInputStream(
				new FileInputStream(quadFile)));
		BufferedReader br = new BufferedReader(reader);
		String fileName = quadFile.getName();
		String outputFileName = fileName.replace(".gz", ".sort.gz");
		// index writer
		FileWriter fw = new FileWriter(new File(outputDir, fileName.replace(
				".gz", ".index")));
		BufferedWriter bw = new BufferedWriter(fw);
		// quad writer
		File quadOutFile = new File(outputDir, outputFileName);
		Writer writer = new OutputStreamWriter(new GZIPOutputStream(
				new FileOutputStream(quadOutFile)));
		BufferedWriter quadWriter = new BufferedWriter(writer);

		long lineCount = 0;
		long start = 0;
		String line;
		;
		String prevPld = "";
		String prevTld = "";
		String[] tldPldQuad = new String[3];
		while (br.ready()) {
			line = br.readLine();
			if (line.trim().length() < 1 || line.trim().split("\t").length < 3) {
				continue;
			}
			lineCount++;
			if (lineCount % 100000 == 0) {
				log.info("Processed " + lineCount + " lines.");
			}
			tldPldQuad = getTldPldQuadFromLine(line, true);
			quadWriter.write(tldPldQuad[2] + "\n");
			if (!tldPldQuad[0].equals(prevTld)
					|| !tldPldQuad[1].equals(prevPld)) {
				// check if its initializiation step
				if (start != 0) {
					// write index
					bw.write(prevTld + "\t" + prevPld + "\t" + outputFileName
							+ "\t" + start + "\t" + (lineCount - 1) + "\n");
				}
				start = lineCount;
				prevTld = tldPldQuad[0];
				prevPld = tldPldQuad[1];
			}
		}
		bw.write(prevTld + "\t" + prevPld + "\t" + outputFileName + "\t"
				+ start + "\t" + lineCount + "\n");
		bw.flush();
		bw.close();
		quadWriter.flush();
		quadWriter.close();
		br.close();

	}

	/**
	 * Extracts tld, pld and quads from line
	 * 
	 * @param line
	 *            the line
	 * @param topApp
	 *            if tld and pld are append before the line
	 * @return array with 0 tld 1 pld 2 line
	 */
	private static String[] getTldPldQuadFromLine(String line, boolean topApp) {

		String[] tldPldQuad = new String[3];
		int firstInd = 0;
		int secondInd = 0;
		try {

			if (topApp) {
				firstInd = line.indexOf('\t');
				tldPldQuad[0] = line.substring(0, firstInd);
				secondInd = line.indexOf('\t', firstInd + 1);
				tldPldQuad[1] = line.substring(firstInd + 1, secondInd);
				tldPldQuad[2] = line.substring(secondInd + 1, line.length());
			} else {
				firstInd = line.lastIndexOf('\t');
				int len = line.length();
				tldPldQuad[1] = line.substring(firstInd + 1, len);
				secondInd = line.lastIndexOf('\t', firstInd);
				tldPldQuad[0] = line.substring(secondInd + 1, firstInd);
				tldPldQuad[2] = line.substring(0, secondInd);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage() + " in " + line + " within index "
					+ firstInd + " and " + secondInd, ex);
		}
		return tldPldQuad;
	}

	// get all files from directory mapping ".gz"
	private static List<File> getQuadFiles(String dirName) {
		List<File> quadList = new ArrayList<File>();
		try {
			if (dirName != null && dirName.length() > 0) {
				File dir = new File(dirName);
				if (dir.isFile()) {
					throw new Exception("Path is a file not a directory.");
				} else {
					for (File f : dir.listFiles()) {
						if (f.isFile() && f.getName().endsWith(".gz")) {
							quadList.add(f);
						}
					}
				}

			} else {
				throw new Exception("Directory name not given or empty.");
			}
		} catch (Exception ex) {
			log.error(ex);
			System.exit(1);
		}
		return quadList;
	}
}
