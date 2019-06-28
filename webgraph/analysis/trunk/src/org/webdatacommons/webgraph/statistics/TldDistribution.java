package org.webdatacommons.webgraph.statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;
import de.uni_mannheim.informatik.dws.dwslib.util.Progress;

public class TldDistribution {
	
	public static void main(String[] args)
	{
		if(args.length!=2)
		{
			System.out.println("Parameters:");
			System.out.println("1. index file");
			System.out.println("2. output file");
			return;
		}
		
		TldDistribution t = new TldDistribution();
		
		try {
			t.Count(args[0], args[1], false);
			t.Count(args[0], args[1] + ".last", true);
			System.out.println("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void Count(String PldIndex, String output, boolean lastPartOnly) throws IOException
	{
		TreeMap<String, Long> tlds = new TreeMap<String, Long>();
		
		// Split Index
		System.out.println("Reading index ...");
		BufferedReader reader = new BufferedReader(new FileReader(new File(PldIndex)));
		String line;
		Progress p = new Progress(100000, "lines read ...");
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			String url = values[0];
			
			// Identify TLD
			String tldName;
			if(lastPartOnly)
			{
				String[] parts = url.split("\\.");
				tldName = parts[parts.length-1];
			}
			else
				tldName = DomainUtil.getTopLevelDomain(url);
			
			if(tlds.containsKey(tldName))
				tlds.put(tldName, tlds.get(tldName)+1);
			else
				tlds.put(tldName, 1l);
			
			p.IncrementAndPrint();
		}
		reader.close();
		
		// write output
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		p = new Progress(500, "lines written...");
		for(String s : tlds.keySet())
		{
			writer.write(s + "\t" + tlds.get(s) + "\n");
			p.IncrementAndPrint();
		}
		writer.close();
	}
}
