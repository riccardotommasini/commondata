package org.webdatacommons.webgraph.process.pajek;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import nl.peterbloem.powerlaws.Discrete;
import nl.peterbloem.powerlaws.PowerLaw.Fit;
import de.uni_mannheim.informatik.dws.dwslib.util.Progress;
public class PartitionSummary {

	public static void main(String[] args)
	{
		if(args.length==0)
		{
			System.out.println("Parameters:");
			System.out.println("1. Task[summary|compare|intersection|combine|create|linkage|powerlaw]");
			System.out.println("1: summary");
			System.out.println("2. path to partition files");
			System.out.println("1: compare");
			System.out.println("2. path to first partition file");
			System.out.println("3. path to second partition file");
			System.out.println("4. path to output file");
			System.out.println("1: intersection");
			System.out.println("2. path to first partition file");
			System.out.println("3. path to second partition file");
			System.out.println("4. path to output file");
			System.out.println("5. ids of first partition (comma-separated)");
			System.out.println("6. ids of second partition (comma-separated)");
			System.out.println("1: combine");
			System.out.println("2. path to first partition file");
			System.out.println("3. path to second partition file");
			System.out.println("4. path to output file");
			System.out.println("1: create");
			System.out.println("2. path to index file");
			System.out.println("3. path to output file");
			System.out.println("4. number of vertices");
			System.out.println("1: linkage");
			System.out.println("2. path to partition file");
			System.out.println("3. path to network file");
			System.out.println("4. path to output file");
			System.out.println("1: powerlaw");
			System.out.println("2. path to partition file");
			System.out.println("3. path to output file");
			return;
		}
		
		PartitionSummary ps = new PartitionSummary();
		
		if(args[0].equals("summary"))
		{
			try {
				ps.CreateNetworkSummary(args[1]);
				System.out.println("Done.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(args[0].equals("compare"))
		{
			try {
				ps.Compare(args[1], args[2], args[3]);
				System.out.println("Done.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		else if(args[0].equals("intersection"))
		{
			try {
				int[] firstIds;
				int[] secondIds;
				
				String[] values = args[4].split(",");
				firstIds = new int[values.length];
				for(int i=0;i<values.length;i++)
					firstIds[i] = Integer.parseInt(values[i]);
				
				values = args[5].split(",");
				secondIds = new int[values.length];
				for(int i=0;i<values.length;i++)
					secondIds[i] = Integer.parseInt(values[i]);
				
				ps.Intersection(args[1], args[2], args[3], firstIds, secondIds);
				System.out.println("Done.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		else if(args[0].equals("combine"))
		{
			try {
				ps.Combine(args[1], args[2], args[3]);
				System.out.println("Done.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		else if(args[0].equals("create"))
		{
			try {
				int num = Integer.parseInt(args[3]);
				
				ps.CreateFromIndex(args[1], args[2], num);
				System.out.println("Done.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		else if(args[0].equals("linkage"))
		{
			try {
				ps.CreateLinkageSummary(args[1], args[2], args[3]);
				System.out.println("Done.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		else if(args[0].equals("powerlaw"))
		{
			try {
				ps.EstimatePowerLaw(args[1], args[2]);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void CreateFromIndex(String indexFile, String outputFile, int numVertices) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(new File(indexFile)));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
		
		writer.write("*Vertices " + numVertices + "\n");
		
		Progress p = new Progress(10000, "lines processed ...");
		
		String line;
		int index =0;
		
		while((line = reader.readLine()) != null)
		{
			int i = Integer.parseInt(line);
			
			while(index<i)
			{
				writer.write("0\n");
				index++;
			}
			
			writer.write("1\n");
			index++;
			
			p.IncrementAndPrint();
		}
		reader.close();
		
		while(index++<numVertices)
			writer.write("0\n");
		
		writer.close();
	}
	
	/*
	 * Combines two partitions
	 * output value: max(part1) * id2 + id1
	 */
	public void Combine(String firstPartition, String secondPartition, String output) throws IOException
	{
		BufferedReader reader1 = new BufferedReader(new FileReader(new File(firstPartition)));
		BufferedReader reader2 = new BufferedReader(new FileReader(new File(secondPartition)));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
	
		String line;
		int numVertices1, numVertices2;
		
		line = reader1.readLine();
		numVertices1 = Integer.parseInt(line.split(" ")[1]);
		reader2.readLine();
		numVertices2 = Integer.parseInt(line.split(" ")[1]);
		
		if(numVertices1 != numVertices2)
		{
			System.out.println("Partition dimensions do not match!");
			return;
		}
		
		int part1Max = 0;
		// Determine max. value for first partition
		while((line = reader1.readLine()) != null)
		{
			part1Max = Math.max(part1Max, Integer.parseInt(line));
		}
		reader1.close();
		reader1 = new BufferedReader(new FileReader(new File(firstPartition)));
		reader1.readLine();
		
		writer.write("*Vertices " + numVertices1 + "\n");
		Progress p = new Progress(1000000, "lines processed ...");
		
		while((line = reader1.readLine()) != null)
		{
			int id1 = Integer.parseInt(line);
			
			String line2 = reader2.readLine();
			int id2 = Integer.parseInt(line2);
			
			writer.write(((part1Max+1)*id2+id1) + "\n");
			
			p.IncrementAndPrint();
		}
		
		reader1.close();
		reader2.close();
		writer.close();
	}
	
	/*
	 * Computes the intersection of two partitions given two sets of partition-ids
	 * and outputs the index of the value in the firstId-set of the first partition for each vertex that is contained in both id-sets
	 * otherwise the output is firstIds.length
	 */
	public void Intersection(String firstPartition, String secondPartition, String output, int[] firstIds, int[]secondIds) throws IOException
	{
		BufferedReader reader1 = new BufferedReader(new FileReader(new File(firstPartition)));
		BufferedReader reader2 = new BufferedReader(new FileReader(new File(secondPartition)));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
	
		String line;
		int numVertices1, numVertices2;
		
		line = reader1.readLine();
		numVertices1 = Integer.parseInt(line.split(" ")[1]);
		reader2.readLine();
		numVertices2 = Integer.parseInt(line.split(" ")[1]);
		
		if(numVertices1 != numVertices2)
		{
			System.out.println("Partition dimensions do not match!");
			return;
		}
		
		writer.write(line + "\n");
		Progress p = new Progress(1000000, "lines processed ...");
		
		while((line = reader1.readLine()) != null)
		{
			int id1 = Integer.parseInt(line);
			
			String line2 = reader2.readLine();
			int id2 = Integer.parseInt(line2);
			
			int index1 = Arrays.binarySearch(firstIds, id1);
			if(index1>=0 && Arrays.binarySearch(secondIds, id2)>=0)
				writer.write(index1 + "\n");
			else
				writer.write(firstIds.length + "\n");
			
			p.IncrementAndPrint();
		}
		
		reader1.close();
		reader2.close();
		writer.close();
	}
	
	/*
	 * Creates a linkage summary of a partition
	 * output:
	 * from partition-id		to partition-id		number of links			
	 */
	public void CreateLinkageSummary(String partition, String network, String output) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(new File(partition)));
		reader.readLine();
		
		String line;
		Progress p = new Progress(1000000, "lines read (partition) ...");
		
		TreeMap<Integer, TreeMap<Integer, Integer>> links = new TreeMap<Integer, TreeMap<Integer,Integer>>();
		ArrayList<Integer> part = new ArrayList<Integer>();
		
		while((line = reader.readLine()) != null)
		{
			int i = Integer.parseInt(line);
			
			part.add(i);
			
			if(!links.containsKey(i))
				links.put(i, new TreeMap<Integer, Integer>());
			p.IncrementAndPrint();
		}
		reader.close();
		
		reader = new BufferedReader(new FileReader(new File(network)));
		p = new Progress(1000000, "edges processed ...");
		
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			
			int i1,i2;
			i1 = part.get(Integer.parseInt(values[0]));
			i2 = part.get(Integer.parseInt(values[1]));
			
			if(!links.get(i1).containsKey(i2))
				links.get(i1).put(i2, 1);
			else
				links.get(i1).put(i2, links.get(i1).get(i2)+1);
			
			p.IncrementAndPrint();
		}
		
		reader.close();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		p = new Progress(10000, "lines written ...");
		
		for(int i1 : links.keySet())
		{
			for(int i2 : links.get(i1).keySet())
			{
				writer.write(i1 + "\t" + i2 + "\t" + links.get(i1).get(i2) + "\n");
				p.IncrementAndPrint();
			}
		}
		writer.close();
	}
	
	/*
	 * Compares two partitions
	 * output:
	 * for each partition from firstPartition
	 * and for each partition from secondPartition
	 * first-id		first-size		second-id		second-count	
	 */
	public void Compare(String firstPartition, String secondPartition, String output) throws IOException
	{
		BufferedReader reader1 = new BufferedReader(new FileReader(new File(firstPartition)));
		BufferedReader reader2 = new BufferedReader(new FileReader(new File(secondPartition)));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		
		String line;
		Progress p = new Progress(1000000, "lines processed ...");
		
		TreeMap<Integer, Integer> firstSizes = new TreeMap<Integer, Integer>();
		TreeMap<Integer, TreeMap<Integer, Integer>> secondParts = new TreeMap<Integer, TreeMap<Integer,Integer>>();
		
		line = reader1.readLine();
		int numVertices1 = Integer.parseInt(line.split(" ")[1]);
		line = reader2.readLine();
		int numVertices2 = Integer.parseInt(line.split(" ")[1]);
		
		if(numVertices1!=numVertices2)
		{
			System.out.println("Partition dimensions do not match!");
			return;
		}
		
		while((line = reader1.readLine()) != null)
		{
			int i1 = Integer.parseInt(line);
			
			if(!firstSizes.containsKey(i1))
				firstSizes.put(i1, 1);
			else
				firstSizes.put(i1, firstSizes.get(i1)+1);
			
			TreeMap<Integer, Integer> second;
			if(!secondParts.containsKey(i1))
			{
				second = new TreeMap<Integer, Integer>();
				secondParts.put(i1, second);
			}
			else
				second = secondParts.get(i1);
				
			line = reader2.readLine();
			
			int i2 = Integer.parseInt(line);
			
			if(!second.containsKey(i2))
				second.put(i2, 1);
			else
				second.put(i2, second.get(i2) + 1);
			
			p.IncrementAndPrint();
		}
		reader1.close();
		reader2.close();
		
		p = new Progress(1000000, "lines written ...");
		StringBuilder sb = new StringBuilder();
		for(int i : firstSizes.keySet())
		{
			for(int i2 : secondParts.get(i).keySet())
			{
				sb.append(i);
				sb.append("\t");
				sb.append(firstSizes.get(i));
				sb.append("\t");
				sb.append(i2);
				sb.append("\t");
				sb.append(secondParts.get(i).get(i2));
				
				writer.write(sb.toString() + "\n");
				sb.setLength(0);
				p.IncrementAndPrint();
			}
		}
		
		writer.close();
	}
	
	public void CreateNetworkSummary(String directory) throws Exception
	{
		if(!directory.endsWith("/") && !directory.endsWith("\\"))
			directory += "/";
		
		// Degrees
		// output: degree	count
		CreatePartitionDistribution(directory + "degree.clu", directory + "degree.dist");
		CreatePartitionDistribution(directory + "in_degree.clu", directory + "in_degree.dist");
		CreatePartitionDistribution(directory + "out_degree.clu", directory + "in_degree.dist");
		
		// Components
		// output: partition	size
		CreatePartitionDistribution(directory + "wcc.clu", directory + "wcc.sizes");
		// output: size	count
		CreateComponentDistribution(directory + "wcc.sizes", directory + "wcc.dist");
		CreatePartitionDistribution(directory + "scc.clu", directory + "scc.sizes");
		CreateComponentDistribution(directory + "scc.sizes", directory + "scc.dist");
		
		// Bow-Tie
		CreatePartitionDistribution(directory + "bowtie.clu", directory + "bowtie.dist");
	}
	
	public void CreatePartitionDistribution(String partitionFile, String outputFile) throws Exception
	{
		System.out.println("*****************************************");
		System.out.println("Partition Distribution");
		System.out.println(partitionFile);
		System.out.println("*****************************************");
		
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(partitionFile)));
		
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
		
		String line;
		Progress p = new Progress(100000, "lines read ...");
		
		// parse header
		// *Vertices 8913901
		line = reader.readLine();
		int numVertices = Integer.parseInt(line.split(" ")[1]);
		
		while((line = reader.readLine()) != null)
		{
			int value = Integer.parseInt(line);
			
			if(map.containsKey(value))
				map.put(value, map.get(value)+1);
			else
				map.put(value, 1);
			
			p.IncrementAndPrint();
		}
		reader.close();
		
		if(numVertices != p.count)
		{
			System.out.println("Invalid partition file!");
			System.out.println("Vertices expected: " + numVertices);
			System.out.println("Vertices read: " + p.count);
			throw new Exception("Invalid file!");
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
		p = new Progress(100000, "lines written ...");
		
		for(int key : map.keySet())
		{
			writer.write(key + "\t" + map.get(key) + "\n");
			
			p.IncrementAndPrint();
		}
		
		writer.close();
	}
	
	public void CreateComponentDistribution(String partitionDistributionFile, String outputFile) throws NumberFormatException, IOException
	{
		System.out.println("*****************************************");
		System.out.println("Component Distribution");
		System.out.println(partitionDistributionFile);
		System.out.println("*****************************************");
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(partitionDistributionFile)));
		
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
		
		String line;
		Progress p = new Progress(100000, "lines read ...");
		
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			int value = Integer.parseInt(values[1]);
			
			if(map.containsKey(value))
				map.put(value, map.get(value)+1);
			else
				map.put(value, 1);
			
			p.IncrementAndPrint();
		}
		
		reader.close();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
		p = new Progress(100000, "lines written ...");
		
		for(int key : map.keySet())
		{
			writer.write(key + "\t" + map.get(key) + "\n");
			
			p.IncrementAndPrint();
		}
		
		writer.close();
	}

	public void EstimatePowerLaw(String distribution, String output) throws NumberFormatException, IOException
	{
		// Read partition
		BufferedReader reader = new BufferedReader(new FileReader(new File(distribution)));
		String line;
		ArrayList<Integer> values = new ArrayList<Integer>();
		//reader.readLine();
		
		Progress p = new Progress(1000000, "lines read ...");
		
		while((line = reader.readLine()) != null)
		{
			String[] parts = line.split("\t");
			values.add(Integer.parseInt(parts[1]));
			p.IncrementAndPrint();
		}
		
		reader.close();
		
		System.out.println("Estimating power law ...");
		
		// http://tuvalu.santafe.edu/~aaronc/powerlaws/
		
		Fit<Integer, Discrete> f = Discrete.fit(values);
		
		Discrete fit = f.fit();
		
		double exp = fit.exponent();
		int xMin = fit.xMin();
		/*
		System.out.println("Checking significance ...");
		
		double significance = fit.significance(values, 0.01);
		*/
		System.out.println("Writing output ...");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		
		writer.write("Power Law Estimation for " + distribution + "\n\n");
		writer.write("Exponent: " + exp + "\n");
		writer.write("x-min: " + xMin + "\n");
		//writer.write("Significance: " + significance + " -> " + (significance<0.01?"reject":"do not reject") + "\n");
		
		writer.close();
		
		/* 
		 * The calculation of the significance is by far the most expensive process. For a 
large data set, the process an easily take hours. Note that this is a 
significance test for the the power law hypothesis (rather than the null 
hypothesis) so that _high_ values mean that the power law is a good fit. Clauset 
et al. suggest that for values below 0.01 the power law hypothesis should be 
rejected.  
		 */
	}
	
}
