package org.webdatacommons.webgraph.process.shrink;

import it.unimi.dsi.fastutil.ints.IntBigArrays;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileExistsException;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

/**
 * Removes based on a input map (which can be a shrinked index transformation
 * map) and removes links whose ids (start and end) would be mapped to the same
 * id. NOTE: Uses ADJ representation of the graph and writes to arc list.
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 */
public class InternalLinkRemoval {

	private static Logger log = Logger.getLogger("InternalLinkRemoval.class");
	private int[][] transformation;
	private String transformationIndexFileOrDir;
	private String adjNetworkFileOrDir;
	private File outputDir;
	private int threads;
	private long maxIndex;

	private static final int EMPTY_SHRINK = -1;

	// the last/highest index 3005287791

	/**
	 * Initializes the class.
	 * 
	 * @param transformationIndexFileOrDir
	 *            the files including a mapping for each original id to a target
	 *            id
	 * @param networkFileOrDir
	 *            the network file with the original ids
	 * @param output
	 *            the directory to put the output to
	 * @param maxIndex
	 *            the highest index value
	 * @param threads
	 *            number of threads which should be used. If 0 the number of
	 *            cores will be used.
	 * @throws FileExistsException
	 */
	public InternalLinkRemoval(String transformationIndexFileOrDir,
			String networkFileOrDir, String output, long maxIndex, int threads)
			throws FileExistsException {
		try {
			log.addHandler(new FileHandler("app.log", false));
			log.setUseParentHandlers(false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// as we know that the mapping is only done to integer values
		this.transformation = IntBigArrays.newBigArray(maxIndex);
		this.transformationIndexFileOrDir = transformationIndexFileOrDir;
		this.adjNetworkFileOrDir = networkFileOrDir;
		this.maxIndex = maxIndex;
		outputDir = new File(output);
		if (!outputDir.exists() || !outputDir.isDirectory()) {
			System.out.println("Output dir does not exists or is no directory");
			throw new FileExistsException();
		}
		if (threads < 0) {
			System.out
					.println("Number of threads has to be positive or 0 for number of available cores");
		} else if (threads == 0) {
			this.threads = Runtime.getRuntime().availableProcessors();
			System.out.println("Setting number of threads to " + this.threads);
		} else {
			this.threads = threads;
		}
	}

	private long printState(ThreadPoolExecutor executor, long startTime) {
		long total = executor.getTaskCount();
		long finished = executor.getCompletedTaskCount();
		long runtime = (System.currentTimeMillis() - startTime) / 1000;
		System.out
				.printf("Runtime: %ds --> Total: %d, Done: %d, %ss / item, Finished in: %ds \n",
						runtime, total, finished,
						String.format("%.2f", ((float) runtime) / finished),
						(int) ((float) runtime / finished) * (total - finished));

		return total - finished;
	}

	public void process() throws IOException {
		System.out.println("Starting ...");
		long startTime = new Date().getTime();
		log.log(Level.INFO, new Date() + " " + "Starting.");
		// file list to work
		System.out.println("Loading transformation table ...");

		initializeMap(EMPTY_SHRINK);
		loadTransformationMap();

		System.out.println("Getting files to process...");
		List<File> filesToProcess = InputUtil.getFileList(adjNetworkFileOrDir);
		log.info(filesToProcess.size() + " files to process.");
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(threads);
		for (File f : filesToProcess) {
			if (f.isFile()) {
				executor.submit(new Worker(f));
			}
		}
		long stillTodo = printState(executor, startTime);
		while (stillTodo != 0) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			stillTodo = printState(executor, startTime);
		}

		executor.shutdown();
		log.log(Level.INFO, new Date() + " " + "Done.");
		System.out.println("Done.");
	}

	private void initializeMap(int value) {
		System.out.println("Initializing map ...");
		for (long i = 0; i < maxIndex; i++) {
			IntBigArrays.set(transformation, i, value);
		}
	}

	private void loadTransformationMap() throws IOException {
		System.out.println("Reading transformation files ...");
		List<File> filesToProcess = InputUtil
				.getFileList(transformationIndexFileOrDir);
		int files = filesToProcess.size();
		int cnt = 0;
		for (File file : filesToProcess) {
			cnt++;
			System.out.println(new Date() + " " + "Reading file " + cnt
					+ " of " + files + ": " + file.getName());
			BufferedReader br = InputUtil.getBufferedReader(file);
			while (br.ready()) {
				String line = br.readLine();
				String tok[] = line.split("\t");
				long originalId = Long.parseLong(tok[0]);
				int shrinkId = Integer.parseInt(tok[1]);
				IntBigArrays.set(transformation, originalId, shrinkId);
			}
		}
		System.out.println("Transformation files read.");
	}

	private class Worker implements Runnable {

		private File file;

		public Worker(File file) {
			this.file = file;
		}

		public void run() {
			log.log(Level.INFO, new Date() + " " + "Start processing " + file);
			try {
				BufferedReader br = InputUtil.getBufferedReader(file);
				// output
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(
								new GZIPOutputStream(new FileOutputStream(
										new File(outputDir, file.getName()
												.replace(".gz", "") + ".gz")))));
				
				while (br.ready()) {
					String line = br.readLine();
					String tok[] = line.split("\t");
					try {
						long origin = Long.parseLong(tok[0]);
						int transformedOrigin = IntBigArrays.get(
								transformation, origin);
						// this needs to be 2 as wie have <starting> <number of targets> <target 1> <target 2>
						for (int i = 2; i < tok.length; i++) {
							long target = Long.parseLong(tok[i]);
							int transformedTarget = IntBigArrays.get(
									transformation, target);
							 if (transformedOrigin == EMPTY_SHRINK
							 || transformedTarget == EMPTY_SHRINK) {
							 // do not write maps which could not be shrinked
							 continue;
							 }
							// write only if no internal link is used
							if (transformedOrigin != transformedTarget) {
								writer.write(origin + "\t" + target + "\n");
							}
						}
					} catch (NumberFormatException ne) {
						System.out.println("Could not get ids from " + line
								+ " in file " + file.getName()
								+ ". Skipping line.");
					}
				}
				writer.close();
				br.close();
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not process file " + file);
			}
		}
	}

	/**
	 * Starter
	 * 
	 * @param args
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void main(String[] args) throws NumberFormatException,
			IOException {
		if (args == null || args.length < 5) {
			System.out
					.println("USAGE: InternalLinkRemoval <TRANSFORMATIONINDEXFILEORDIR> <NETWORKFILEORDIR> <OUTPUTDIR> <MAXINDEX> <NUMTHREADS>");
		} else {
			startMapping(args[0], args[1], args[2], Long.parseLong(args[3]),
					Integer.parseInt(args[4]));
		}
	}

	/**
	 * The real starter for this.
	 * 
	 * @param transformationIndexFileOrDir
	 * @param networkFileOrDir
	 * @param output
	 * @param maxIndex
	 * @param threads
	 * @throws IOException
	 */
	private static void startMapping(String transformationIndexFileOrDir,
			String networkFileOrDir, String output, long maxIndex, int threads)
			throws IOException {
		InternalLinkRemoval map = new InternalLinkRemoval(
				transformationIndexFileOrDir, networkFileOrDir, output,
				maxIndex, threads);
		map.process();
	}
}
