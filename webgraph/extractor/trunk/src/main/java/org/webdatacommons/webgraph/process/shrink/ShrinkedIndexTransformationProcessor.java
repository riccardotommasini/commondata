package org.webdatacommons.webgraph.process.shrink;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

/**
 * Should read a newly created smaller index for a certain level and match the
 * original index of the higher level to the new level.
 * 
 * Input:
 * <ul>
 * <li>Original Index (e.g. URL Level)</li>
 * <li>Shrinked Index (e.g. PLD Level)</li>
 * </ul>
 * Output:
 * <ul>
 * <li>Permutation from original to shrinked index using the format [ShrinkID]\t[OriginalID]</li>
 * </ul>
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 */
public class ShrinkedIndexTransformationProcessor {

	private HashMap<String, Integer> shrinkedIndex = new HashMap<String, Integer>();
	private String shrinkedIndexFileOrDir;
	private String originalIndexFileOrDir;
	private String outputDir;
	private String separator = "\t";
	private static Logger log = Logger.getLogger("ShrinkedIndexTransformationProcessor.class");
	private static Logger statusLog = Logger.getLogger("ShrinkedIndexTransformationProcessor.class");
	private TransformationLevel level;
	private int threads;

	public ShrinkedIndexTransformationProcessor(String shrinkedIndexFileOrDir,
			String originalIndexFileOrDir, String outputDir, int threads,
			String level) {
		try {
			log.addHandler(new FileHandler("app.log", false));
			log.setUseParentHandlers(false);
			statusLog.addHandler(new FileHandler("status.log", false));
			statusLog.setUseParentHandlers(false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (new File(shrinkedIndexFileOrDir).exists()) {
			this.shrinkedIndexFileOrDir = shrinkedIndexFileOrDir;
		} else {
			log.log(Level.SEVERE, "File or directory " + shrinkedIndexFileOrDir
					+ " does not exists.");
			return;
		}
		if (new File(originalIndexFileOrDir).exists()) {
			this.originalIndexFileOrDir = originalIndexFileOrDir;
		} else {
			log.log(Level.SEVERE, "File or directory " + originalIndexFileOrDir
					+ " does not exists.");
			return;
		}
		File output = new File(outputDir);
		if (output.exists() && output.isDirectory()) {
			this.outputDir = outputDir;
		} else {
			log.log(Level.SEVERE, "Directory " + outputDir
					+ " does not exists or is not a directory.");
			return;
		}

		if (threads < 0) {
			System.out
					.println("Number of threads has to be positive or 0 for number of available cores");
		} else if (threads == 0) {
			this.threads = Runtime.getRuntime().availableProcessors();
		} else {
			this.threads = threads;
		}
		this.level = TransformationLevel.valueOf(level);

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

	private void fillMap() throws IOException {
		List<File> filesToProcess = InputUtil
				.getFileList(shrinkedIndexFileOrDir);
		log.info("Loading shrinked index. " + filesToProcess.size()
				+ " files to process.");
		for (File f : filesToProcess) {
			BufferedReader br = InputUtil.getBufferedReader(f);
			while (br.ready()) {
				String line = br.readLine();
				String tok[] = line.split(separator);
				try {
					shrinkedIndex.put(tok[0], Integer.parseInt(tok[1]));
				} catch (NumberFormatException ne) {
					log.log(Level.WARNING, "Could not parse index from line "
							+ line);
				}
			}
		}
		log.info("Loaded " + shrinkedIndex.size()
				+ " entries from shrinked index input files.");
	}

	public void process() throws IOException {
		System.out.println("Starting ...");
		long startTime = new Date().getTime();
		log.log(Level.INFO, new Date() + " " + "Starting.");
		// file list to work
		System.out.println("Filling map ...");
		fillMap();
		System.out.println("Map filled with " + shrinkedIndex.size()
				+ " elements.");
		System.out.println("Getting files to process...");
		List<File> filesToProcess = InputUtil
				.getFileList(originalIndexFileOrDir);
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
						new OutputStreamWriter(new GZIPOutputStream(
								new FileOutputStream(new File(outputDir, level
										.toString().toLowerCase()
										+ "-"
										+ file.getName().replace(".gz", "")
										+ ".gz")))));
				while (br.ready()) {
					String line = br.readLine();
					String tok[] = line.split("\t");
					String url = tok[0];
					// uncompress
					url = DomainUtil.uncompress(url);
					// depending on the level we need a different shrink level
					String key = null;
					switch (level) {
					case PLD:
						key = DomainUtil.getPayLevelDomainFromWholeURL(url);
						break;
					case SD:
						key = DomainUtil.getSubDomainFromWholeUrl(url);
						break;
					case SD1:
						key = DomainUtil.getFirstSubDomainFromWholeUrl(url);
						break;
					}
					
					// now we check if the map has the key
					if (shrinkedIndex.containsKey(key)) {
						// so we found it. Now we write the key and the original
						// key in a file so we can map it finally
						writer.write(tok[1] + separator
								+ shrinkedIndex.get(key) + "\n");
					} else {
						// its not in there - should not happen but maybe some
						// data was eliminated from the index.
					}
				}
				writer.close();
				br.close();
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not process file " + file);
			}
		}
	}

}
