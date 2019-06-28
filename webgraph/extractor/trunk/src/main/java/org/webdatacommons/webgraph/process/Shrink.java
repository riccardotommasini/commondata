package org.webdatacommons.webgraph.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

import de.uni_mannheim.informatik.dws.dwslib.util.Progress;


public class Shrink {

	public static void main(String[] args)
	{
		//ShrinkBowTies();
		//ShrinkBackbone();
		//ShrinkTldBowTies_BowTie();
		//ShrinkDmoz();
		ShrinkPldNetwork_TldBowTies_BowTie();
	}

	public static void ShrinkPldNetwork_TldBowTies_BowTie()
	{
		Shrink s = new Shrink();
		
		String network = "E:\\WGC Network\\pld\\network\\network";
		String partition = "E:\\WGC Network\\pld\\pld_network_tld_bowties\\tldbowties_bowtie.clu";
		String output = "E:\\WGC Network\\pld\\pld_network_tld_bowties\\tldbowties_bowtie";
		
		String[] labels = new String[6 * 11 * 6];
	
		String[] labels1 = new String[] {
				"co.uk",
				"com",
				"com.br",
				"de",
				"info",
				"it",
				"net",
				"nl",
				"org",
				"others",
				"ru"
		};
		
		String[] labels2 = new String[] {
			"OTHERS",
			"LSCC",
			"IN",
			"OUT",
			"TUBES",
			"TENDRILS"
		};
		
		
		int part1Max = labels1.length*labels2.length;
		
		for(int i=0;i<labels1.length;i++) // all tld-networks
		{
			for(int j=0;j<labels2.length;j++) // tld-bowties
			{
				for(int k=0;k<labels2.length;k++) // notld-bowtie
				{
					labels[part1Max * k + (i*labels2.length) + j] = labels1[i] + "_" + labels2[j] + "_" + labels2[k];
				}
			}
		}
	
		try {
			s.ShrinkSingle(network, partition, output, labels);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ShrinkTldBowTies_BowTie()
	{
		Shrink s = new Shrink();
		
		String network = "E:\\WGC Network\\pld\\no_tld\\network";
		String partition = "E:\\WGC Network\\pld\\no_tld_network_tld_bowties\\tldbowties_bowtie.clu";
		String output = "E:\\WGC Network\\pld\\no_tld_network_tld_bowties\\tldbowties_bowtie";
		
		String[] labels = new String[6 * 11 * 6];
	
		String[] labels1 = new String[] {
				"co.uk",
				"com",
				"com.br",
				"de",
				"info",
				"it",
				"net",
				"nl",
				"org",
				"others",
				"ru"
		};
		
		String[] labels2 = new String[] {
			"OTHERS",
			"LSCC",
			"IN",
			"OUT",
			"TUBES",
			"TENDRILS"
		};
		
		
		int part1Max = labels1.length*labels2.length;
		
		for(int i=0;i<labels1.length;i++) // all tld-networks
		{
			for(int j=0;j<labels2.length;j++) // tld-bowties
			{
				for(int k=0;k<labels2.length;k++) // notld-bowtie
				{
					labels[part1Max * k + (i*labels2.length) + j] = labels1[i] + "_" + labels2[j] + "_" + labels2[k];
				}
			}
		}
	
		try {
			s.ShrinkSingle(network, partition, output, labels);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ShrinkBackbone()
	{
		Shrink s = new Shrink();
		
		String network = "E:\\WGC Network\\pld\\network\\network";
		String partition = "E:\\WGC Network\\pld\\backbone.clu";
		String output = "E:\\WGC Network\\pld\\backbone";
		String[] labels = new String[] {
				"co.uk",
				"com",
				"com.br",
				"de",
				"info",
				"it",
				"net",
				"nl",
				"org",
				"others",
				"ru",
				"removed"
		};
		try {
			s.ShrinkSingle(network, partition, output, labels);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void TestShrink()
	{
		Shrink s = new Shrink();
		
		String network = "E:\\WGC Network\\tld\\it\\network";
		String partition = "E:\\WGC Network\\tld\\it\\bowtie.clu";
		String output = "E:\\WGC Network\\tld\\it\\bowtie";
		String[] labels = new String[] {
			"OTHERS",
			"LSCC",
			"IN",
			"OUT",
			"TUBES",
			"TENDRILS"
		};
		try {
			s.ShrinkSingle(network, partition, output, labels);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ShrinkDmoz()
	{
		Shrink s = new Shrink();
		
		String[] partitions = new String[13];
		
		String[] labels = new String[]
		{
				"arts",
				"business",
				"colleges_and_universities",
				"computers",
				"games",
				"health",
				"home",
				"news",
				"recreation",
				"science",
				"shopping",
				"society",
				"sports"
		};
		
		for(int i=0;i<partitions.length;i++)
			partitions[i] = "E:\\DMOZ\\" + labels[i] + ".clu";
		
		String[] newLabels;
		try {
			newLabels = s.MergeOverlappingBinaryPartitions(partitions, labels, "E:\\DMOZ\\categories.clu");
			s.ShrinkSingle("E:\\WGC Network\\pld\\network\\network", "E:\\DMOZ\\categories.clu", "E:\\DMOZ\\categories.net", newLabels);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void ShrinkBowTies()
	{
		Shrink s = new Shrink();
		
		String network = "E:\\WGC Network\\pld\\network\\network";
		String[] partitions = new String[] {
				"E:\\WGC Network\\tld\\co.uk\\bowtie.clu",
				"E:\\WGC Network\\tld\\com\\bowtie.clu",
				"E:\\WGC Network\\tld\\com.br\\bowtie.clu",
				"E:\\WGC Network\\tld\\de\\bowtie.clu",
				"E:\\WGC Network\\tld\\info\\bowtie.clu",
				"E:\\WGC Network\\tld\\it\\bowtie.clu",
				"E:\\WGC Network\\tld\\net\\bowtie.clu",
				"E:\\WGC Network\\tld\\nl\\bowtie.clu",
				"E:\\WGC Network\\tld\\org\\bowtie.clu",
				"E:\\WGC Network\\tld\\others_co.uk_com_com.br_de_info_it_net_nl_org_ru\\bowtie.clu",
				"E:\\WGC Network\\tld\\ru\\bowtie.clu"
		};
		
		String[] transformation = new String[] {
				"E:\\WGC Network\\tld\\co.uk\\transformation",
				"E:\\WGC Network\\tld\\com\\transformation",
				"E:\\WGC Network\\tld\\com.br\\transformation",
				"E:\\WGC Network\\tld\\de\\transformation",
				"E:\\WGC Network\\tld\\info\\transformation",
				"E:\\WGC Network\\tld\\it\\transformation",
				"E:\\WGC Network\\tld\\net\\transformation",
				"E:\\WGC Network\\tld\\nl\\transformation",
				"E:\\WGC Network\\tld\\org\\transformation",
				"E:\\WGC Network\\tld\\others_co.uk_com_com.br_de_info_it_net_nl_org_ru\\transformation",
				"E:\\WGC Network\\tld\\ru\\transformation"
		};

		String[] labels = new String[] {
				"co.uk",
				"com",
				"com.br",
				"de",
				"info",
				"it",
				"net",
				"nl",
				"org",
				"others",
				"ru"
		};
		
		String output = "E:\\WGC Network\\pld\\tld_bowties";
		
		try {
			s.ShrinkMultiple(network, partitions, transformation, labels, output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Shrinks a network
	 */
	public void ShrinkSingle(String network, String partition, String output, String[] labels) throws IOException
	{
		if(!new File(partition).exists())
		{
			System.out.println("partition file does not exist!");
			return;
		}
		
		// Edge transformation requires random-access, so read whole partition file into memory
		BufferedReader reader = new BufferedReader(new FileReader(new File(partition)));
		
		String line = reader.readLine();
		int numVertices = Integer.parseInt(line.split(" ")[1]);
		
		int[] part = new int[numVertices];
		TreeMap<Integer, Integer> vertexCount = new TreeMap<Integer, Integer>();
		
		Progress p = new Progress(1000000,"partition values read ...");
		int index=0;
		
		System.out.println("Reading partition into memory ...");
		while((line = reader.readLine()) != null)
		{
			int i = Integer.parseInt(line);
			part[index++] = i;
			if(!vertexCount.containsKey(i))
				vertexCount.put(i, 1);
			else
				vertexCount.put(i, vertexCount.get(i)+1);
			p.IncrementAndPrint();
		}
		reader.close();
		
		// transform edges
		System.out.println("Shrinking ...");
		reader = new BufferedReader(new FileReader(new File(network)));
		BufferedWriter edgeWriter = new BufferedWriter(new FileWriter(new File(output + "_remainingEdges.edges")));
		TreeMap<Edge, Integer> edges = new TreeMap<Edge, Integer>();
		p = new Progress(1000000, "edges processed ...");
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			int i1, i2;
			
			i1 = part[Integer.parseInt(values[0])];
			i2 = part[Integer.parseInt(values[1])];
			
			if(i1!=i2)
			{
				Edge e = new Edge(i1,i2);
				//String edge = i1 + "\t" + i2;
				if(!edges.containsKey(e))
					edges.put(e, 1);
				else
					edges.put(e, edges.get(e)+1);
				
				// Write all edges that are used to build the new network
				//edgeWriter.write(line + "\n");
			}
			
			p.IncrementAndPrint();
		}
		reader.close();
		edgeWriter.close();
		
		System.out.println("Writing output ...");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output + ".net")));
		BufferedWriter sizes = new BufferedWriter(new FileWriter(new File(output + "_sizes.clu")));
		p = new Progress(1000, "lines written ...");
		
		writer.write("*Vertices " + vertexCount.size() + "\n");
		sizes.write("*Vertices " + vertexCount.size() + "\n");
		
		// renumber vertices if needed
		TreeMap<Integer, Integer> idMap = new TreeMap<Integer, Integer>();
		
		// Add 1: Partitions-IDs start at 0, but Vertex-IDs must start at 1
		int newId=0;
		for(int i=0;i<labels.length;i++)
		{
			// only create vertices that actually exist
			if(vertexCount.containsKey(i))
			{
				idMap.put(i, newId);
				writer.write((newId+1) + " \"" + labels[i] + "\"\n");
				sizes.write(vertexCount.get(i) + "\n");
				newId++;
			}
			
			p.IncrementAndPrint();
		}
		sizes.close();

		writer.write("*Arcs\n");
		for(Edge e : edges.keySet())
		{
			writer.write((idMap.get(e.from)+1) + " " + (idMap.get(e.to)+1) + " " + edges.get(e) + "\n");
			p.IncrementAndPrint();
			
		}
		writer.close();
	}
	
	/*
	 * Shrinks a network using multiple partitions
	 */
	public void ShrinkMultiple(String network, String[] partitions, String[] transformation, String[] labels, String output) throws Exception
	{
		// Merge all partitions
		String[] vertexLabels = MergePartitions(partitions, transformation, labels, output + ".clu");
		
		ShrinkSingle(network, output + ".clu", output, vertexLabels);
	}
	
	/*
	 * Merges multiple overlapping partitions
	 * 
	 */
	public String[] MergeOverlappingBinaryPartitions(String[] partitions, String[] labels, String output) throws IOException
	{
		// example for 3 partitions:
		// 000 = 0 all partitions 0
		// 001 = 1 partition1 
		// 010 = 2 partition2 
		// 011 = 3 partition1 & partition2
		// 100 = 4 partition3		
		// 101 = 5 partition1 & partition3
		// 110 = 6 partition2 & partition3
		// 111 = 7 partition1 & partition2 & partition3
		
		
		String[] newLabels = new String[(int)Math.pow(2, partitions.length)];
		
		BufferedReader[] reader = new BufferedReader[partitions.length];
		
		String line;
		int numVertices=0;
		for(int i=0;i<partitions.length;i++)
		{
			reader[i] = new BufferedReader(new FileReader(new File(partitions[i])));
			line = reader[i].readLine();
			
			int n = Integer.parseInt(line.split(" ")[1]);
			
			if(numVertices==0)
				numVertices=n;
			else
				if(numVertices != n)
				{
					System.out.println("Partition dimensions do not match!");
					return null;
				}
		}
		
		Progress p = new Progress(1000000, "lines processed ...");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		writer.write("*Vertices " + numVertices + "\n");
		
		while((line = reader[0].readLine()) != null)
		{
			int result = 0;
			int value = Integer.parseInt(line);
			
			result = value;
			
			StringBuilder sb = new StringBuilder();
			if(value!=0)
				sb.append(labels[0]);
			
			for(int i=1;i<partitions.length;i++)
			{
				line = reader[i].readLine();
				value = Integer.parseInt(line) * (int)Math.pow(2, i);
				
				if(value!=0)
				{
					if(sb.length()>0)
						sb.append(" + ");
					sb.append(labels[i]);
				}
				result += value;
			}
			
			newLabels[result] = sb.toString();
			sb.setLength(0);
			
			writer.write(result + "\n");
			p.IncrementAndPrint();
		}
		writer.close();
		
		return newLabels;
	}
	
	/*
	 * Merges multiple partition files (of sub-networks) using the transformation-index
	 * output: one partition file for the original network
	 * return value: labels for each partition
	 */
	public String[] MergePartitions(String[] partitions, String[] transformations, String[] labels, String output) throws Exception
	{
		NetworkTransformation[] t = new NetworkTransformation[transformations.length];
		BufferedReader[] p = new BufferedReader[partitions.length];
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output + ".tmp")));
		int nextId = 0;
		int[] startValue = new int[partitions.length];
		startValue[0]=0;
		int numPartitions=0;
		String line;
		
		System.out.println("Merging partitions ...");
		
		try
		{
			for(int i=0; i<transformations.length;i++)
				t[i] = new NetworkTransformation(transformations[i]);
			
			for(int i=0; i<partitions.length;i++)
			{
				p[i] = new BufferedReader(new FileReader(new File(partitions[i])));
				// ignore partition header
				p[i].readLine();
				
				Progress prg = new Progress(1000000, "lines read (" + partitions[i] + ") ...");
				int max=0;
				// determine max. value
				while((line = p[i].readLine()) != null)
				{
					max = Math.max(max, Integer.parseInt(line));
					prg.IncrementAndPrint();
				}
				
				if(i<partitions.length-1)
					startValue[i+1]=startValue[i]+max+1;
				else
					numPartitions = startValue[i]+max+1;
				
				// reset reader
				p[i].close();
				p[i] = new BufferedReader(new FileReader(new File(partitions[i])));
				p[i].readLine();
			}
			
			int completed = 0;

			// read first value of each transformation-index
			for(int i=0;i<transformations.length;i++)
				t[i].Read();
			
			Progress prg = new Progress(1000000, "Vertices processed ...");
			// until all transformation files are completely processed
			while(completed != transformations.length)
			{
				int last = nextId;
				for(int i=0;i<transformations.length;i++)
				{
					if(!t[i].IsEOF)
					{
						if(t[i].OriginalId == nextId)
						{
							// t[i] is the transformation-index that contains the next id
							// so, p[i] is the corresponding partition file
							line = p[i].readLine();
							
							// now transform the partition value
							int value = Integer.parseInt(line);
							value += startValue[i];
							
							// and write the transformed value
							writer.write(value + "\n");
							
							// read next value
							nextId++;
							if(!t[i].Read())
								completed++;
						}
					}
				}
				
				prg.IncrementAndPrint();
				// no progress was made in this iteration
				if(last==nextId)
				{
					for(int i=0;i<transformations.length;i++)
					{
						System.out.println(t[i].file + ": " + t[i].OriginalId);
					}
					throw new Exception("Error: Transformation for OriginalId " + nextId + " does not exist!");
				}
			}
		}
		finally
		{
			for(int i=0;i<p.length;i++)
				p[i].close();
			
			writer.close();
		}
		
		final int numVertices = nextId;
		// now write the output file again and include the header
		System.out.println("Writing merged partition file ...");
		FileTransformation f = new FileTransformation() {
			
			@Override
			public String transformValue(String input) {
				return input;
			}

			@Override
			public String createHeader() {
				return "*Vertices " + (numVertices);
			}
		};
		
		f.Transform(output + ".tmp", output, null);
		
		// delete temporary file
		new File(output + ".tmp").delete();
	
		// create labels
		String[] partitionLabels = new String[numPartitions];
		for(int i=0;i<partitions.length-1;i++)
		{
			for(int j=startValue[i];j<startValue[i+1];j++)
			{
				partitionLabels[j] = labels[i] + "_" + (j-startValue[i]);	
			}
		}
		
		for(int i=startValue[partitions.length-1];i<numPartitions;i++)
			partitionLabels[i] = labels[partitions.length-1] + "_" + (i-startValue[partitions.length-1]);
		
		return partitionLabels;
	}

}
