package org.webdatacommons.webgraph.big.algo;

import it.unimi.dsi.big.webgraph.BVGraph;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.ints.IntBigArrays;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

public class NodeIdReplacer {

	int[][] mappedNodes;
	BVGraph graph;
	ProgressLogger pl;
	File outputDir;
	// progress tracker
	private final AtomicLong progress;

	public NodeIdReplacer(int[][] mappedNodes, BVGraph graph, File outputDir) {
		super();
		this.progress = new AtomicLong();
		this.pl = new ProgressLogger();
		this.mappedNodes = mappedNodes;
		this.graph = graph;
		this.outputDir = outputDir;
	}

	public void compute(int numberOfThreads) throws FileNotFoundException,
			IOException {
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
			pl.start("Starting replacing ids ...");
		}
		for (long i = 0; i < thread.length; i++) {
			thread[(int) i] = new IterationThread();
			thread[(int) i].firstNode = graph.numNodes()
					/ (long) numberOfThreads * i;
			thread[(int) i].lastNode = graph.numNodes()
					/ (long) numberOfThreads * (i + 1);
			thread[(int) i].copyGraph = graph.copy();
			thread[(int) i].bw = new BufferedWriter(new OutputStreamWriter(
					new GZIPOutputStream(new FileOutputStream(new File(
							outputDir, "part-" + String.format("%05d", i)
									+ ".gz")))));
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
		public BufferedWriter bw;

		public void run() {
			long pos = firstNode;
			long lastPos = firstNode;
			NodeIterator iter = copyGraph.nodeIterator(pos);

			while (iter != null && iter.hasNext() && pos < lastNode) {
				pos = iter.nextLong();

				final LazyLongIterator successors = iter.successors();
				long outdegree = iter.outdegree();
				long s = successors.nextLong();
				int posMap = IntBigArrays.get(mappedNodes, pos);
				while (s != -1 && outdegree-- > 0) {
					try {
						int sMap = IntBigArrays.get(mappedNodes, s);
						if (posMap != sMap) {

							bw.write(posMap + "\t" + sMap + "\n");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					s = successors.nextLong();
				}

				if (pos % 500000 == 0) {
					progress.addAndGet(pos - lastPos);
					lastPos = pos;

					if (pl != null)
						pl.set(progress.get());
				}

			}
			progress.addAndGet(lastNode - lastPos - 1);
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (pl != null)
				pl.set(progress.get());
		}
	}

}
