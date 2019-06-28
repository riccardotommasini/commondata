package org.webdatacommons.webgraph.big.algo;

/*		 
 * Copyright (C) 2011-2013 Sebastiano Vigna 
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.logging.ProgressLogger;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performs breadth-firsts visits of a graph (big) exploiting multicore
 * parallelism. This class was copied from the
 * {@link it.unimi.dsi.webgraph.algo.ParallelBreadthFirstVisit} and adapted to
 * handle big graphs (number of nodes > Max Integer) (by Robert).
 * 
 * <p>
 * To use this class you first create an instance, and then invoke
 * {@link #visit(long)}. If you want perform more visits preserving the
 * {@link #marker} state you can invoke {@link #visit(long)} again. By calling
 * {@link #clear()}, instead, you can reset {@link #marker} (i.e., forget about
 * visited nodes).
 * 
 * <p>
 * Alternatively, {@link #visitAll()} will start a visit from all the nodes of
 * the graph in a more efficient way.
 * 
 * <p>
 * After the visit, you can peek at the field {@link #marker} to discover
 * details about the visit. Depending on the {@link #parent} value provided at
 * construction time, the array {@link #marker} will be filled with parent
 * information (e.g., with the index of the parent node in the visit tree) or
 * with a <em>{@linkplain #round round number}</em> increased at each nonempty
 * visit, which act as a connected-component index if the graph is symmetric.
 * 
 * <p>
 * Observe that in the former case (if {@link #parent} is <code>true</code>),
 * the array {@link #marker} will contain the value -1 for the nodes that have
 * not been reached by the visit, the parent of the node in the BFS tree if the
 * node was not the root, or the node itself for the root.
 * 
 * <p>
 * In the case of {@link #visit(long, long)}, {@link #queue} and
 * {@link #cutPoints}, too, provide useful information. In particular, the nodes
 * in {@link #queue} from the <var>d</var>-th to the (<var>d</var>&nbsp;+1)-th
 * cutpoint are exactly the nodes at distance <var>d</var> from the source.
 * 
 * <h2>Performance issues</h2>
 * 
 * <p>
 * This class needs three longs per node. If there are several available
 * cores, breadth-first visits will be <em>decomposed</em> into relatively small
 * tasks (small blocks of nodes in the queue at the same distance from the
 * starting node) and each task will be assigned to the first available core.
 * Since all tasks are completely independent, this ensures a very high degree
 * of parallelism. However, on very sparse graphs the cost of keeping the
 * threads synchronised can be extremely high, and even end up
 * <em>increasing</em> the visit time.
 * 
 * <p>
 * Note that if the degree distribution is extremely skewed some cores might get
 * stuck in the enumeration of the successors of some nodes with a very high
 * degree.
 */

public class ParallelBreadthFirstVisit {
	/** The graph under examination. */
	public final ImmutableGraph graph;
	/** The queue of visited nodes. */
	public final LongBigArrayBigList queue;
	/**
	 * At the end of a visit, the cutpoints of {@link #queue}. The
	 * <var>d</var>-th cutpoint is the first node in the queue at distance
	 * <var>d</var>. The last cutpoint is the queue size.
	 */
	public final LongBigArrayBigList cutPoints;
	/** Whether {@link #marker} contains parent nodes or round numbers. */
	public final boolean parent;
	/**
	 * The marker array; contains -1 for nodes that have not still been
	 * enqueued, the parent of the visit tree if {@link #parent} is true, or an
	 * index increased at each visit if {@link #parent} is false, which in the
	 * symmetric case is the index of the connected component of the node.
	 */
	public final long[][] marker;
	/** The global progress logger. */
	private final ProgressLogger pl;
	/** The number of threads. */
	private final int numberOfThreads;
	/** The number of nodes visited. */
	private final AtomicLong progress;
	/**
	 * The next node position to be picked from the last segment of
	 * {@link #queue}.
	 */
	private final AtomicLong nextPosition;
	/** If true, the current visit is over. */
	private volatile boolean completed;
	/** The barrier used to synchronize visiting threads. */
	private volatile CyclicBarrier barrier;
	/** Keeps track of problems in visiting threads. */
	private volatile Throwable threadThrowable;
	/**
	 * A number increased at each nonempty visit (used to mark {@link #marker}
	 * if {@link #parent} is false).
	 */
	public long round;

