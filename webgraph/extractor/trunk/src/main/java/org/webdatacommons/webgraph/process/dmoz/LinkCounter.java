package org.webdatacommons.webgraph.process.dmoz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.webdatacommons.webgraph.process.Edge;

import de.uni_mannheim.informatik.dws.dwslib.util.Progress;

public class LinkCounter {

	public static void main(String[] args)
	{
		if(args.length<4)
		{
			usage();
			return;
		}
		
		if(args[0].equals("count"))
		{
			if(args.length!=4)
			{
				usage();
				return;
			}
			count(args[1], args[2], args[3]);
		}
		else if(args[0].equals("distribution"))
		{
			if(args.length!=5)
			{
				usage();
				return;
			}
			distribution(args[1], args[2], args[4], args[3]);
		}
	}
	
	private static void usage()
	{
		System.out.println("Parameters:");
		System.out.println("1. count|distribution");
		System.out.println("2. node id directory for categories");
		System.out.println("   (file names should be the category names,");
		System.out.println("    with whitespaces replaces by a '_')");
		System.out.println("3. path to edge file");
		System.out.println("4. path to output file");
		System.out.println("");
		System.out.println("Additional Parameters for 'distribution'");
		System.out.println("5. path to index file");
	}
		
 	public static void count(String nodeIds, String network, String output)
	{
		LinkCounter lc = new LinkCounter();
		
		/* top categories */
		String[] idLists = new String[]
		{
				"adult",
				"arts",
				"business",
				"computers",
				"games",
				"health",
				"home",
				"kids_and_teens",
				"news",
				"recreation",
				"reference",
				"science",
				"shopping",
				"society",
				"sports"
		};
		/**/
		
		
		for(int i=0;i<idLists.length;i++)
			//idLists[i] = "E:\\DMOZ\\content\\top topics - pld\\" + idLists[i] + "_ids";
			idLists[i] = nodeIds + idLists[i];
		
		//String network = "E:\\WGC Network\\pld\\network\\network";
		//String output = "E:\\DMOZ\\unique_linkage_pld.tab";
		
		try {
			lc.CountLinks(idLists, network, output);
			
			System.out.println("Done.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void distribution(String nodeIds, String network, String index, String output)
	{
		String[] labels = new String[]
		{
				"adult",
				"arts",
				"business",
				"computers",
				"games",
				"health",
				"home",
				"kids_and_teens",
				"news",
				"recreation",
				"reference",
				"science",
				"shopping",
				"society",
				"sports"
		};
		
		String[] idLists = new String[labels.length];
		//String[] output = new String[idLists.length];
		
		for(int i=0;i<idLists.length;i++)
		{
			//idLists[i] = "E:\\DMOZ\\content\\top topics - pld\\" + labels[i] + "_ids";
			idLists[i] = nodeIds + labels[i];
		}
		
		//String output = "E:\\DMOZ\\distribution_pld_unique.tab";
		//String network = "E:\\WGC Network\\pld\\network\\network";
		//String index = "E:\\WGC Network\\pld\\index\\index-00000";
		
		LinkCounter lc = new LinkCounter();
		
		try {
			lc.LinkDistribution(idLists, labels, network, index, output);
			System.out.println("done.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void LinkDistribution(String[] idLists, String[] labels, String network, String index, String output) throws IOException
	{
		// for each categorized node
		// sum in- & out-links for each category
		
		if(idLists.length!=labels.length)
		{
			System.out.println("idLists and label Arrays must be of equal size!");
			return;
		}
		
		String line;
		
		Progress p = new Progress(1000000, "lines read (index) ...");
		// read index
		BufferedReader reader = new BufferedReader(new FileReader(new File(index)));
		ArrayList<String> indexMap = new ArrayList<String>(42889800);
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			indexMap.add(values[0]);
			//indexMap.put(Integer.parseInt(values[1]), values[0]);
			p.IncrementAndPrint();
		}
		reader.close();
		
		// cat id -> node id -> cat2 id -> count
		TreeMap<Integer, TreeMap<Integer, ArrayList<TreeMap<Integer,Integer>>>> map = new TreeMap<Integer, TreeMap<Integer,ArrayList<TreeMap<Integer,Integer>>>>();
		
		
		int IN=0, OUT=1;
		
		p = new Progress(10000, "ids read ...");
		// read id-lists and build map
		for(int cat = 0; cat < idLists.length; cat++)
		{
			TreeMap<Integer, ArrayList<TreeMap<Integer,Integer>>> nodes = new TreeMap<Integer, ArrayList<TreeMap<Integer,Integer>>>();
			map.put(cat, nodes);

			reader = new BufferedReader(new FileReader(new File(idLists[cat])));
			
			while((line = reader.readLine()) != null)
			{
				int i = Integer.parseInt(line);
				
				ArrayList<TreeMap<Integer,Integer>> links = new ArrayList<TreeMap<Integer,Integer>>();
				// in-links
				TreeMap<Integer, Integer> inlinks = new TreeMap<Integer, Integer>();
				links.add(inlinks);
				// out-links
				TreeMap<Integer, Integer> outlinks = new TreeMap<Integer, Integer>();
				links.add(outlinks);
				
				for(int cat2 = 0; cat2 < idLists.length; cat2++)
				{
					inlinks.put(cat2, 0);
					outlinks.put(cat2, 0);
				}
				
				nodes.put(i, links);
				
				p.IncrementAndPrint();
			}
			
			reader.close();
		}
		
		// read network and count links
		p = new Progress(100000, "edges processed ...");
		reader = new BufferedReader(new FileReader(network));
		
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			int id1 = Integer.parseInt(values[0]);
			int id2 = Integer.parseInt(values[1]);
			int c1=-1,c2=-1;
			boolean multipleCategories = false;
			
			for(int cat = 0; cat < idLists.length; cat++)
			{
				if(map.get(cat).containsKey(id1))
				{
					// from category cat
					for(int cat2 = 0; cat2 < idLists.length; cat2++)
					{
						if(map.get(cat2).containsKey(id2))
						{
							// to category cat2
							if(c1!=-1)
							{
								multipleCategories = true;
								break;
							}
							
							c1 = cat;
							c2 = cat2;
						}
					}
				}
				
				if(multipleCategories)
					break;
			}
			
			if(!multipleCategories && c1!=-1)
			{
				TreeMap<Integer, Integer> link;
				
				// out-link
				link = map.get(c1).get(id1).get(OUT);
				link.put(c2, link.get(c2)+1);
				
				// in-link
				link = map.get(c2).get(id2).get(IN);
				link.put(c1, link.get(c1)+1);
			}
			
			p.IncrementAndPrint();
		}
		
		reader.close();
		
		// write output
		p = new Progress(100000, "lines written ...");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		StringBuilder sb = new StringBuilder();
		sb.append("node_id\turl\tcategory");
		for(int cat = 0; cat < idLists.length; cat++)
		{
			sb.append("\t");
			sb.append(labels[cat] + "-in");
		}
		for(int cat = 0; cat < idLists.length; cat++)
		{
			sb.append("\t");
			sb.append(labels[cat] + "-out");
		}
		sb.append("\n");
		writer.write(sb.toString());
		sb.setLength(0);
		
		for(int cat = 0; cat < idLists.length; cat++)
		{
			
			TreeMap<Integer, ArrayList<TreeMap<Integer,Integer>>> nodes = map.get(cat);
			
			for(int node : nodes.keySet())
			{
				sb.append(node);
				sb.append("\t");
				sb.append(indexMap.get(node));
				sb.append("\t");
				sb.append(labels[cat]);
				
				TreeMap<Integer, Integer> links = nodes.get(node).get(IN);
				// in-links
				for(int link=0; link < links.size(); link++)
				{
					sb.append("\t");
					sb.append(links.get(link));
				}
				
				// out-links
				links = nodes.get(node).get(OUT);
				for(int link=0; link < links.size(); link++)
				{
					sb.append("\t");
					sb.append(links.get(link));
				}
				
				sb.append("\n");
				writer.write(sb.toString());
				sb.setLength(0);
				p.IncrementAndPrint();
			}
		}
		writer.close();
		
	}
	
	public void CountLinks(String[] idLists, String network, String output) throws IOException
	{
		// partitions holds the ids for each category
		ArrayList<TreeSet<Integer>> partitions = new ArrayList<TreeSet<Integer>>();
		// result stores the number of links between two categories
		TreeMap<Edge, Integer> result = new TreeMap<Edge, Integer>();
		
		String line;
		Progress p;
		BufferedReader reader;
		
		// read ids for each partition (category)
		for(int i=0; i<idLists.length;i++)
		{
			p = new Progress(10000, "keys read (" + idLists[i] + ") ...");
			
			reader = new BufferedReader(new FileReader(new File(idLists[i])));
			partitions.add(new TreeSet<Integer>());
			
			while((line = reader.readLine())!=null)
			{
				partitions.get(i).add(Integer.parseInt(line));
				p.IncrementAndPrint();
			}
			
			reader.close();
		}
		
		reader = new BufferedReader(new FileReader(new File(network)));
		p = new Progress(1000000, "edges processed ...");
		
		// Process network
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			
			int i1,i2;
			Edge e;
			
			i1 = Integer.parseInt(values[0]);
			i2 = Integer.parseInt(values[1]);
			
			e = null;
			boolean multipleCategories=false;
			// count all links for all categories
			for(int i=0;i<idLists.length;i++)
			{
				if(partitions.get(i).contains(i1))
				{
					// Category i contains "from" node
					
					//boolean hasMatch = false;
					for(int j=0;j<idLists.length;j++)
					{
						if(partitions.get(j).contains(i2))
						{
							// Category j contains "to" node
							
							//hasMatch = true;
							
							// more than one category for at least one node -> ignore
							if(e!=null)
							{
								multipleCategories=true;
								break;
							}
							
							// link from partition i to partition j
							e = new Edge(i,j);
							
							/*if(!result.containsKey(e))
								result.put(e, 1);
							else
								result.put(e, result.get(e)+1);*/
						}
					}
					
					/*if(hasMatch)
					{
						// total number of edges leaving a category
						edge edge_count = new edge(i, idLists.length);
						if(!result.containsKey(edge_count))
							result.put(edge_count, 1);
						else
							result.put(edge_count, result.get(edge_count)+1);
					}*/
					
					if(multipleCategories)
						break;
				}
			}
			
			if(!multipleCategories && e!=null)
			{
				if(!result.containsKey(e))
					result.put(e, 1);
				else
					result.put(e, result.get(e)+1);
			}
			
			p.IncrementAndPrint();
		}
		reader.close();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		p = new Progress(1000, "lines written ...");
		
		for(Edge e : result.keySet())
		{
			writer.write(e.from + "\t" + e.to + "\t" + result.get(e) + "\n");
			p.IncrementAndPrint();
		}
		writer.close();
	}
}
