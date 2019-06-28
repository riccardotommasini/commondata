package org.webdatacommons.webgraph.algo;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.algo.ParallelBreadthFirstVisit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

public class BFSNodeReachability {

	public static void main(String[] args) throws NumberFormatException, IOException
	{
		if(args.length!=6)
		{
			System.out.println("Parameters:");
			System.out.println("1. Graph");
			System.out.println("2. Transpose Graph");
			System.out.println("3. Start nodes file");
			System.out.println("4. bowtie partition file");
			System.out.println("5. output file");
			System.out.println("6. number of threads");
			return;
		}
		
		String graphFile = args[0];
		String graphTranspose = args[1];
		String start = args[2];
		String bowtieFile = args[3];
		String output = args[4];
		int numThreads = Integer.parseInt(args[5]);
		
		// read bowtie
		BufferedReader r = new BufferedReader(new FileReader(new File(bowtieFile)));
		String line;
		ArrayList<Integer> bowTie = new ArrayList<Integer>();
		r.readLine(); // skip header
		while((line = r.readLine()) != null)
			bowTie.add(Integer.parseInt(line));
		r.close();
				
		// load graph
		ProgressLogger pl = new ProgressLogger();
		BVGraph graph=null;
		try {
			graph = BVGraph.load(graphFile, pl);
		} catch (IOException e) {
			System.out.println("Error loading graph!");
			e.printStackTrace();
			return;
		}
		
		// read start points
		ArrayList<Integer> startPoints = new ArrayList<Integer>();
		if(new File(start).exists())
		{
			r = new BufferedReader(new FileReader(new File(start)));
			while((line = r.readLine()) != null)
				startPoints.add(Integer.parseInt(line));
			r.close();
		}
		else
		{
			// create start points
			Random rnd = new Random();
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(start)));
			for(int i=0;i<500;i++)
			{
				int s = rnd.nextInt(graph.numNodes());
				startPoints.add(s);
				w.write(s + "\n");
			}
			w.close();
		}
		
		// run bfs - forward
		new BFSNodeReachability().Run(graph, pl, startPoints, bowTie, output + "_forward.tab", numThreads);
		
		// load graph - transpose
		try {
			graph = BVGraph.load(graphTranspose, pl);
		} catch (IOException e) {
			System.out.println("Error loading graph!");
			e.printStackTrace();
			return;
		}
		
		// run bfs - backward
		new BFSNodeReachability().Run(graph, pl, startPoints, bowTie, output + "_backward.tab", numThreads);
	}
	
	public BFSNodeReachability()
	{
		
	}
	
	public void Run(ImmutableGraph graph, ProgressLogger pl, ArrayList<Integer> startPoints, ArrayList<Integer> bowTie, String output, int numThreads) throws IOException
	{
		BufferedWriter w = new BufferedWriter(new FileWriter(new File(output)));
		
		ParallelBreadthFirstVisit bfs = new ParallelBreadthFirstVisit(graph, numThreads, false, pl);

		w.write("start-id\tstart-id-BowTieComponent\tpath-length\tDISC-reached\tIN-reached\tLSCC-reached\tOUT-reached\tTUBES-reached\tTENDRILS-reached\tnodes-reached\n");
		
		int cnt=1;
		// for each sample point
		for(int start : startPoints)
		{
			pl.info = "BFS (Run " + cnt + "/" + startPoints.size() + ")";
			// do BFS run
			bfs.visit(start);
			
			// now prepare data
			TreeMap<Integer, Integer[]> nodes = new TreeMap<Integer, Integer[]>();
			// Integer[] will hold the values for each component:
			// 0 - DISC
			// 1 - LSCC
			// 2 - IN 
			// 3 - OUT
			// 4 - TUBES
			// 5 - TENDRILS
			// 6 - TOTAL
			

			pl.start("aggregate values (Run " + cnt + "/" + startPoints.size() + ")");
			pl.info = "aggregate values (Run " + cnt + "/" + startPoints.size() + ")";
			pl.expectedUpdates = bfs.queue.size();

			// collect data for every step in the bfs
			// count the number of nodes at each step
			for(int d = 0; d <= bfs.maxDistance(); d++)
			{
				nodes.put(d, new Integer[] { 0,0,0,0,0,0,0 });
				
				int from = bfs.cutPoints.getInt(d);
				int to  = bfs.cutPoints.getInt(d+1);
				
				for(int i = from; i < to; i++)
				{
					// node id is at distance d
					int id = bfs.queue.getInt(i);
					
					// bowtie-component of current node
					int bowtie_comp = bowTie.get(id);
					
					// values for the current step (at distance d)
					Integer[] values = nodes.get(d);
					
					// increment value for component
					values[bowtie_comp]++;
					// increment total value
					values[6]++;
					
					pl.update();
				}
			}
			pl.done();
			
			pl.start("write output (Run " + cnt + "/" + startPoints.size() + ")");
			pl.info = "write output (Run " + cnt + "/" + startPoints.size() + ")";
			pl.expectedUpdates = bfs.marker.length();
			// Write output:
			// start-id / start-id-BowTieComponent / path-length / DISC-reached / IN-reached / LSCC-reached / OUT-reached / TUBES-reached / TENDRILS-reached / nodes-reached 
			for(int dist : nodes.keySet())
			{
				StringBuilder sb = new StringBuilder();
				
				sb.append(start);
				sb.append("\t");
				sb.append(bowTie.get(start));
				sb.append("\t");
				sb.append(dist);
				
				for(int i=0;i<7;i++)
				{
					sb.append("\t");
					sb.append(nodes.get(dist)[i]);
				}
				
				sb.append("\n");
				
				w.write(sb.toString());
			}
			pl.done();
			
			w.flush();
			bfs.clear();
			cnt++;
		} // for start
		
		
	}
	
}
