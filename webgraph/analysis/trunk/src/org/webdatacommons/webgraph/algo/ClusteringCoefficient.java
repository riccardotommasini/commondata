package org.webdatacommons.webgraph.algo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;

/**
 * Calculates the Clustering Coefficent for the specified nodes.
 * 
 * 
 * @author Oliver
 *
 */

public class ClusteringCoefficient {

	ImmutableGraph graph;
	ImmutableGraph transpose;
	ProgressLogger pl;
	public float[] CC;
	int numThreads;
	
	public static void main(String[] args)
	{
		if(args.length!=3)
		{
			System.out.println("Parameters:");
			System.out.println("1. symmetric graph");
			System.out.println("2. Id-file directory");
			System.out.println("3. number of threads to use");
			//System.out.println("3. output file");
			return;
		}
		
		if(!new File(args[1]).isDirectory())
		{
			System.out.println("Parameter 2 is not a directory!");
			return;
		}
		
		int numThreads = Integer.parseInt(args[2]);
		
		ProgressLogger pl = new ProgressLogger();
		
		BVGraph graph=null;
		try {
			graph = BVGraph.loadMapped(args[0], pl);
		} catch (IOException e) {
			System.out.println("Error loading graph!");
			e.printStackTrace();
			return;
		}
		
		File dir = new File(args[1]);
		
		for(String f : dir.list())
		{
			try
			{
				ClusteringCoefficient cc = new ClusteringCoefficient(graph, pl, numThreads);
				cc.Run(args[1] + f, args[1] + f + "_cc.tab");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public ClusteringCoefficient(final ImmutableGraph symmetricGraph, final ProgressLogger pl, int numThreads)
	{
		this.graph = symmetricGraph;
		this.pl = pl;
		this.numThreads = numThreads;
	}
	
	public void Run(String vertexList, String output) throws IOException, InterruptedException
	{
		HashSet<Integer> ids = new HashSet<Integer>();
		
		readIntCollection(vertexList, ids);
		
		CC = new float[graph.numNodes()];
		
		final HashMap<Integer, ArrayList<Integer>> vertexLinks = new HashMap<Integer, ArrayList<Integer>>();
		//final HashMap<Integer, HashSet<Integer>> linkMap = new HashMap<Integer, HashSet<Integer>>();
		
		pl.start("Loading links");
		pl.info = "Load links: " + vertexList;
		pl.expectedUpdates = ids.size();
		
		new ParallelForEach() {
			
			@Override
			public void loop(Object var) {
				int node = (Integer) var;
				
				// get all neighbours
				int degree = graph.outdegree(node);
				int cnt=0;
				LazyIntIterator it = graph.successors(node);
				int link = it.nextInt();
				ArrayList<Integer> links = new ArrayList<Integer>();
				
				while(link!=-1&&cnt<degree)
				{
					links.add(link);
					link = it.nextInt();
					cnt++;
				}
				//Collections.sort(links);
				vertexLinks.put(node, links);
				
				//int linkCnt=0;
				//int maxLinks = degree * (degree-1);
				
				// load each neighbour's links
				for(int neighbour : links)
				{
					if(!vertexLinks.containsKey(neighbour))
					{
						ArrayList<Integer> nLinks = new ArrayList<Integer>();
						degree = graph.outdegree(neighbour);
						cnt=0;
						it = graph.successors(neighbour);
						link = it.nextInt();
						while(link!=-1&&cnt<degree)
						{
							nLinks.add(link);
							link = it.nextInt();
							cnt++;
						}
						
						//Collections.sort(nLinks);
						vertexLinks.put(neighbour, nLinks);
					}
				}
				
				pl.update();
			}
		}.Start(ids, numThreads);
		
		pl.done();
		
		pl.start("Computing Clustering Coefficient");
		pl.info = "Clustering Coefficient: " + vertexList;
		pl.expectedUpdates = ids.size();
		
		new ParallelForEach() {
			
			@Override
			public void loop(Object var) {
				int node = (Integer) var;
				
				ArrayList<Integer> links = vertexLinks.get(node);
				double linkCnt=0;
				double maxLinks = (double)links.size() * ((double)links.size() - 1);
				
				// for all neighbours
				for(int neighbour : links)
				{
					// get the neighbour's neighbours
					ArrayList<Integer> nLinks = vertexLinks.get(neighbour);
					
					// determine intersection count
					linkCnt += GetOutlinkIntersectionCount(links, nLinks);
					
					
					/*for(int nLink : links)
						if(nLinks.contains(nLink))
							linkCnt++;*/
					
					/*for(int nLink : linkMap.get(neighbour))
					{
						if(links.contains(nLink))
							linkCnt++;
					}*/
				}
				
				CC[node] = (float)(linkCnt / maxLinks);
				
				pl.update();
			}
		}.Start(ids, numThreads);
		
		pl.done();
		pl.start("Writing output");
		pl.info = "Write output";
		pl.expectedUpdates = ids.size();
		
		// Write output
		BufferedWriter w = new BufferedWriter(new FileWriter(new File(output)));
		
		for(int i : ids)
		{
			w.write(i + "\t" + CC[i] + "\n");
			pl.update();
		}
		
		w.close();
		
		pl.done();
	}
	
	protected long GetOutlinkIntersectionCount(ArrayList<Integer> links1, ArrayList<Integer> links2)
	{
		long cnt=0;
		
		int i1=0,i2=0;
		
		ArrayList<Integer> l1, l2;
		
		l1 = links1;
		l2 = links2;
		
		while(i1<l1.size() && i2<l2.size())
		{
			int comp = l1.get(i1).compareTo(l2.get(i2)); 
					
			if(comp<0)
				i1++;
			else if(comp>0)
				i2++;
			else
			{
				i1++;
				i2++;
				cnt++;
			}
		}
		
		/*for(int i : outlinks.get(node1))
			if(outlinks.get(node2).contains(i))
				cnt++;*/
		
		return cnt;
	}
	
	private void readIntCollection(String filename, Collection<Integer> col) throws IOException
	{
		BufferedReader r = InputUtil.getBufferedReader(new File(filename));
		
		String line;
		
		int cnt=0;
		while((line = r.readLine()) != null)
		{
			int i=-1;
			try
			{
				i = Integer.parseInt(line);
				col.add(i);
			}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage());
			}
		}
		r.close();
	}
}
