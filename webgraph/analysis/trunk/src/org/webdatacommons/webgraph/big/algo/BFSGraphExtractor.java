package org.webdatacommons.webgraph.big.algo;

import it.unimi.dsi.big.webgraph.BVGraph;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

/**
 * This class discovers all nodes within a certain range starting from a given
 * set of starting nodes or simply one.
 * 
 * @author Robert Meusel
 * 
 */
public class BFSGraphExtractor {

	public static void main(String[] args) throws NumberFormatException,
			IOException {
		if (args.length != 4) {
			System.out
					.println("Usage: BFSGraphExtractor <GRAPHFILE> <STARTINGNODES> <OUTPUTFILE> <THREADNUM>");
			return;
		}
		// get parameters
		String graphFile = args[0];
		String start = args[1];
		String output = args[2];
		int numThreads = Integer.parseInt(args[3]);

		// load graph
		ProgressLogger pl = new ProgressLogger();
		BVGraph graph = null;
		try {
			graph = BVGraph.loadMapped(graphFile, pl);
		} catch (IOException e) {
			System.out.println("Error loading graph!");
			e.printStackTrace();
			return;
		}

		// read start points
		ArrayList<Integer> startPoints = new ArrayList<Integer>();
		if (new File(start).exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					start)));
			while (reader.ready()) {
				String line = reader.readLine();
				if (line != null) {
					startPoints.add(Integer.parseInt(line));
				}
			}
			reader.close();
		}
		if (startPoints.size() < 1) {
			System.out
					.println("No starting points given ... stopping process.");
			return;
		} else {
			System.out.println("Read " + startPoints.size()
					+ " starting points.");
		}

		// run bfs - forward
		new BFSGraphExtractor().run(graph, pl, startPoints, output
				+ "_forward.tab", numThreads);
	}

	public BFSGraphExtractor() {

	}

	public void run(ImmutableGraph graph, ProgressLogger pl,
			ArrayList<Integer> startPoints, String output, int numThreads)
			throws IOException {

		ParallelBreadthFirstVisit bfs = new ParallelBreadthFirstVisit(graph,
				numThreads, false, pl);

		int cnt = 1;
		// for each sample point
		for (int start : startPoints) {
			pl.info = "BFS (Run " + cnt + "/" + startPoints.size() + ")";
			// do BFS run
			bfs.visit(start);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new GZIPOutputStream(new FileOutputStream(new File("node_"
							+ start + "_" + output + ".gz")))));
			// collect data for every step in the bfs
			// count the number of nodes at each step
			
			System.out.println("Maximal distance is " + bfs.maxDistance());
			for (long d = 0; d <= bfs.maxDistance(); d++) {

				long from = bfs.cutPoints.getLong(d);
				long to = bfs.cutPoints.getLong(d + 1);
				bw.write("" + to);
				for (long i = from; i < to; i++) {
					bw.write("\t");
					bw.write("" + bfs.queue.getLong(i));
				}
				bw.write("\n");
				pl.update();
			}
			
			bw.flush();
			bw.close();
			pl.done();
		} // for start

	}

}
