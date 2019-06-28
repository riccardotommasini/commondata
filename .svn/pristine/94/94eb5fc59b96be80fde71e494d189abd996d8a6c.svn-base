package org.webdatacommons.webgraph.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;

public class RemoveDuplicates {

	public static void main(String[] args)
	{
		if(args.length!=2)
		{
			System.out.println("parameters:");
			System.out.println("1. input file");
			System.out.println("2. output file");
			return;
		}
		
		try {
			new RemoveDuplicates().Remove(args[0], args[1]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Remove(String input, String output) throws IOException
	{
		TreeSet<String> set = new TreeSet<String>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(input)));
		
		String line;
		
		while((line = reader.readLine())!=null)
			set.add(line);
		
		reader.close();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		
		for(String s : set)
			writer.write(s + "\n");
		
		writer.close();
	}
	
}
