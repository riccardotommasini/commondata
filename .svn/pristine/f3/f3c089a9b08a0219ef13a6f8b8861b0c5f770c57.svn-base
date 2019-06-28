package org.webdatacommons.webgraph.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class TendrilTubeHelper {

	public static void main(String[] args) throws NumberFormatException, IOException {
		Set<Integer> set1 = new TreeSet<Integer>();
		Set<Integer> set2 = new TreeSet<Integer>();
		//read set1
		BufferedReader br = InputUtil.getBufferedReader(new File(args[0]));
		while (br.ready()){
			String line = br.readLine();
			Integer id = Integer.parseInt(line);
			set1.add(id);
		}
		br.close();
		//read set2
		br = InputUtil.getBufferedReader(new File(args[1]));
		while (br.ready()){
			String line = br.readLine();
			Integer id = Integer.parseInt(line);
			set2.add(id);
		}
		br.close();
		// calculate intersection
		Set<Integer> tubes = new TreeSet<Integer>(set1);
		tubes.retainAll(set2);
		printSet(tubes, "tube_ids");
		Set<Integer> tendrils1 = new TreeSet<Integer>(set1);
		tendrils1.removeAll(tubes);
		printSet(tendrils1, "tendrils1_ids");
		Set<Integer> tendrils2 = new TreeSet<Integer>(set2);
		tendrils2.removeAll(tubes);
		printSet(tendrils2, "tendrils2_ids");
		
		System.out.println("Done.");
	}
	
	private static void printSet(Set<Integer> set, String fileName) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));
		for (Integer i : set){
			bw.write(i + "\n");
		}
		bw.close();
	}
	
}
