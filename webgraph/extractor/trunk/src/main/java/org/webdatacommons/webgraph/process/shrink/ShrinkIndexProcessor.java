package org.webdatacommons.webgraph.process.shrink;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.common.net.InternetDomainName;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;

/**
 * Reads an URL index of the format (<URL>\t<ID>) and computes the following
 * lists
 * <ul>
 * <li>Pay-Level-Domains</li>
 * <li>Full Domain</li>
 * <li>First Sub-Domain</li>
 * </ul>
 * As an example this processor would calculate from the url:
 * http://dws.informatik.uni-mannheim.de/index.php the following reductions:
 * <ul>
 * <li>uni-mannheim.de</li>
 * <li>dws.informatik.uni-mannheim.de</li>
 * <li>informatik.uni-mannheim.de</li>
 * </ul>
 * Please note, if the input is ordered occurrences of the same reduction-entity
 * are not written twice into the output file to reduce the size and as this
 * processor is originally designed to create sub-indexes where duplicates are
 * removed later on anyway. In addition, if a Pay-Level-Domain could not be
 * calculated based on malformed URLs not following the restrictions of
 * {@link InternetDomainName} the entry is skipped.
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 */
public class ShrinkIndexProcessor {

	private List<File> filesToProcess = new ArrayList<File>();
	private String fileOrDirectory;
	private File outPutDirectory;
	private File pldOutputDir;
	private File sdOutputDir;
	private File sd1OutputDir;
	private File failOutputDir;
	private boolean makePredecessorOptimization;
	private int threads;
	private int urlIndex;
	private static Logger log = Logger.getLogger("IndexProcessor.class");
	private static Logger statusLog = Logger.getLogger("IndexProcessor.class");

