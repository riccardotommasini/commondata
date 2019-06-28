package org.webdatacommons.webgraph.big.algo;

import it.unimi.dsi.big.webgraph.BVGraph;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BFSShowPath {

	public static void main(String[] args) throws IOException
	{
		String graphFile = args[0];
		String outputFile = args[1];
		long minPath = Long.parseLong(args[2]);
		long start = Long.parseLong(args[3]);
		int threads = Integer.parseInt(args[4]);
		if (threads == 0){
			threads = Runtime.getRuntime().availableProcessors();
		}
		
		ProgressLogger pl = new ProgressLogger();
		BVGraph graph=null;
		try {
			graph = BVGraph.loadMapped(graphFile, pl);
		} catch (IOException e) {
			System.out.println("Error loading graph!");
			e.printStackTrace();
			return;
		}

		ParallelBreadthFirstVisit bfs = new ParallelBreadthFirstVisit(graph, threads, true, pl);
		bfs.visit(start);
		
		BufferedWriter w = new BufferedWriter(new FileWriter(new File(outputFile)));

		long from = bfs.cutPoints.getLong(minPath);
		long to = bfs.cutPoints.getLong(bfs.maxDistance()+1);
		
		for(long i=from;i<to;i++)
		{
			long node = bfs.queue.getLong(i);
			
			w.write(i + "\t" + node + "\n");
		}
		w.close();
		
		/* max path
		int node = bfs.nodeAtMaxDistance();
		int last = -1;
		do
		{
			last = node;
			
			System.out.println(node);
			
			node = bfs.marker.get(node);
		} while(node!=-1 && node != last);
		*/
	}
	
}
