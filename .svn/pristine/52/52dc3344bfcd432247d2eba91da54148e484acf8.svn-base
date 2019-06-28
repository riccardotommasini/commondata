package org.webdatacommons.webgraph.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class AdaptMappingFile {

	public static void main(String[] args) throws IOException {
		System.out
				.println("USAGE: AdaptMappingFile <MAPPINGFILE> <IDSTOMAP> <OUTPUT>");
		BufferedReader br = InputUtil.getBufferedReader(new File(args[1]));
		Set<Long> listOfIds = new TreeSet<Long>();
		while (br.ready()) {
			String line = br.readLine();
			listOfIds.add(Long.parseLong(line));
		}
		System.out.println("Read " + listOfIds.size()
				+ " ids to be mapped. Rest is ignored in mapping.");
		br.close();

		br = InputUtil.getBufferedReader(new File(args[0]));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(new File(args[2])))));
		long lnCnt = 0;
		while (br.ready()) {
			String line = br.readLine();
			long mappingId = Long.parseLong(line);
			if (listOfIds.contains(mappingId)) {
				bw.write(mappingId + "\n");
			} else {
				bw.write(lnCnt + "\n");
			}
			if (++lnCnt % 10000000 == 0){
				System.out.println("Parsed " + lnCnt + " lines.");
			}
		}
		br.close();
		bw.close();

	}

}
