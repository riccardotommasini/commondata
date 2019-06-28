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

import it.unimi.dsi.Util;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.longs.AbstractLongComparator;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * Computes the connected components of a <em>symmetric</em> (a.k.a&#46;
 * <em>undirected</em>) graph using a {@linkplain ParallelBreadthFirstVisit
 * parallel breadth-first visit}. This class has been changed from the original
 * {@link it.unimi.dsi.webgraph.algo.ConnectedComponents} so it is capable of
 * graphs with more than Max Integer nodes (by Robert).
 * 
 * <p>
 * The {@link #compute(ImmutableGraph, int, ProgressLogger)} method of this
 * class will return an instance that contains the data computed by visiting the
 * graph (using an instance of {@link ParallelBreadthFirstVisit}). Note that it
 * is your responsibility to pass a symmetric graph to
 * {@link #compute(ImmutableGraph, int, ProgressLogger)}. Otherwise, results
 * will be unpredictable.
 * 
 * <p>
 * After getting an instance, it is possible to run the {@link #computeSizes()}
 * and {@link #sortBySize(long[][])} methods to obtain further information. This
 * scheme has been devised to exploit the available memory as much as
 * possible&mdash;after the components have been computed, the returned instance
 * keeps no track of the graph, and the related memory can be freed by the
 * garbage collector.
 * 
 * <h2>Performance issues</h2>
 * 
 * <p>
 * This class uses an instance of {@link ParallelBreadthFirstVisit} to ensure a
 * high degree of parallelism (see its documentation for memory requirements).
 * 
 */

public class ConnectedComponents {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConnectedComponents.class);

	/** The number of connected components. */
	public final long numberOfComponents;

	/** The component of each node. */
	public final long[][] component;

	protected ConnectedComponents(final long numberOfComponents,
			final long[][] component) {
		this.numberOfComponents = numberOfComponents;
		this.component = component;
	}

	/**
	 * Computes the diameter of a symmetric graph.
	 * 
	 * @param symGraph
	 *            a symmetric graph.
	 * @param threads
	 *            the requested number of threads (0 for
	 *            {@link Runtime#availableProcessors()}).
	 * @param pl
	 *            a progress logger, or <code>null</code>.
	 * @return an instance of this class containing the computed components.
	 */
	public static ConnectedComponents compute(final ImmutableGraph symGraph,
			final int threads, final ProgressLogger pl) {
		ParallelBreadthFirstVisit visit = new ParallelBreadthFirstVisit(
				symGraph, threads, false, pl);
		visit.visitAll();
		final long numberOfComponents = visit.round + 1;
		final long[][] component = visit.marker.clone();
		visit = null;
		return new ConnectedComponents(numberOfComponents, component);
	}

	/**
	 * Returns the size array for this set of connected components.
	 * 
	 * @return the size array for this set of connected components.
	 */
	public long[][] computeSizes() {
		final long[][] size = LongBigArrays.newBigArray(numberOfComponents);
		for (int i = component.length; i-- != 0;) {
			long t[] = component[i];
			for (int j = t.length; j-- != 0;) {
				LongBigArrays.incr(size, t[j]);
			}
		}
		return size;
	}

	/**
	 * Renumbers by decreasing size the components of this set.
	 * 
	 * <p>
	 * After a call to this method, both the internal status of this class and
	 * the argument array are permuted so that the sizes of connected components
	 * are decreasing in the component index.
	 * 
	 * @param size
	 *            the components sizes, as returned by {@link #computeSizes()}.
	 */
	public void sortBySize(final long[][] size) {
		final long[][] perm = Util.identity(size);
		LongBigArrays.quickSort(perm, 0L, LongBigArrays.length(perm),
				new AbstractLongComparator() {
					@Override
					public int compare(long k1, long k2) {
						if (k2 == k1) {
							return 0;
						} else if (k2 > k1) {
							return 1;
						} else {
							return -1;
						}
					}
				});
		final long[][] copy = size.clone();
		for (long i = LongBigArrays.length(size); i-- != 0;) {
			LongBigArrays.set(size, i,
					LongBigArrays.get(copy, LongBigArrays.get(perm, i)));
		}
		Util.invertPermutationInPlace(perm);
		for (long i = LongBigArrays.length(component); i-- != 0;)
			LongBigArrays.set(component, i,
					LongBigArrays.get(perm, LongBigArrays.get(component, i)));
	}

	public static void main(String arg[]) throws IOException, JSAPException {
		SimpleJSAP jsap = new SimpleJSAP(
				ConnectedComponents.class.getName(),
				"Computes the connected components of a symmetric graph of given basename. The resulting data is saved "
						+ "in files stemmed from the given basename with extension .scc (a list of binary integers specifying the "
						+ "component of each node) and .sccsizes (a list of binary integer specifying the size of each component).",
				new Parameter[] {
						new Switch("sizes", 's', "sizes",
								"Compute component sizes."),
						new Switch("renumber", 'r', "renumber",
								"Renumber components in decreasing-size order."),
						new FlaggedOption("logInterval", JSAP.LONG_PARSER, Long
								.toString(ProgressLogger.DEFAULT_LOG_INTERVAL),
								JSAP.NOT_REQUIRED, 'l', "log-interval",
								"The minimum time interval between activity logs in milliseconds."),
						new Switch("mapped", 'm', "mapped",
								"Do not load the graph in main memory, but rather memory-map it."),
						new FlaggedOption(
								"threads",
								JSAP.INTSIZE_PARSER,
								"0",
								JSAP.NOT_REQUIRED,
								'T',
								"threads",
								"The number of threads to be used. If 0, the number will be estimated automatically."),
						new UnflaggedOption("basename", JSAP.STRING_PARSER,
								JSAP.NO_DEFAULT, JSAP.REQUIRED,
								JSAP.NOT_GREEDY, "The basename of the graph."),
						new UnflaggedOption("resultsBasename",
								JSAP.STRING_PARSER, JSAP.NO_DEFAULT,
								JSAP.NOT_REQUIRED, JSAP.NOT_GREEDY,
								"The basename of the resulting files."), });

		JSAPResult jsapResult = jsap.parse(arg);
		if (jsap.messagePrinted())
			System.exit(1);

		final String basename = jsapResult.getString("basename");
		final String resultsBasename = jsapResult.getString("resultsBasename",
				basename);
		final int threads = jsapResult.getInt("threads");
		ProgressLogger pl = new ProgressLogger(LOGGER,
				jsapResult.getLong("logInterval"), TimeUnit.MILLISECONDS);

		final ConnectedComponents components = ConnectedComponents.compute(
				jsapResult.userSpecified("mapped") ? ImmutableGraph
						.loadMapped(basename) : ImmutableGraph.load(basename),
				threads, pl);

		if (jsapResult.getBoolean("sizes") || jsapResult.getBoolean("renumber")) {
			final long size[][] = components.computeSizes();
			if (jsapResult.getBoolean("renumber"))
				components.sortBySize(size);
			if (jsapResult.getBoolean("sizes"))
				BinIO.storeLongs(size, resultsBasename + ".sccsizes");
		}
		BinIO.storeLongs(components.component, resultsBasename + ".scc");
	}
}