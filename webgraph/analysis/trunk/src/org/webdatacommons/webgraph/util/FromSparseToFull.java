package org.webdatacommons.webgraph.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class FromSparseToFull {
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = InputUtil.getBufferedReader(new File(args[0]));
		BufferedWriter bw = new BufferedWriter(new FileWriter(args[1]));
		long outputLineCnt = 0;
		while (br.ready()){
			String line = br.readLine();
			String tok[] = line.split("\t");
			while (outputLineCnt < Long.parseLong(tok[0])){
				bw.write("0\n");
				outputLineCnt++;
			}
			bw.write(tok[1] + "\n");
			outputLineCnt++;
		}
		
		br.close();
		bw.close();
	}

}
