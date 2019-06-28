package org.webdatacommons.webgraph.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Join {

	public static void main(String args[])
	{
		if(args.length!=7)
		{
			System.out.println("Parameters:");
			System.out.println("1. join type:");
			System.out.println("  leftindexedjoin");
			System.out.println();
			System.out.println("1: leftindexedjoin");
			System.out.println("2. left file");
			System.out.println("3. index file");
			System.out.println("4. output file");
			System.out.println("5. value for indexed records");
			System.out.println("6. value for non-indexed records");
			System.out.println("7. output value of left file [true|false]");
		}
		
		Join j = new Join();
		
		if(args[0].equals("leftindexedjoin"))
		{
			try {
				boolean bKeepLeft = Boolean.parseBoolean(args[6]);
				
				j.LeftIndexedJoin(args[1], args[2], args[3], args[4], args[5], bKeepLeft);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Unknown parameter: " + args[0]);
			return;
		}
		
		System.out.println("Done.");
	}

	/*
	 * Joins two (tab-separated) files by comparing the values specified in leftindex and rightIndex 
	 */
	public void LeftJoin(String leftFile, String rightFile, String outFile, int leftIndex, int rightIndex)
	{
		
	}
	
	/*
	 * For every line of leftFile, if the line is contained in indicesFile, joins value, otherwise join noValue
	 */
	public void LeftIndexedJoin(String leftFile, String indicesFile, String outFile, String value, String noValue, boolean keepLeftValue) throws IOException
	{		
		String line;
		
		ArrayList<Integer> indexValues = new ArrayList<Integer>();
		
		System.out.println("Reading indices into memory ...");
		BufferedReader indices = new BufferedReader(new FileReader(new File(indicesFile)));
		while((line = indices.readLine()) != null)
		{
			indexValues.add(Integer.parseInt(line));
		}
		indices.close();
		
		System.out.println("Joining ...");
		BufferedReader left = new BufferedReader(new FileReader(new File(leftFile)));
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outFile)));
		int index=0;
		while((line = left.readLine()) != null)
		{
			if(indexValues.size() > 0 && index == indexValues.get(0))
			{
				if(keepLeftValue)
					out.write(line + "\t" + value + "\n");
				else
					out.write(value + "\n");
				indexValues.remove(0);
			}
			else
			{
				if(keepLeftValue)
					out.write(line + "\t" + noValue + "\n");
				else
					out.write(noValue + "\n");
			}
			index++;
			
			if(index % 100000 == 0)
				System.out.println(index + " lines processed");
		}
		left.close();
		out.close();
	}
	
}

