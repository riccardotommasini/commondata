package org.webdatacommons.webgraph.util;

import it.unimi.dsi.fastutil.longs.LongBigArrays;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class ComponentCalculationHelper {

	public static void main(String[] args) throws NumberFormatException,
			IOException {
		// load sizes
		BufferedReader br = InputUtil.getBufferedReader(new File(args[0]));
		long[][] sizes = LongBigArrays.newBigArray(Long.parseLong(args[1]));

		// This could be optimized, if we assume that a large number of components has the size 1
		// we could simply stop reading the file and initialize beforehand with 1 (instead of 0)
		System.out.println("Start reading sizes.");
		long lineCnt = 0;
		while (br.ready()) {
			String line = br.readLine();
			long size = Long.parseLong(line);
			LongBigArrays.set(sizes, lineCnt, size);
			lineCnt++;
		}
		System.out.println("Read sizes");
		br.close();

		for (int i = 2; i < args.length; i++) {
			long sum = 0;
			br = InputUtil.getBufferedReader(new File(args[i]));
			System.out.println("Reading file " + args[i]);
			while (br.ready()) {
				String line = br.readLine();
				long id = Long.parseLong(line);
				sum += LongBigArrays.get(sizes, id);
			}
			br.close();
			System.out
					.println("Size of ids in file " + args[i] + " is: " + sum);
		}
		System.out.println("Done.");
	}

}