	/**
	 * Initialize instance
	 * 
	 * @param fileOrDirectory
	 *            Input file or directory.
	 * @param outPutDirectory
	 *            Output directory
	 * @param urlIndex
	 *            The index of the URL within the file
	 * @param makePredecessorOpt
	 *            states if the current written line should be compared with the
	 *            previous line and in case of equality skipped
	 * @param threads
	 *            Number of parallel threads. If 0 number of cores is taken.
	 * @throws FileNotFoundException
	 *             If either inputfiles could not be found or output directory
	 *             is not present.
	 */
	public ShrinkIndexProcessor(String fileOrDirectory, String outPutDirectory,
			int urlIndex, boolean makePredecessorOpt, int threads)
			throws FileNotFoundException {
		this.fileOrDirectory = fileOrDirectory;
		this.urlIndex = urlIndex;
		this.makePredecessorOptimization = makePredecessorOpt;
		File output = new File(outPutDirectory);
		if (output.exists() && output.isDirectory()) {
			this.outPutDirectory = output;
		} else {
			System.out
					.println("Output directory does not exists or is no directory.");
			throw new FileNotFoundException();
		}
		if (threads < 0) {
			System.out
					.println("Number of threads has to be positive or 0 for number of available cores");
		} else if (threads == 0) {
			this.threads = Runtime.getRuntime().availableProcessors();
		} else {
			this.threads = threads;
		}
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

	private class Worker implements Runnable {

		private String file;

		public Worker(String file) {
			this.file = file;
		}

		public void run() {
			log.log(Level.INFO, new Date() + " " + "Start processing " + file);
			File file = new File(this.file);
			try {
				BufferedReader br;
				if (file.getName().endsWith("gz")) {
					br = new BufferedReader(new InputStreamReader(
							new GZIPInputStream(new FileInputStream(file))));
				} else {
					br = new BufferedReader(new FileReader(file));
				}
				BufferedWriter pldwriter = new BufferedWriter(
						new OutputStreamWriter(new GZIPOutputStream(
								new FileOutputStream(new File(pldOutputDir,
										"pld-"
												+ file.getName().replace(".gz",
														"") + ".gz")))));
				BufferedWriter sdwriter = new BufferedWriter(
						new OutputStreamWriter(new GZIPOutputStream(
								new FileOutputStream(new File(sdOutputDir,
										"sd-"
												+ file.getName().replace(".gz",
														"") + ".gz")))));
				BufferedWriter sd1writer = new BufferedWriter(
						new OutputStreamWriter(new GZIPOutputStream(
								new FileOutputStream(new File(sd1OutputDir,
										"sd1-"
												+ file.getName().replace(".gz",
														"") + ".gz")))));
				BufferedWriter failedwriter = new BufferedWriter(
						new OutputStreamWriter(new GZIPOutputStream(
								new FileOutputStream(new File(failOutputDir,
										"fail-"
												+ file.getName().replace(".gz",
														"") + ".gz")))));
				String pldt0 = "";
				String sdt0 = "";
				String sd1t0 = "";
				while (br.ready()) {
					String line = br.readLine();
					String url = line.split("\t")[urlIndex];
					url = DomainUtil.uncompress(url);
					String pld = DomainUtil.getPayLevelDomainFromWholeURL(url);
					if (pld == DomainUtil.INVALID_URL) {
						failedwriter.write(line + "\n");
						continue;
					}
					if (pld != DomainUtil.INVALID_URL
							&& (makePredecessorOptimization ? !pldt0
									.equals(pld) : true)) {
						pldwriter.write(pld + "\n");
						pldt0 = pld;
					}
					String sd = DomainUtil.getSubDomainFromWholeUrl(url);
					if (sd != DomainUtil.INVALID_URL
							&& (makePredecessorOptimization ? !sdt0.equals(sd)
									: true)) {
						sdwriter.write(sd + "\n");
						sdt0 = sd;
					}
					String sd1 = DomainUtil.getFirstSubDomainFromWholeUrl(url);
					if (sd1 != DomainUtil.INVALID_URL
							&& (makePredecessorOptimization ? !sd1t0
									.equals(sd1) : true)) {
						sd1writer.write(sd1 + "\n");
						sd1t0 = sd1;
					}
				}
				pldwriter.close();
				sdwriter.close();
				sd1writer.close();
				failedwriter.close();
				br.close();
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not process file " + file);
			}
		}
	}

	private void fillFileList() throws FileNotFoundException, IOException {
		// check if input is file or dir
		File input = new File(fileOrDirectory);
		if (!input.exists()) {
			log.log(Level.SEVERE, input + " file does not exist!");
			throw new FileNotFoundException();
		}
		if (input.isDirectory()) {
			for (File f : input.listFiles()) {
				if (f.isFile()) {
					filesToProcess.add(f);
				}
			}
		} else {
			filesToProcess.add(input);
		}
	}

	private void createOutputDirs() throws FileNotFoundException {
		File pldOutput = new File(outPutDirectory, "pld");
		if (pldOutput.mkdir()) {
			pldOutputDir = pldOutput;
		} else {
			log.log(Level.SEVERE, "Could not create pld output file.");
			throw new FileNotFoundException();
		}
		File sdOutput = new File(outPutDirectory, "sd");
		if (sdOutput.mkdir()) {
			sdOutputDir = sdOutput;
		} else {
			log.log(Level.SEVERE, "Could not create sd output file.");
			throw new FileNotFoundException();
		}
		File sd1Output = new File(outPutDirectory, "sd1");
		if (sd1Output.mkdir()) {
			sd1OutputDir = sd1Output;
		} else {
			log.log(Level.SEVERE, "Could not create pld output file.");
			throw new FileNotFoundException();
		}
		File failOutput = new File(outPutDirectory, "fail");
		if (failOutput.mkdir()) {
			failOutputDir = failOutput;
		} else {
			log.log(Level.SEVERE, "Could not create fail output file.");
			throw new FileNotFoundException();
		}
	}

	/**
	 * Initializes the shrinking and calculation of the different abstraction
	 * levels for the given input index.
	 * 
	 * @throws FileNotFoundException
	 *             If the output directories could not be created
	 * @throws IOException
	 *             If the files could not be written to the output directories.
	 */
	public void process() throws FileNotFoundException, IOException {
		long startTime = new Date().getTime();
		log.log(Level.INFO, new Date() + " " + "Starting.");
		// file list to work
		fillFileList();
		// create output directories
		createOutputDirs();
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(threads);
		for (File f : filesToProcess) {
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

	public static void main(String[] args) throws NumberFormatException,
			IOException {
		System.out
				.println("ShrinkIndexProcess <INPUTFILEORDIR> <OUTPUTDIR> <URLINDEX> <OPTIZEDWRITING (0|1)> <THREADS>");
		ShrinkIndexProcessor p = new ShrinkIndexProcessor(args[0], args[1],
				Integer.parseInt(args[2]), Boolean.parseBoolean(args[3]),
				Integer.parseInt(args[4]));
		p.process();
	}

}
