package org.webdatacommons.webgraph.big.algo;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.ints.IntBigArrays;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

/**
 * The original idea behind this class is to load a map of information about
 * out-degree of the nodes and combine it with the transposed graph to get to
 * gather information about the "siblings".
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 */
public class KnowYourSiblings {

	// the transposed graph!
	ImmutableGraph graph;
	// some progress logger
	ProgressLogger pl;
	// progress tracker
	private final AtomicLong progress;
	// this are the information of the out-degrees of all nodes
	public final int[][] outdegreeClass1;
	public final int[][] outdegreeClass2;

	// this are the information of the out-degrees of all nodes
	public final int[][] siblingsClass1;
	public final int[][] siblingsClass2;

	/**
	 * Writes the results to a stream using the format
	 * ID\tNUMBEROFSIBLINGSCLASS1\tNUMBEROFSIBLINGSCLASS2
	 * 
	 * 
	 * @param stream
	 * @throws IOException
	 */
	public void writeDistributionToStream(OutputStream stream)
			throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(stream));
		for (long i = 0; i < graph.numNodes(); i++) {
			bw.write(i + "\t" + IntBigArrays.get(siblingsClass1, i) + "\t"
					+ IntBigArrays.get(siblingsClass2, i) + "\n");
		}
	}

	public enum ExecutionMode {

		SUBGRAPH, ALL;
	}

	/**
	 * Init the class.
	 * 
	 * @param graph
	 *            transposed graph
	 * @param outdegreeClass1
	 *            outdegree of the nodes in the graph for class 1
	 * @param outdegreeClass2
	 *            outdegree of the nodes in the graph for class 2
	 * @param pl
	 *            progress logger
	 */
	public KnowYourSiblings(ImmutableGraph graph, int[][] outdegreeClass1,
			int[][] outdegreeClass2, ProgressLogger pl) {
		this.pl = pl;
		this.progress = new AtomicLong();
		this.graph = graph;
		this.outdegreeClass1 = outdegreeClass1;
		this.outdegreeClass2 = outdegreeClass2;
		siblingsClass1 = IntBigArrays.newBigArray(graph.numNodes());
		siblingsClass2 = IntBigArrays.newBigArray(graph.numNodes());
	}

	public void compute(int numberOfThreads) {
		if (numberOfThreads < 1) {
			numberOfThreads = Runtime.getRuntime().availableProcessors();
		}
		// init graph and threads
		final IterationThread[] thread = new IterationThread[numberOfThreads];
		for (int i = 0; i < numberOfThreads; i++) {
			thread[i] = new IterationThread();
		}
		if (pl != null) {
			pl.expectedUpdates = graph.numNodes();
			pl.start("Starting know-your-sibling calculation ...");
		}
		for (long i = 0; i < thread.length; i++) {
			thread[(int) i] = new IterationThread();
			thread[(int) i].firstNode = graph.numNodes()
					/ (long) numberOfThreads * i;
			thread[(int) i].lastNode = graph.numNodes()
					/ (long) numberOfThreads * (i + 1);
			thread[(int) i].copyGraph = graph.copy();
		}
		for (int i = 0; i < thread.length; i++) {
			System.out.println("Initialized Thread " + i + " from "
					+ thread[i].firstNode + " to " + thread[i].lastNode);
		}
		thread[thread.length - 1].lastNode = graph.numNodes();
		for (int i = thread.length; i-- != 0;)
			thread[i].start();
		for (int i = thread.length; i-- != 0;)
			try {
				thread[i].join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		if (pl != null)
			pl.done();
	}

	private final class IterationThread extends Thread {
		public long firstNode;
		public long lastNode;
		public ImmutableGraph copyGraph;

		long lastPos = firstNode;

		public void run() {
			// TODO check this
			long pos = 0;
			NodeIterator iter = null;
			boolean success = false;
			while (!success) {
				try {
					iter = copyGraph.nodeIterator(pos);
					success = true;
				} catch (IllegalArgumentException ie) {
					ie.printStackTrace();
					System.out.println("Caught " + ie.getLocalizedMessage()
							+ " for pos: " + pos++);
				}
			}
			while (iter != null && iter.hasNext() && pos < lastNode) {
				pos = iter.nextLong();
				try {
					final LazyLongIterator successors = iter.successors();
					long outdegree = iter.outdegree();
					int cntClass1 = 0;
					int cntClass2 = 0;
					long s = successors.nextLong();
					while (s != -1 && outdegree-- > 0) {
						cntClass1 += IntBigArrays.get(outdegreeClass1, s);
						cntClass2 += IntBigArrays.get(outdegreeClass2, s);
						s = successors.nextLong();
					}
					IntBigArrays.set(siblingsClass1, pos, cntClass1);
					IntBigArrays.set(siblingsClass2, pos, cntClass2);

					if (pos % 500000 == 0) {
						progress.addAndGet(pos - lastPos);
						lastPos = pos;

						if (pl != null)
							pl.set(progress.get());
					}
				} catch (IllegalArgumentException ie) {
					ie.printStackTrace();
					System.out.println("Caught " + ie.getLocalizedMessage()
							+ " for pos: " + pos);
					continue;
				} catch (Throwable t) {
					System.out.println("Caught throwable "
							+ t.getLocalizedMessage() + " for pos: " + pos);
					t.printStackTrace();
				}
			}

			progress.addAndGet(lastNode - lastPos - 1);

			if (pl != null)
				pl.set(progress.get());
		}
	}

	public static void main(String[] args) throws IOException {
		calculateStatistics(args[0]);
	}

	// TODO change this
	public static void calculateStatistics(String inputFile) throws IOException {
		long numGraphNodes = 0;
		long numbSubgraphNodes = 0;
		// AVG ratio of outlinks by label
		long subGraphToSubgraph = 0;
		long subGraphToGraph = 0;
		long graphToSubgraph = 0;
		long graphToGraph = 0;
		// AVG ratio of outlinks of nodes having at least 1 link to subgraph by
		// label
		long numGraphNodesWithLinkToSubgraph = 0;
		long numSubgraphNodesWithLinkToSubgraph = 0;
		long subGraphToSubgraphM1 = 0;
		long subGraphToGraphM1 = 0;
		long graphToSubgraphM1 = 0;
		long graphToGraphM1 = 0;

		// AVG ratio of outlinks of nodes having at least 5 link to subgraph by
		// label
		long numGraphNodesWith5LinkToSubgraph = 0;
		long numSubgraphNodesWith5LinkToSubgraph = 0;
		long subGraphToSubgraphM5 = 0;
		long subGraphToGraphM5 = 0;
		long graphToSubgraphM5 = 0;
		long graphToGraphM5 = 0;

		BufferedReader br = InputUtil.getBufferedReader(new File(inputFile));
		long lineCnt = 0;
		while (br.ready()) {
			lineCnt++;
			if (lineCnt % 10000000 == 0) {
				System.out.println(new Date() + " parsed " + lineCnt
						+ " lines.");
			}
			String line = br.readLine();
			String tok[] = line.split("\t");
			// 0 = id, 1 = OutToSubgraph, 2 = OutToGraph, 3 = Label (1 subgraph,
			// 0 graph)
			try {
				int label = Integer.parseInt(tok[3]);
				int outToSubgraph = Integer.parseInt(tok[1]);
				int outToGraph = Integer.parseInt(tok[2]);
				if (label == 0) {
					numGraphNodes++;
					graphToGraph += outToGraph;
					graphToSubgraph += outToSubgraph;
					if (outToSubgraph > 0) {
						numGraphNodesWithLinkToSubgraph++;
						graphToGraphM1 += outToGraph;
						graphToSubgraphM1 += outToSubgraph;
						if (outToSubgraph > 4) {
							numGraphNodesWith5LinkToSubgraph++;
							graphToGraphM5 += outToGraph;
							graphToSubgraphM5 += outToSubgraph;
						}
					}
				} else {
					numbSubgraphNodes++;
					subGraphToGraph += outToGraph;
					subGraphToSubgraph += outToSubgraph;
					if (outToSubgraph > 0) {
						numSubgraphNodesWithLinkToSubgraph++;
						subGraphToGraphM1 += outToGraph;
						subGraphToSubgraphM1 += outToSubgraph;
						if (outToSubgraph > 4) {
							numSubgraphNodesWith5LinkToSubgraph++;
							subGraphToGraphM5 += outToGraph;
							subGraphToSubgraphM5 += outToSubgraph;
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Exception at " + e.getLocalizedMessage());
			}
		}
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("Number of nodes in Graph (without subgraph): "
				+ numGraphNodes);
		System.out.println("Number of nodes in Subgraph: " + numbSubgraphNodes);
		System.out.println("Number of Arcs from Graph To Graph: "
				+ graphToGraph);
		System.out.println("Number of Arcs from Graph To Subgraph: "
				+ graphToSubgraph);
		System.out.println("Number of Arcs from Subgraph To Graph: "
				+ subGraphToGraph);
		System.out.println("Number of Arcs from Subgraph To Subgraph: "
				+ subGraphToSubgraph);
		System.out.println("-------------------------------------------------");
		System.out
				.println("Number of nodes in Graph (without subgraph) with 1 arc to Subgraph: "
						+ numGraphNodesWithLinkToSubgraph);
		System.out
				.println("Number of nodes in Subgraph with 1 arc to Subgraph: "
						+ numSubgraphNodesWithLinkToSubgraph);
		System.out.println("Number of Arcs from Graph To Graph (M1): "
				+ graphToGraphM1);
		System.out.println("Number of Arcs from Graph To Subgraph (M1): "
				+ graphToSubgraphM1);
		System.out.println("Number of Arcs from Subgraph To Graph (M1): "
				+ subGraphToGraphM1);
		System.out.println("Number of Arcs from Subgraph To Subgraph (M1): "
				+ subGraphToSubgraphM1);
		System.out.println("-------------------------------------------------");
		System.out
				.println("Number of nodes in Graph (without subgraph) with 5 arc to Subgraph: "
						+ numGraphNodesWith5LinkToSubgraph);
		System.out
				.println("Number of nodes in Subgraph with 1 arc to Subgraph: "
						+ numSubgraphNodesWith5LinkToSubgraph);
		System.out.println("Number of Arcs from Graph To Graph (M5): "
				+ graphToGraphM5);
		System.out.println("Number of Arcs from Graph To Subgraph (M5): "
				+ graphToSubgraphM5);
		System.out.println("Number of Arcs from Subgraph To Graph (M5): "
				+ subGraphToGraphM5);
		System.out.println("Number of Arcs from Subgraph To Subgraph (M5): "
				+ subGraphToSubgraphM5);

	}

}
