package org.webdatacommons.webgraph.process.shrink;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class SubgraphExtractor {

	public static void main(String[] args) throws IOException {
		System.out.println("Loading id list.");
		BufferedReader br = InputUtil.getBufferedReader(new File(args[0]));
		Set<Long> ids = new TreeSet<Long>();
		
		long cnt = 0;
		while (br.ready()) {
			cnt++;
			ids.add(Long.parseLong(br.readLine().split("\t")[0]));
		}
		br.close();
		System.out.println("Read " + cnt + " ids from file.");
		System.out.println("Reading network file.");
		br = InputUtil.getBufferedReader(new File(args[1]));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(new File(args[2])))));
		cnt = 0;
		while (br.ready()){
			if (++cnt % 100000 == 0){
				System.out.println("Parsed " + cnt + " lines.");
			}
			String line = br.readLine();
			String tok[] = line.split("\t");
			long start = Long.parseLong(tok[0]);
			if (ids.contains(start)){
				ArrayList<Long> subIds = new ArrayList<Long>();
				for (int i = 1; i < tok.length; i++){
					long id = Long.parseLong(tok[i]);
					if (ids.contains(id)){
						subIds.add(id);
					}
				}
				if (subIds.size() > 0){
					bw.write("" + start);
					for (Long id : subIds){
						bw.write("\t" + id);
					}
					bw.write("\n");
				}
			}
		}
		bw.close();
		br.close();
		System.out.println("Done.");
	}

}
