package org.webdatacommons.webgraph.process.format;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FromEdgeToAdjazens {

	public static void main(String[] args) throws IOException {
		format(args[0], args[1]);
	}

	private static void format(String inputFileOrDirectory, String basename)
			throws IOException {
		// first we get all data which need to be considered
		System.out.println(new Date() + " " + "Validating input ...");
		List<File> filesToWorkThrough = new ArrayList<File>();
		File input = new File(inputFileOrDirectory);
		if (input.isDirectory()) {
			for (File f : input.listFiles()) {
				if (f.isFile()) {
					filesToWorkThrough.add(f);
				} else {
					System.out.println("Found sub-directoy " + f.getName()
							+ " in input directory. Skipping.");
				}
			}
		} else {
			filesToWorkThrough.add(input);
		}
		// now that we have the list we need to sort it as the input needs to be
		// sorted
		Collections.sort(filesToWorkThrough);
		System.out.println("Found " + filesToWorkThrough.size()
				+ " files to be parsed.");
		// now we go through the list and write our new output somewhere
		if (!basename.endsWith("gz")) {
			basename = basename + ".gz";
		}
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(new File(basename)))));
		BufferedReader br;
		long selfArcs = 0;
		List<Long> targets = new ArrayList<Long>();
		long curVertex = -1;
		String line;
		for (File inputFile : filesToWorkThrough) {
			System.out.println(new Date() + " " + "Parsing file "
					+ inputFile.getName());
			if (inputFile.getName().endsWith(".gz")) {
				br = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(new FileInputStream(inputFile))));
			} else {
				br = new BufferedReader(new FileReader(inputFile));
			}
			while (br.ready()) {
				// each line is an arc LONG LONG
				line = br.readLine();
				if (line == null || line.length() < 1) {
					System.out
							.println("Line could not be read. Skipping.");
					continue;
				}
				String[] tok = line.split("\t");
				if (tok.length != 2) {
					System.out
							.println("Line size does not match expected pattern <LONG><TAB><LONG>");
					continue;
				}
				// check if we have to write a new line
				if (curVertex != Long.parseLong(tok[0])) {
					// if its not the first time write data
					if (curVertex != -1) {
						StringBuffer lineToWrite = new StringBuffer();
						lineToWrite.append(curVertex);
						lineToWrite.append("\t");
						lineToWrite.append(targets.size());
						Collections.sort(targets);
						for (Long t : targets) {
							lineToWrite.append("\t");
							lineToWrite.append(t);
						}
						lineToWrite.append("\n");
						bw.write(lineToWrite.toString());
					}
					// reset buffer
					targets = new ArrayList<Long>();
					curVertex = Long.parseLong(tok[0]);
				}

				// now add the "arcs"
				long targetId = Long.parseLong(tok[1]);
				if (targetId != curVertex) {
					targets.add(targetId);
				} else {
					selfArcs++;
				}
			}
			br.close();
		}
		// make sure we write everything out to the file
		StringBuffer lineToWrite = new StringBuffer();
		lineToWrite.append(curVertex);
		lineToWrite.append("\t");
		lineToWrite.append(targets.size());
		Collections.sort(targets);
		for (Long t : targets) {
			lineToWrite.append("\t");
			lineToWrite.append(t);
		}
		lineToWrite.append("\n");
		bw.write(lineToWrite.toString());

		bw.close();
		System.out.println("Did not write " + selfArcs
				+ " arcs pointing to vertex itself.");
		System.out.println(new Date() + " " + "Done.");
	}

}
