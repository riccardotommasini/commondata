package org.webdatacommons.webgraph.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * This class creates an index for a given list of urls.
 * 
 * @author Robert
 * 
 */
public class Indexer {

	public static void main(String[] args) {
		writeIndex(args[0], args[1]);
	}

	/**
	 * This function creates for a given set of input files an index where each
	 * line of the input file gets an id. The function pays attention on the
	 * order of the files, as they will be processed based on their naming.
	 * 
	 * @param inputDirectory
	 *            the directory with the input files
	 * @param outputDirectory
	 *            the directory for the output (index)
	 */
	private static void writeIndex(String inputDirectory, String outputDirectory) {
		File inputDir = new File(inputDirectory);
		File outputDir = new File(outputDirectory);
		if (inputDir.isFile() || outputDir.isFile()) {
			System.out.println("Input is not a directory.");
			return;
		} else {
			List<File> files = new ArrayList<File>();
			BufferedWriter bw;
			try {

				for (File f : inputDir.listFiles()) {
					if (f.isFile()) {
						files.add(f);
					}
				}
				// now we sort it
				Collections.sort(files);
				long index = 0;
				long fileCnt = 0;
				System.out.println(new Date() + " Starting parsing ...");
				for (File file : files) {
					bw = new BufferedWriter(new FileWriter(new File(outputDir,
							"index-" + String.format("%05d", fileCnt))));
					fileCnt++;
					if (fileCnt % 10 == 0) {
						System.out.println("Parsed "
								+ (((double) fileCnt) / files.size()) * 100
								+ " % of files. " + new Date());
					}
					try {
						BufferedReader br;
						if (file.getName().endsWith(".gz")) {
							br = new BufferedReader(new InputStreamReader(
									new GZIPInputStream(new FileInputStream(
											file))));
						} else {
							br = new BufferedReader(new FileReader(file));
						}
						String line = "";
						while (br.ready()) {
							line = br.readLine();
							bw.write(line.trim() + "\t" + index + "\n");
							index++;
						}
						br.close();
					} catch (FileNotFoundException e) {
						System.out.println("Could not open file "
								+ file.getAbsolutePath());
						e.printStackTrace();
					} catch (IOException e) {
						System.out.println("Could not read file "
								+ file.getAbsolutePath());
						e.printStackTrace();
					}
					bw.close();
				}
				System.out.println(new Date() + " Done. Parsed " + fileCnt
						+ " files with " + index + " lines.");
			} catch (IOException e1) {
				System.out.println("Could not create outputfile.");
				e1.printStackTrace();
				return;
			}
		}

	}
}
