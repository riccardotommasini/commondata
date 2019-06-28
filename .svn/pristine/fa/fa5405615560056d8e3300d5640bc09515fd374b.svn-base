package org.webdatacommons.webgraph;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.algo.ConnectedComponents;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponents;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

/**
 * Testing the Webgraph Framework
 * 
 * @author sseufert
 */
public class WebgraphTest {
	// static Logger log = Logger.getRootLogger();
	static Logger log = Logger.getLogger(WebgraphTest.class.getName());

	private static void convertFromUnsortedUndistinctFile(String dir,
			int numNodes, String output) throws IOException {
		System.out.println("Creating empty graph...");
		ArrayListMutableGraph graph = new ArrayListMutableGraph(numNodes);
		List<File> fileList = InputUtil.getFileList(dir);
		long lncnt = 0;
		for (File f : fileList) {
			BufferedReader br = InputUtil.getBufferedReader(f);
			System.out.println("Start parsing file " + f.toString());

			while (br.ready()) {
				if (++lncnt % 10000000 == 0) {
					System.out.println("Parsed " + lncnt + " lines.");
				}
				String line = br.readLine();
				String tok[] = line.split("\t");
				int startNode = Integer.parseInt(tok[0]);
				for (int i = 1; i < tok.length; i++) {
					int target = Integer.parseInt(tok[i]);
					try {
						graph.addArc(startNode, target);
					} catch (IllegalArgumentException e) {
						// arc already exists
					}
				}
			}
		}
		System.out.println("Storing graph.");
		BVGraph.store(graph.immutableView(), output, new ProgressLogger());
	}

