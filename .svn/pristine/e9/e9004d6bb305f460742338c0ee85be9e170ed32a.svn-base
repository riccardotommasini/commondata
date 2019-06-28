package org.webdatacommons.webgraph.process.format;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class reads the plds and creates an index file. Than replaces all labels
 * with the corresponding integer value and stores a adjazent matrix
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 */
public class FromSparseToAdjazens {

	public static void main(String[] args) throws IOException {
		makeIndexAndMatrix(args[0]);
	}

	private static void makeIndexAndMatrix(String inputFile) throws IOException {
		System.out.println(new Date() + " Starting ...");
		Map<String, Integer> index = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(new File(
				inputFile)));
		int numVertices = 0;
		String line;

		int lineCnt = 0;
		while (br.ready()) {
			lineCnt++;
			if (lineCnt % 1000000 == 0) {
				System.out.println(new Date() + " Processed " + lineCnt
						+ " lines.");
			}
			line = br.readLine();
			String[] tokens = line.split("\t");
			if (tokens.length < 2) {
				continue;
			} else {
				String origin = tokens[0];
				if (!index.containsKey(origin)) {
					index.put(origin, numVertices);
					numVertices++;
				}
			}
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				inputFile + "_index")));
		for (String key : index.keySet()) {
			bw.write(key + "\t" + index.get(key) + "\n");
		}
		System.out.println("Index loaded: It has " + numVertices + " entries.");
		bw.close();
		br.close();
		bw = new BufferedWriter(new FileWriter(new File(inputFile + "_adj")));
		br = new BufferedReader(new FileReader(new File(inputFile)));
		numVertices = 0;
		lineCnt = 0;
		long arcCnt = 0;
		while (br.ready()) {
			lineCnt++;
			if (lineCnt % 1000000 == 0) {
				System.out.println(new Date() + " Processed " + lineCnt
						+ " lines.");
			}

			line = br.readLine();
			String[] tokens = line.split("\t");
			if (tokens.length < 2) {
				continue;
			} else {
				StringBuffer lineToWrite = new StringBuffer();
				String origin = tokens[0];
				if (index.containsKey(origin)) {
					lineToWrite.append(index.get(origin));
				} else {
					// should not happen
					continue;
				}
				// some cleaning
				String targetString = tokens[1].replace("{", "").replace("}",
						"");
				List<Integer> idList = new ArrayList<Integer>();
				if (targetString.length() > 0) {
					String[] urls = targetString.split("\\),\\(");
					for (String url : urls) {
						// more cleaning
						// remove it
						url = url.replace("(", "").replace(")", "");
						// cut the number
						url = url.substring(0, url.indexOf(","));
						if (index.containsKey(url)) {
							idList.add(index.get(url));
							arcCnt++;
						}
					}
				}
				lineToWrite.append("\t");
				lineToWrite.append(idList.size());
				for (Integer i : idList) {
					lineToWrite.append("\t");
					lineToWrite.append(i);
				}
				lineToWrite.append("\n");
				bw.write(lineToWrite.toString());
			}
		}		
		bw.close();
		br.close();
		System.out.println("Found " + arcCnt + " arcs.");
		System.out.println(new Date() + " Done ...");
	}
}
