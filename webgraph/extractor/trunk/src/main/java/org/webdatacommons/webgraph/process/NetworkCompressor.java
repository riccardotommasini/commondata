package org.webdatacommons.webgraph.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Should read a file with urls which should remain in the network and the id
 * for each page as long. Original sparse network files are parsed and pages are
 * replaced by ids if they are included in the list. Otherwise they are ignored.
 * 
 * @author Robert Meusel
 * 
 */
public class NetworkCompressor {

	private final HashMap<Long, Integer> pageMap = new HashMap<Long, Integer>();
	private String page_delimiter = "\t";
	private String pageFile;
	private String directory;
	private int cores = 4;

	public void setCores(int cores) {
		this.cores = cores;
	}

	public NetworkCompressor(String pageFile, String directory, int cores) {
		this.pageFile = pageFile;
		this.cores = cores;
		this.directory = directory;
	}

	public NetworkCompressor(String pageFile, String directory) {
		this.pageFile = pageFile;
		this.directory = directory;
		try {
			log.addHandler(new FileHandler("app.log", false));
			log.setUseParentHandlers(false);
			statusLog.addHandler(new FileHandler("status.log", false));
			statusLog.setUseParentHandlers(false);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Logger log = Logger.getLogger("NetworkCompressor.class");
	private static Logger statusLog = Logger
			.getLogger("NetworkCompressor.class");

	private void fillMap() throws IOException {

		if (pageFile == null) {
			log.log(Level.WARNING, "Missing page file. Could not load map.");
			throw new IOException();
		}
		log.log(Level.INFO, new Date() + " " + "Start loading page file ...");
		BufferedReader br;
		if (pageFile.endsWith(".gz")) {
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(
					new FileInputStream(pageFile))));
		} else {
			br = new BufferedReader(new FileReader(new File(pageFile)));
		}
		int i = 0;
		int hashdup = 0;
		while (br.ready()) {
			i++;
			if (i % 1000000 == 0) {
				System.out.println("... " + i + " lines loaded " + new Date());
			}
			String line = br.readLine();

			String[] tokens = line.split(page_delimiter);
			if (tokens.length == 2) {
					if (pageMap.containsKey(hash(tokens[0]))) {
						System.out.println("Duplicated hashcode found... " + hashdup + " found in total.");
						hashdup++;
					} else {
						pageMap.put(hash(tokens[0]),
								Integer.parseInt(tokens[1]));
					}
				
			} else {
				log.log(Level.FINE,
						"Could not read line in page file. Maybe empty or to long line: "
								+ line);
			}
		}
		br.close();
		log.log(Level.INFO, new Date() + " " + "Done loading file list.");
	}

	public void start() {
		long startTime = new Date().getTime();
		File dir = new File(directory);
		if (dir.isFile()) {
			log.log(Level.SEVERE, "Directory is a file.");
			return;
		}
		// fill map
		try {
			fillMap();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not load page map", e);
			return;
		}
		// fill todo list
		File[] files = dir.listFiles();
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(cores);

		for (File f : files) {
			if (f.isFile()) {
				executor.submit(new Worker(f.getAbsolutePath()));
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
	}

	private long printState(ThreadPoolExecutor executor, long startTime) {
		long total = executor.getTaskCount();
		long finished = executor.getCompletedTaskCount();
		long runtime = (System.currentTimeMillis() - startTime) / 1000;
		System.out.printf("Runtime: %ds --> Total: %d, Done: %d, %f / item\n",
				runtime, total, finished, ((float) runtime) / finished);

		return total - finished;
	}

	private class Worker implements Runnable {

		private String file;

		public Worker(String file) {
			this.file = file;
		}

		public void run() {
			log.log(Level.INFO, new Date() + " " + "Start processing " + file);
			try {
				BufferedReader br;
				String outputFile = "";
				if (file.endsWith(".gz")) {
					br = new BufferedReader(new InputStreamReader(
							new GZIPInputStream(new FileInputStream(file))));
					outputFile = file.replace(".gz", "_cleaned.gz");
				} else {
					br = new BufferedReader(new FileReader(new File(file)));
					outputFile = file + "_cleaned.gz";
				}

				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						new GZIPOutputStream(new FileOutputStream(outputFile))));

				String line = "";
				long linksToUncrawledPages = 0;
				long linksToCrawledPages = 0;
				long numVertices = 0;

				while (br.ready()) {

					line = br.readLine();
					String[] tokens = line.split("\t");
					if (tokens.length < 2) {
						continue;
					} else {
						String origin = tokens[0];
						numVertices++;
						Integer id = pageMap.get(origin);
						if (id == null) {
							continue;
						}
						bw.write("" + id);
						String urlsString = tokens[1].replace("{", "").replace(
								"}", "");
						if (urlsString.length() > 0) {
							String[] urls = urlsString.split("\\),\\(");
							for (String url : urls) {
								url = url.replace("(", "").replace(")", "");
								Integer urlId = pageMap.get(url);
								if (urlId != null) {
									bw.write("\t" + urlId);
									linksToCrawledPages++;
								} else {
									linksToUncrawledPages++;
								}
							}
						}
						bw.write("\n");
					}
				}
				statusLog.log(Level.INFO, file + "\t" + numVertices + "\t"
						+ linksToCrawledPages + "\t" + linksToUncrawledPages);
				log.log(Level.INFO, new Date() + " " + "Done processing "
						+ file);
				bw.close();
				br.close();
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not process file " + file);
			}

		}

	}

	public static long hash(String string) {
		  long h = 1125899906842597L; // prime
		  int len = string.length();

		  for (int i = 0; i < len; i++) {
		    h = 31*h + string.charAt(i);
		  }
		  return h;
		}
	
}
