package org.webdatacommons.webgraph.big.algo;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.ints.IntBigArrays;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
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
 * The original idea was to calculate the linkage of nodes (semantic pages =
 * subgraph) within the whole graph. Either by using the original graph to
 * calculate the number of semantic/non-semantic pages linked by a
 * semantic/non-semantic page or by using the transposed graph to calculate the
 * the number of inlinks from semantic/non-semantic pages to
 * semantic/non-semantic pages. Make sure enough memory is available or switch
 * to loadMapped as this class will load beside the graph three integer-arrays
 * with the size of numNodes() of the graph.
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 *         TODO fix the progress logger, i think he gets confused because of
 *         multiple threads doing updates.
 * 
 */
public class OutdegreeOnSubgraph {

	ImmutableGraph graph;
	ProgressLogger pl;
	private final AtomicLong progress;
	public final int[][] subgraphOutDegree;
	public final int[][] externalOutDegree;
	private int[][] subgraph;
	private ExecutionMode mode;

	/**
	 * Writes the outdegree distribution of the subgraph to an
	 * {@link OutputStream} using tab-separated format:
	 * NODEID\tNUMBEROFARCTOSUBGRAPHNODES\tNUMBEROFARCHTOEXTERNALNODES\tLABEL
	 * 
	 * 
	 * @param stream
	 * @throws IOException
	 */
	public void writeDistributionToStream(OutputStream stream)
			throws IOException {
		System.out.println(new Date() + " Writing output file ...");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(stream));
		switch (mode) {
		case ALL:
			for (long i = 0; i < graph.numNodes(); i++) {
				if (i % 10000000 == 0) {
					System.out.println(new Date() + " wrote " + i + " lines.");
				}
				bw.write(i + "\t" + IntBigArrays.get(subgraphOutDegree, i)
						+ "\t" + IntBigArrays.get(externalOutDegree, i) + "\t"
						+ IntBigArrays.get(subgraph, i) + "\n");
			}
			break;
		case SUBGRAPH:
			for (long i = 0; i < graph.numNodes(); i++) {
				if (i % 10000000 == 0) {
					System.out.println(new Date() + " checked " + i
							+ " lines and wrote output.");
				}
				if (IntBigArrays.get(subgraph, i) == 1) {
					bw.write(i + "\t" + IntBigArrays.get(subgraphOutDegree, i)
							+ "\t" + IntBigArrays.get(externalOutDegree, i)
							+ "\t1\n");
				}
			}
			break;
		}
		System.out.println(new Date() + " Done writing output.");
	}

	public enum ExecutionMode {

		SUBGRAPH, ALL;
	}

	public OutdegreeOnSubgraph(ImmutableGraph graph, ExecutionMode mode,
			int[][] subgraph, ProgressLogger pl) {
		this.pl = pl;
		this.mode = mode;
		this.progress = new AtomicLong();
		this.graph = graph;
		subgraphOutDegree = IntBigArrays.newBigArray(graph.numNodes());
		externalOutDegree = IntBigArrays.newBigArray(graph.numNodes());
		this.subgraph = subgraph;
	}

	public void compute(int numberOfThreads) {
		if (numberOfThreads < 1) {
			numberOfThreads = Runtime.getRuntime().availableProcessors();
		}
		// we equally distribute depending on the mode
		switch (mode) {
		case SUBGRAPH:
			// init graph and threads
			final IterationThread[] thread = new IterationThread[numberOfThreads];
			for (int i = 0; i < numberOfThreads; i++) {
				thread[i] = new IterationThread();
				thread[i].graphCopy = graph.copy();
			}
			long cnt = 0;
			for (long j = 0; j < graph.numNodes(); j++) {
				if (IntBigArrays.get(subgraph, j) == 1) {
					thread[(int) j % numberOfThreads].ids.add(j);
					cnt++;
				}
			}
			pl.expectedUpdates = cnt;
			pl.start("Start calculating outdegree for subgraph ...");
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
			break;
		case ALL:
			// init graph and threads
			final IterationThreadAll[] threada = new IterationThreadAll[numberOfThreads];
			for (int i = 0; i < numberOfThreads; i++) {
				threada[i] = new IterationThreadAll();
			}
			if (pl != null) {
				pl.expectedUpdates = graph.numNodes();
				pl.start("Starting calculating outdegree ...");
			}
			for (long i = 0; i < threada.length; i++) {
				threada[(int) i] = new IterationThreadAll();
				threada[(int) i].firstNode = graph.numNodes()
						/ (long) numberOfThreads * i;
				threada[(int) i].lastNode = graph.numNodes()
						/ (long) numberOfThreads * (i + 1);
				threada[(int) i].graphCopy = graph.copy();
			}
			for (int i = 0; i < threada.length; i++) {
				System.out.println("Initialized Thread " + i + " from "
						+ threada[i].firstNode + " to " + threada[i].lastNode);
			}
			threada[threada.length - 1].lastNode = graph.numNodes();
			for (int i = threada.length; i-- != 0;) {
				threada[i].start();

			}
			for (int i = threada.length; i-- != 0;)
				try {
					threada[i].join();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			if (pl != null)
				pl.done();
			break;
		}
	}

	private final class IterationThreadAll extends Thread {
		public long firstNode;
		public long lastNode;
		public ImmutableGraph graphCopy;

		long lastPos;

		public void run() {
			long pos = firstNode;
			lastPos = firstNode;
			boolean success = false;
			NodeIterator iter = null;
			while (!success) {
				try {
					iter = graphCopy.nodeIterator(pos);
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
					long s = successors.nextLong();
					while (s != -1 && outdegree-- > 0) {
						// here we do the counting of files out and in
						if (IntBigArrays.get(subgraph, s) == 1) {
							// this is within the subgrpah
							IntBigArrays.incr(subgraphOutDegree, pos);
						} else {
							// this is outside the subgraph
							IntBigArrays.incr(externalOutDegree, pos);
						}
						// next
						s = successors.nextLong();
					}

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

	private final class IterationThread extends Thread {
		public LongBigArrayBigList ids = new LongBigArrayBigList();
		public ImmutableGraph graphCopy;

		public void run() {
			int cnt = 0;
			try {
				for (long pos : ids) {
					cnt++;
					// get the neighbors
					final LazyLongIterator successors = graphCopy
							.successors(pos);
					// get number of real outs
					long outdegree = graphCopy.outdegree(pos);
					long s = successors.nextLong();
					while (s != -1 && outdegree-- > 0) {
						// here we do the counting of files out and in
						if (IntBigArrays.get(subgraph, s) == 1) {
							// this is within the subgrpah
							IntBigArrays.incr(subgraphOutDegree, pos);
						} else {
							// this is outside the subgraph
							IntBigArrays.incr(externalOutDegree, pos);
						}
						// next
						s = successors.nextLong();
					}

					if (cnt >= 100000) {
						progress.addAndGet(cnt);
						cnt = 0;
						if (pl != null)
							pl.set(progress.get());
					}
				}

				progress.addAndGet(cnt);
				if (pl != null)
					pl.set(progress.get());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		calculateStatistics(args[0]);
	}

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
