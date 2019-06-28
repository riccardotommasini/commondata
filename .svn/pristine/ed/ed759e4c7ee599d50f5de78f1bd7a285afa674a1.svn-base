package org.webdatacommons.structureddata.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dwslab.dwslib.util.io.InputUtil;

public class SubsetsUtils {

	public static void main(String[] args) throws Exception {

		SubsetsUtils utils = new SubsetsUtils();
		utils.countLines();

	}
	
	public void sort() throws IOException{
		String inputFile = "C:\\Users\\User\\Desktop\\htmlScript.txt";
		String outputFile = "C:\\Users\\User\\Desktop\\htmlScriptSorted.txt";

		FileReader fileReader = new FileReader(inputFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String inputLine;
		List<String> lineList = new ArrayList<String>();
		while ((inputLine = bufferedReader.readLine()) != null) {
			lineList.add(inputLine);
		}
		fileReader.close();

		Collections.sort(lineList);

		FileWriter fileWriter = new FileWriter(outputFile);
		PrintWriter out = new PrintWriter(fileWriter);
		for (String outputLine : lineList) {
			out.println(outputLine);
		}
		out.flush();
		out.close();
		fileWriter.close();
	}
	
	public void countLines() throws IOException{
		
		File folder = new File("");
		File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	    	BufferedReader br = InputUtil.getBufferedReader(listOfFiles[i]);
	    	int lines = 0;
			while (br.readLine() != null) lines++;
			
			br.close();
							
			System.out.println(listOfFiles[i].getName()+":"+lines);
	    }
		
	}
}