	private static void strongComponent(String baseName, String outputPath)
			throws IOException {
		// progress logger
		ProgressLogger pl0 = new ProgressLogger();
		// graph
		BVGraph graph = BVGraph.loadMapped(baseName, pl0);
		// connected component
		ProgressLogger pl1 = new ProgressLogger();

		StronglyConnectedComponents scc = StronglyConnectedComponents.compute(
				graph, false, pl1);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseName
				+ ".scc.clu")));
		System.out.println("Writing output.");
		for (int i = 0; i < graph.numNodes(); i++) {
			bw.write(i + "\t" + scc.component[i] + "\n");
		}
		bw.close();

		bw = new BufferedWriter(
				new FileWriter(new File(baseName + ".scc.dist")));
		for (int j : scc.computeSizes()) {
			bw.write(j + "\n");
		}
		bw.close();
		System.out.println("Done.");
	}

	private static void connectedComponent(String baseName, String outputPath)
			throws IOException {
		// progress logger
		ProgressLogger pl0 = new ProgressLogger();
		// graph
		BVGraph graph = BVGraph.loadMapped(baseName, pl0);
		// connected component
		ProgressLogger pl1 = new ProgressLogger();
		ConnectedComponents cc = ConnectedComponents.compute(graph, 0, pl1);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseName
				+ ".wcc.clu")));
		System.out.println("Writing output.");
		for (int i = 0; i < graph.numNodes(); i++) {
			bw.write(i + "\t" + cc.component[i] + "\n");
		}
		bw.close();

		bw = new BufferedWriter(
				new FileWriter(new File(baseName + ".wcc.dist")));
		for (int j : cc.computeSizes()) {
			bw.write(j + "\n");
		}
		bw.close();
		System.out.println("Done.");

	}

	private static void convert(String arcListFile, String path) {
		log.info("Converting " + arcListFile + " into " + path);

		/* parse and store arc list graph */
		InputStream is;
		try {
			// distinguish inflated and deflated input files
			String ext = FilenameUtils.getExtension(arcListFile);
			if (ext.equals("gz")) {
				is = new GZIPInputStream(new FileInputStream(arcListFile));
			} else {
				is = new FileInputStream(arcListFile);
			}

			// output file name base
			String output = path + File.separator
					+ FilenameUtils.getBaseName(arcListFile);

			// convert and store in compressed form
			ProgressLogger pl = new ProgressLogger();
			BVGraph.store(ArcListASCIIGraph.loadOnce(is, 0), output, pl);
		} catch (FileNotFoundException e1) {
			log.error("File not found: " + arcListFile);
			System.exit(1);
		} catch (IOException e) {
			log.error("Error parsing file: " + arcListFile);
			e.printStackTrace();
			System.exit(1);
		}
		log.info("Conversion finished.");
	}

	private static void transpose(String baseName, String newName,
			String tmpDirName) {
		try {
			// progress logger
			ProgressLogger pl0 = new ProgressLogger();
			// graph
			BVGraph graph = BVGraph.loadMapped(baseName, pl0);
			File tmpDir = new File(tmpDirName);
			if (!tmpDir.exists() || tmpDir.isFile()) {
				System.out.println("Temp Dir does not exist or is no dir.");
				return;
			}

			int batchSize = 1024 * 1024 * 1024;
			ImmutableGraph transGraph = Transform.transposeOffline(graph,
					batchSize, tmpDir, pl0);

			BVGraph.store(transGraph, newName, pl0);

			log.info("Conversion finished");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void symmetrize(String baseName, String newName,
			String tmpDirName) {
		try {
			// progress logger
			ProgressLogger pl0 = new ProgressLogger();
			// graph
			BVGraph graph = BVGraph.loadMapped(baseName, pl0);
			File tmpDir = new File(tmpDirName);
			if (!tmpDir.exists() || tmpDir.isFile()) {
				System.out.println("Temp Dir does not exist or is no dir.");
				return;
			}

			int batchSize = 256 * 1024 * 1024;
			ImmutableGraph symGraph = Transform.symmetrizeOffline(graph,
					batchSize, tmpDir, pl0);

			BVGraph.store(symGraph, newName, pl0);

			log.info("Conversion finished");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            command line arguments
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {

		/* set up logger */
		PropertyConfigurator.configure("config/log4j.properties");

		/* set up command line parameters */
		Options options = new Options();

		options.addOption(OptionBuilder.hasArgs(1).create("aggregate"));
		options.addOption(OptionBuilder.hasArgs(2).create("convert"));
		options.addOption(OptionBuilder.hasArgs(3).create("convertugly"));

		options.addOption(OptionBuilder.hasArgs(2).create("weakcomponent"));

		options.addOption(OptionBuilder.hasArgs(2).create("strongcomponent"));
		options.addOption(OptionBuilder.hasArgs(3).create("transpose"));
		options.addOption(OptionBuilder.hasArgs(3).create("symmetrize"));

		/* parse command line parameters */
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			// conversion?
			String[] io = cmd.getOptionValues("convert");
			if (io != null) {
				if (io.length != 2) {
					System.err
							.println("Usage: WebgraphTest -convert <INPUTFILE> <OUTPUTPATH>");
					System.exit(1);
				}
				convert(io[0], io[1]);
			}

			// conversion?
			io = cmd.getOptionValues("convertugly");
			if (io != null) {
				if (io.length != 3) {
					System.err
							.println("Usage: WebgraphTest -convertugly <INPUTFILE> <NUMNODES> <OUTPUTPATH>");
					System.exit(1);
				}
				convertFromUnsortedUndistinctFile(io[0],
						Integer.parseInt(io[1]), io[2]);
			}
			io = cmd.getOptionValues("weakcomponent");
			if (io != null) {
				if (io.length != 2) {
					System.err
							.println("Usage: WebgraphTest -weakcomponent <INPUTFILE> <OUTPUTPATH>");
					System.exit(1);
				}
				connectedComponent(io[0], io[1]);
			}
			io = cmd.getOptionValues("aggregate");
			if (io != null) {
				if (io.length != 1) {
					System.err
							.println("Usage: WebgraphTest -aggregate <INPUTFILE>");
					System.exit(1);
				}
				aggregateList(io[0]);
			}

			io = cmd.getOptionValues("strongcomponent");
			if (io != null) {
				if (io.length != 2) {
					System.err
							.println("Usage: WebgraphTest -strongcomponent <INPUTFILE> <OUTPUTPATH>");
					System.exit(1);
				}
				strongComponent(io[0], io[1]);
			}

			io = cmd.getOptionValues("transpose");
			if (io != null) {
				if (io.length != 3) {
					System.err
							.println("Usage: WebgraphTest -transpose <INPUT> <OUTPUT> <TEMPDIR>");
					System.exit(1);
				}
				transpose(io[0], io[1], io[2]);
			}

			io = cmd.getOptionValues("symmetrize");
			if (io != null) {
				if (io.length != 3) {
					System.err
							.println("Usage: WebgraphTest -symmetrize <INPUT> <OUTPUT> <TEMPDIR>");
					System.exit(1);
				}
				symmetrize(io[0], io[1], io[2]);
			}

		} catch (ParseException e) {
			System.err.println("Usage: WebgraphTest [OPTIONS]");
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// aggregate list of components
	private static void aggregateList(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		HashMap<Integer, Integer> aggregatedList = new HashMap<Integer, Integer>();
		int lncnt = 0;
		while (br.ready()) {
			lncnt++;
			if (lncnt % 100000 == 0) {
				System.out.println(new Date() + " Read " + lncnt + " lines.");
			}
			String line = br.readLine();
			Integer key = Integer.parseInt(line);
			int num = 1;
			if (aggregatedList.containsKey(key)) {
				num += aggregatedList.get(key);
			}
			aggregatedList.put(key, num);
		}
		br.close();
		List<Integer> orderedList = new ArrayList<Integer>(
				aggregatedList.keySet());
		Collections.sort(orderedList);

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file
				+ "_agg")));
		for (Integer key : orderedList) {
			bw.write(key + "\t" + aggregatedList.get(key) + "\n");
		}
		bw.close();
	}
}