	/**
	 * Creates a new class for keeping track of the state of parallel
	 * breadth-first visits.
	 * 
	 * @param graph
	 *            a graph.
	 * @param requestedThreads
	 *            the requested number of threads (0 for
	 *            {@link Runtime#availableProcessors()}).
	 * @param parent
	 *            if true, {@link #marker} will contain parent nodes; otherwise,
	 *            it will contain {@linkplain #round round numbers}.
	 * @param pl
	 *            a progress logger, or <code>null</code>.
	 */
	public ParallelBreadthFirstVisit(final ImmutableGraph graph,
			final int requestedThreads, final boolean parent,
			final ProgressLogger pl) {
		this.graph = graph;
		this.parent = parent;
		this.pl = pl;
		this.marker = LongBigArrays.newBigArray(graph.numNodes());
		this.queue = new LongBigArrayBigList(graph.numNodes());
		this.progress = new AtomicLong();
		this.nextPosition = new AtomicLong();
		this.cutPoints = new LongBigArrayBigList();
		numberOfThreads = requestedThreads != 0 ? requestedThreads : Runtime
				.getRuntime().availableProcessors();
		System.out.println("Number of parallel threads is " + numberOfThreads);
		clear();
	}

	/**
	 * Clear the marker and set every entry to -1
	 */
	private void clear() {
		round = -1;
		for (long i = 0; i < graph.numNodes(); i++) {
			LongBigArrays.set(marker, i, -1);
		}
	}

	private final class IterationThread extends Thread {
		private static final int GRANULARITY = 1000;

		public void run() {
			try {
				// We cache frequently used fields.
				final long[][] marker = ParallelBreadthFirstVisit.this.marker;
				final ImmutableGraph graph = ParallelBreadthFirstVisit.this.graph
						.copy();
				final boolean parent = ParallelBreadthFirstVisit.this.parent;

				for (;;) {
					barrier.await();
					if (completed)
						return;
					final LongBigArrayBigList out = new LongBigArrayBigList();
					final long first = cutPoints
							.getLong(cutPoints.size64() - 2);
					final long last = cutPoints.getLong(cutPoints.size64() - 1);
					long mark = round;
					for (;;) {
						// Try to get another piece of work.
						final long start = first
								+ nextPosition.getAndAdd(GRANULARITY);
						if (start >= last) {
							nextPosition.getAndAdd(-GRANULARITY);
							break;
						}

						final long end = (Math.min(last, start + GRANULARITY));
						out.clear();

						for (long pos = start; pos < end; pos++) {
							final long curr = queue.getLong(pos);
							if (parent) {
								mark = curr;
							}
							final LazyLongIterator successors = graph
									.successors(curr);
							for (long s; (s = successors.nextLong()) != -1;) {
								if (LongBigArrays.get(marker, s) == -1) {
									LongBigArrays.set(marker, s, mark);
									out.add(s);
								}
							}
						}

						progress.addAndGet(end - start);

						if (!out.isEmpty())
							synchronized (queue) {
								queue.addAll(out);
							}
					}
				}
			} catch (Throwable t) {
				threadThrowable = t;
				t.printStackTrace();
			}
		}
	}

	/**
	 * Performs a breadth-first visit of the given graph starting from the given
	 * node.
	 * 
	 * <p>
	 * This method will increment {@link #round}.
	 * 
	 * @param start
	 *            the starting node.
	 * @return the number of visited nodes.
	 * @see #visit(long,long)
	 */
	public long visit(final long start) {
		return visit(start, -1);
	}

