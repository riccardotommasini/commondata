package org.webdatacommons.webgraph.process.pajek;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.jasper.tagplugins.jstl.core.Remove;

public class RemoveLinks {

	public static void main(String[] args) throws NumberFormatException, IOException
	{
		if(args.length!=4)
		{
			System.out.println("Parameters:");
			System.out.println("1. Pajek .net file");
			System.out.println("2. In-degree .clu file");
			System.out.println("3. Output .net file");
			System.out.println("4. In degree threshold");
			return;
		}
		
		RemoveLinks rl = new RemoveLinks();
		rl.RunRemoveLinks(args[0], args[1], args[2], Integer.parseInt(args[3]));
	}
	
	public void RunRemoveLinks(String pajek, String in_degree, String output, Integer RemoveInDegree) throws IOException
	{
		BufferedReader net = new BufferedReader(new FileReader(new File(pajek)));
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(output)));
		
		System.out.println("Copying *Vertices ...");
		// copy *Vertices part
		String line;
		while((line = net.readLine()) !=  null)
		{
			if(!line.startsWith("*Arcs"))
				out.write(line + "\n");
			else
				break;
		}
		
		System.out.println("Reading in-degrees ...");
		// read in-degrees
		BufferedReader in = new BufferedReader(new FileReader(new File(in_degree)));
		// skip header
		in.readLine();
		// read in-degree
		ArrayList<Integer> ind = new ArrayList<Integer>();
		while((line = in.readLine()) != null)
		{
			ind.add(Integer.parseInt(line));
		}
		in.close();
		
		System.out.println("Processing arcs ...");
		int cnt=0;
		out.write("*Arcs");
		// process arcs
		while((line = net.readLine()) != null)
		{
			String[] values = line.split("\\s");
			
			int to = Integer.parseInt(values[1])-1;
			
			if(ind.get(to)<RemoveInDegree)
				out.write(line + "\n");
			
			if(++cnt % 1000000 == 0)
				System.out.println("\t" + cnt + " arcs processed.");
		}
		net.close();
		out.close();
		
		System.out.println("Done.");
	}
	
}
