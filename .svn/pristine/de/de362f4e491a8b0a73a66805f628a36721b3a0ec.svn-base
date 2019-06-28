package org.webdatacommons.webgraph.util;

import it.unimi.dsi.fastutil.longs.LongBigArrays;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class MappingHelper {

	/**
	 * creates a full mapping list for the mapping of in and out component
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out
				.println("MappingHelper <LISTIN> <LISTOUT> <MAXINDEX> <OUTPUTFILE>");

		String listIN = args[0];
		String listOUT = args[1];
		String outputList = args[3];
		Long maxIndex = Long.parseLong(args[2]);

		long[][] mapping = LongBigArrays.newBigArray(maxIndex);
		// init with one on one mapping
		for (long i = 0; i < maxIndex; i++) {
			LongBigArrays.set(mapping, i, i);
		}

		BufferedReader br = InputUtil.getBufferedReader(new File(listIN));
		while (br.ready()) {
			String line = br.readLine();
			String tok[] = line.split("\t");
			long mappingId = Long.parseLong(tok[0]);
			long id = Long.parseLong(tok[1]);
			LongBigArrays.set(mapping, id, mappingId);
		}
		br.close();

		br = InputUtil.getBufferedReader(new File(listOUT));
		while (br.ready()) {
			String line = br.readLine();
			String tok[] = line.split("\t");
			long mappingId = Long.parseLong(tok[0]);
			long id = Long.parseLong(tok[1]);
			LongBigArrays.set(mapping, id, mappingId);
		}
		br.close();

		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(new File(outputList)))));
		for (long i = 0; i < maxIndex; i++) {
			bw.write(i + "\t" + LongBigArrays.get(mapping, i) + "\n");
		}
		bw.close();
	}

}