	/**
	 * Performs a breadth-first visit of the given graph starting from the given
	 * node.
	 * 
	 * <p>
	 * This method will increment {@link #round} if at least one node is
	 * visited.
	 * 
	 * @param start
	 *            the starting node.
	 * @param expectedSize
	 *            the expected size (number of nodes) of the visit (for
	 *            logging), or -1 to use the number of nodes of the graph.
	 * @return the number of visited nodes.
	 */
	public long visit(final long start, final long expectedSize) {
		if (LongBigArrays.get(marker, start) != -1) {
			return 0;
		}
		round++;
		completed = false;
		queue.clear();
		cutPoints.clear();
		queue.add(start);
		cutPoints.add(0);
		LongBigArrays.set(marker, start, parent ? start : round);
		final IterationThread[] thread = new IterationThread[numberOfThreads];
		for (int i = thread.length; i-- != 0;)
			thread[i] = new IterationThread();
		progress.set(0);

		if (pl != null) {
			pl.start("Starting visit...");
			pl.expectedUpdates = expectedSize != -1 ? expectedSize : graph
					.numNodes();
			pl.itemsName = "nodes";
		}

		barrier = new CyclicBarrier(numberOfThreads, new Runnable() {
			public void run() {
				if (pl != null)
					pl.set(progress.get());

				if (queue.size64() == cutPoints.getLong(cutPoints.size64() - 1)) {
					completed = true;
					return;
				}

				cutPoints.add(queue.size64());
				nextPosition.set(0);
			}
		});

		for (int i = thread.length; i-- != 0;)
			thread[i].start();
		for (int i = thread.length; i-- != 0;)
			try {
				thread[i].join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

		if (threadThrowable != null)
			throw new RuntimeException(threadThrowable);
		if (pl != null)
			pl.done();
		return queue.size64();
	}

	/**
	 * Visits all nodes. Calls {@link #clear()} initially.
	 * 
	 * <p>
	 * This method is more efficient than invoking {@link #visit(long, long)} on
	 * all nodes as threads are created just once.
	 */
	public void visitAll() {
		final IterationThread[] thread = new IterationThread[numberOfThreads];
		for (int i = thread.length; i-- != 0;)
			thread[i] = new IterationThread();
		final long n = graph.numNodes();
		completed = false;
		clear();
		queue.clear();
		cutPoints.clear();
		progress.set(0);

		if (pl != null) {
			pl.start("Starting visits...");
			pl.expectedUpdates = graph.numNodes();
			pl.itemsName = "nodes";
		}

		barrier = new CyclicBarrier(numberOfThreads, new Runnable() {
			long curr = -1;

			public void run() {
				if (pl != null)
					pl.set(progress.get());
				// Either first call, or queue did not grow from the last call.
				if (curr == -1
						|| queue.size64() == cutPoints.getLong(cutPoints
								.size64() - 1)) {
					if (pl != null)
						pl.set(progress.get());
					// Look for the first node not visited yet.
					while (++curr < n && LongBigArrays.get(marker, curr) != -1)
						;

					if (curr == n) {
						completed = true;
						return;
					} else {
						round++;
						queue.clear();
						queue.add(curr);

						cutPoints.clear();
						cutPoints.add(0);

						LongBigArrays.set(marker, curr, parent ? curr : round);
					}
				}

				cutPoints.add(queue.size64());
				nextPosition.set(0);
			}
		});

		for (int i = thread.length; i-- != 0;)
			thread[i].start();
		for (int i = thread.length; i-- != 0;)
			try {
				thread[i].join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

		if (threadThrowable != null)
			throw new RuntimeException(threadThrowable);
		if (pl != null)
			pl.done();
	}

	/**
	 * Returns a node at maximum distance during the last visit (e.g., a node
	 * realizing the positive eccentricity of the starting node).
	 * 
	 * @return the maximum distance computed during the last visit.
	 */
	public long nodeAtMaxDistance() {
		return queue.getLong(queue.size64() - 1);
	}

	/**
	 * Returns the maximum distance computed during the last visit (e.g., the
	 * eccentricity of the source).
	 * 
	 * @return the maximum distance computed during the last visit.
	 */

	public long maxDistance() {
		return cutPoints.size64() - 2;
	}
}