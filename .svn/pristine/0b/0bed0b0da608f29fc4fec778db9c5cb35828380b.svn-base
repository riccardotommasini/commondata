package org.webdatacommons.webgraph.big.algo;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.logging.ProgressLogger;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Calculates the out-degree of a given graph. Only! This means if you want to
 * calculate the degree (in & out) of a graph, first symmetrize the graph. If
 * you want to calculate the in-degree of the graph, first transpose the graph.
 * 
 * @author Oliver, Robert
 * 
 */
public class OutDegree {

	public final long[][] degree;
	ImmutableGraph graph;
	ProgressLogger progressLogger;
	private final AtomicLong progress;

	public OutDegree(final ImmutableGraph symGraph, final ProgressLogger pl) {
		degree = LongBigArrays.newBigArray(symGraph.numNodes());
		graph = symGraph;
		this.progressLogger = pl;
		progressLogger.expectedUpdates = symGraph.numNodes();
		this.progress = new AtomicLong();
		pl.expectedUpdates = symGraph.numNodes();
	}

	public static OutDegree compute(final ImmutableGraph symGraph,
			final int threads, final ProgressLogger pl) {
		OutDegree d = new OutDegree(symGraph, pl);

		int numThreads = threads != 0 ? threads : Runtime.getRuntime()
				.availableProcessors();

		d.compute(numThreads);

		return d;
	}

	public void compute(int numberOfThreads) {
		final IterationThread[] thread = new IterationThread[numberOfThreads];
		for (long i = 0; i < thread.length; i++) {
			thread[(int) i] = new IterationThread();
			thread[(int) i].firstNode = graph.numNodes()
					/ (long) numberOfThreads * i;
			thread[(int) i].lastNode = graph.numNodes()
					/ (long) numberOfThreads * (i + 1);
			thread[(int) i].graphCopy = graph.copy();
		}
		thread[thread.length - 1].lastNode = graph.numNodes();
		if (progressLogger != null)
			progressLogger.start("Starting output degree calculation ...");
		for (int i = thread.length; i-- != 0;)
			thread[i].start();
		for (int i = thread.length; i-- != 0;) {
			try {
				thread[i].join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		if (progressLogger != null)
			progressLogger.done();
	}

	private final class IterationThread extends Thread {
		public long firstNode;
		public long lastNode;
		public ImmutableGraph graphCopy;
		long lastPos;

		public void run() {
			try {
				lastPos = firstNode;
				for (long pos = firstNode; pos < lastNode; pos++) {
					try {
						LongBigArrays
								.add(degree, pos, graphCopy.outdegree(pos));
					} catch (Throwable t) {
						t.printStackTrace();
						System.out.println("Caught throwable "
								+ t.getLocalizedMessage() + " for position "
								+ pos);
						continue;
					}
					if (pos % 500000 == 0) {
						progress.addAndGet(pos - lastPos);
						lastPos = pos;
						if (progressLogger != null)
							progressLogger.set(progress.get());
					}
				}

				progress.addAndGet(lastNode - lastPos - 1);

				if (progressLogger != null)
					progressLogger.set(progress.get());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
