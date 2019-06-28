package org.webdatacommons.webgraph.algo;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.algo.ParallelBreadthFirstVisit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BFSShowPath {

	public static void main(String[] args) throws IOException
	{
		String graphFile = args[0];
		int start = Integer.parseInt(args[1]);
		
		ProgressLogger pl = new ProgressLogger();
		BVGraph graph=null;
		try {
			graph = BVGraph.loadMapped(graphFile, pl);
		} catch (IOException e) {
			System.out.println("Error loading graph!");
			e.printStackTrace();
			return;
		}
		
		ParallelBreadthFirstVisit bfs = new ParallelBreadthFirstVisit(graph, 4, true, pl);
		bfs.visit(start);
		
		BufferedWriter w = new BufferedWriter(new FileWriter(new File("E:\\WGC Network\\pld\\BFS\\distantnodes.txt")));
		
		// nodes at distance > 20
		int from = bfs.cutPoints.getInt(20);
		int to = bfs.cutPoints.getInt(bfs.maxDistance()+1);
		
		for(int i=from;i<to;i++)
		{
			int node = bfs.queue.getInt(i);
			
			w.write(node + "\n");
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
