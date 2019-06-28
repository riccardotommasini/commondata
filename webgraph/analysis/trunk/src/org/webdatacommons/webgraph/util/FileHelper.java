package org.webdatacommons.webgraph.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class FileHelper {

	public static void main(String[] args) throws NumberFormatException,
			IOException {
		if (args.length < 1) {
			System.out.println("Usage: FileHelper inputfile");
			return;
		}
		tab2List(args[0]);
	}

	/**
	 * Converts somehow stored chunks with an tab separated into a list where
	 * each line has one id.
	 * 
	 * @param fileName
	 *            the file name
	 * @param leaveOut
	 *            leaves to the corresponding position in each line
	 * @throws IOException
	 */
	public static void tab2List(String fileName) throws IOException {
		BufferedReader br = InputUtil.getBufferedReader(new File(fileName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("list_"
				+ fileName.replace(".gz", ""))));
		StringBuilder sb = new StringBuilder();
		boolean ignoreFirst = true;
		while (br.ready()) {
			char c = (char) br.read();
			if (c == '\t' || c == '\n') {
				if (!ignoreFirst)
					bw.write(sb.toString() + "\n");
				sb.setLength(0);
				/*
				 * Ignores first token after line-break
				 */
				if (c == '\n') {
					ignoreFirst = true;
				} else {
					ignoreFirst = false;
				}
			} else {
				sb.append(c);
			}
			// String line = br.readLine();
			// String tok[] = line.split("\t");
			// for (int i = 0; i < tok.length; i++){
			// if (i != leaveOut){
			// bw.write(tok[i] + "\n");
			// }
			// }
		}
		bw.close();
		br.close();

	}

}
