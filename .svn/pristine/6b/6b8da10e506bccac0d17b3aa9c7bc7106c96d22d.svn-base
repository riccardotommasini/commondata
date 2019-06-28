package org.webdatacommons.webgraph.big;

import it.unimi.dsi.big.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.big.webgraph.BVGraph;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.ImmutableSequentialGraph;
import it.unimi.dsi.big.webgraph.Transform;
import it.unimi.dsi.big.webgraph.algo.HyperBall;
import it.unimi.dsi.big.webgraph.algo.StronglyConnectedComponents;
import it.unimi.dsi.fastutil.ints.IntBigArrays;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.webdatacommons.webgraph.big.algo.ConnectedComponents;
import org.webdatacommons.webgraph.big.algo.KnowYourSiblings;
import org.webdatacommons.webgraph.big.algo.NodeIdReplacer;
import org.webdatacommons.webgraph.big.algo.OutDegree;
import org.webdatacommons.webgraph.big.algo.OutdegreeOnSubgraph;
import org.webdatacommons.webgraph.big.algo.OutdegreeOnSubgraph.ExecutionMode;

import de.uni_mannheim.informatik.dws.dwslib.collections.LimitedPriorityQueue;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;
import de.uni_mannheim.informatik.dws.dwslib.util.Tuple;

//import org.webgraphcommons.webgraph.big.algo.Degree;

/**
 * Testing the Webgraph Framework
 * 
 * @author rmeusel
 */
public class WebGraphProcessor {
	// static Logger log = Logger.getRootLogger();
	static Logger log = Logger.getLogger(WebGraphProcessor.class.getName());

	private static void hyperBall(String baseName, String transposedBaseName,
			String outBaseName, int log2m) throws IOException {

		// progress logger
		ProgressLogger pl0 = new ProgressLogger();
		// graph
		BVGraph graph = BVGraph.loadMapped(baseName, pl0);
		// transposed graph
		BVGraph tgraph = BVGraph.loadMapped(transposedBaseName, pl0);

		HyperBall hyper = new HyperBall(graph, tgraph, log2m, pl0);
		hyper.run();
		hyper.close();

		float[][] distances = hyper.sumOfDistances;
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(new File(outBaseName
						+ "_distance.gzip")))));
		for (int d1 = 0; d1 < distances.length; d1++) {
			for (int d2 = 0; d2 < distances[d1].length; d2++) {
				bw.write(distances[d1] + "\t" + distances[d2] + "\n");
			}
		}
		bw.close();
		float[][] harmonic = hyper.sumOfInverseDistances;
		bw = new BufferedWriter(
				new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(new File(outBaseName
								+ "_harmonic.gzip")))));
		for (int d1 = 0; d1 < harmonic.length; d1++) {
			for (int d2 = 0; d2 < harmonic[d1].length; d2++) {
				bw.write(harmonic[d1] + "\t" + harmonic[d2] + "\n");
			}
		}
		bw.close();
		System.out.println("Done.");
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
		for (long i = 0; i < graph.numNodes(); i++) {
			bw.write(i + "\t" + LongBigArrays.get(scc.component, i) + "\n");
		}
		bw.close();

		bw = new BufferedWriter(
				new FileWriter(new File(baseName + ".scc.dist")));

		long[][] sizes = scc.computeSizes();
		for (long j = 0; j < LongBigArrays.length(sizes); j++) {
			bw.write(LongBigArrays.get(sizes, j) + "\n");
		}
		bw.close();
		System.out.println("Done.");
	}

	private static void degree(String baseName, String outputPath)
			throws IOException {
		// progress logger
		ProgressLogger pl0 = new ProgressLogger();
		// graph
		BVGraph graph = BVGraph.loadMapped(baseName, pl0);

		if (graph == null) {
			System.out.println("Could not load graph!");
			return;
		}

		// degree
		ProgressLogger pl1 = new ProgressLogger();
		OutDegree degree = OutDegree.compute(graph, 0, pl1);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(new File(baseName
						+ ".degree.clu.gz")))));
		System.out.println("Writing output.");
		for (long i = 0; i < graph.numNodes(); i++) {
			bw.write(LongBigArrays.get(degree.degree, i) + "\n");
		}
		bw.close();

		System.out.println("Done.");

	}

	private static void connectedComponent(String baseName, String outputPath)
			throws IOException {
		connectedComponent(baseName, outputPath, 0);
	}

	private static void outdegreeOnSubgraph(String baseName,
			String subgraphIdFileOrDir, String outputPath, String mode)
			throws IOException {
		File outputDir = new File(outputPath);
		if (!outputDir.exists() || outputDir.isFile()) {
			System.out.println("Output dir does not exist or is a file.");
			System.exit(0);
		}
		ExecutionMode m = ExecutionMode.valueOf(mode);
		System.out.println("Execution mode is: " + m.toString());
		// progress logger
		ProgressLogger pl0 = new ProgressLogger();
		// graph
		BVGraph graph = BVGraph.loadMapped(baseName, pl0);
		// id list
		List<File> inputFiles = InputUtil.getFileList(subgraphIdFileOrDir);
		BufferedReader br = InputUtil.getBufferedReader(inputFiles);
		int[][] subGraph = IntBigArrays.newBigArray(graph.numNodes());
		System.out.println("Reading id list of subgraph.");
		long cnt = 0;
		while (br.ready()) {
			String line = br.readLine();
			cnt++;
			try {
				IntBigArrays.set(subGraph, Long.parseLong(line.trim()), 1);
			} catch (NumberFormatException e) {
				System.out.println("Could not parse " + line + ". Scipping");
			}
		}
		br.close();
		System.out.println("Done reading. Read " + cnt + " ids.");
		ProgressLogger pl1 = new ProgressLogger();
		OutdegreeOnSubgraph outdegree = new OutdegreeOnSubgraph(graph, m,
				subGraph, pl1);
		outdegree.compute(0);
		GZIPOutputStream outputstream = new GZIPOutputStream(
				new FileOutputStream(new File(outputDir,
						"degreeDistribution.gz")));
		outdegree.writeDistributionToStream(outputstream);
		outputstream.close();
	}

	private static void connectedComponent(String baseName, String outputPath,
			int numOfThreads) throws IOException {
		// progress logger
		ProgressLogger pl0 = new ProgressLogger();
		// graph
		BVGraph graph = BVGraph.loadMapped(baseName, pl0);

		// connected component
		ProgressLogger pl1 = new ProgressLogger();
		ConnectedComponents cc = ConnectedComponents.compute(graph,
				numOfThreads, pl1);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(baseName
				+ ".wcc.clu")));
		System.out.println("Writing output.");
		for (long i = 0; i < graph.numNodes(); i++) {
			bw.write(i + "\t" + LongBigArrays.get(cc.component, i) + "\n");
		}
		bw.close();

		bw = new BufferedWriter(
				new FileWriter(new File(baseName + ".wcc.dist")));
		long[][] sizes = cc.computeSizes();
		for (long j = 0; j < LongBigArrays.length(sizes); j++) {
			bw.write(LongBigArrays.get(sizes, j) + "\n");
		}
		bw.close();
		System.out.println("Done.");

	}

	private static void createSiblingInformation(String baseName,
			String outdegreeInputFile, String outputPath) {
		File outputDir = new File(outputPath);
		if (!outputDir.exists() || outputDir.isFile()) {
			System.out.println("Output dir does not exist or is a file.");
			System.exit(0);
		}
		try {
			// progress logger
			ProgressLogger pl0 = new ProgressLogger();
			// graph
			BVGraph graph = BVGraph.loadMapped(baseName, pl0);

			int[][] outdegreeClass1 = IntBigArrays
					.newBigArray(graph.numNodes());
			int[][] outdegreeClass2 = IntBigArrays
					.newBigArray(graph.numNodes());
			File inputFile = new File(outdegreeInputFile);
			System.out.println(new Date() + "");
			ProgressLogger plx = new ProgressLogger();
			plx.expectedUpdates = graph.numNodes();
			long progress = 0;
			BufferedReader br = InputUtil.getBufferedReader(inputFile);
			while (br.ready()) {
				progress++;
				String line = br.readLine();
				// line has to have at least 3 tokens where first token is the
				// id, 2 is class1 3 is class2
				String tok[] = line.split("\t");
				if (tok.length > 2) {
					long id = Long.parseLong(tok[0]);
					int class1Cnt = Integer.parseInt(tok[1]);
					int class2Cnt = Integer.parseInt(tok[2]);
					IntBigArrays.set(outdegreeClass1, id, class1Cnt);
					IntBigArrays.set(outdegreeClass2, id, class2Cnt);
				} else {
					System.out
							.println("Warningn: Line has not enough tokens. LINE: "
									+ line);
				}

				if (progress % 100000 == 0) {
					plx.set(progress);
				}
			}
			plx.done(progress);

			// progress logger
			ProgressLogger pl1 = new ProgressLogger();
			KnowYourSiblings kys = new KnowYourSiblings(graph, outdegreeClass1,
					outdegreeClass2, pl1);
			kys.compute(0);
			System.out.println("Writing outputfile.");
			GZIPOutputStream outputstream = new GZIPOutputStream(
					new FileOutputStream(new File(outputDir,
							"siblinginformation.gz")));
			kys.writeDistributionToStream(outputstream);
			outputstream.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static void convert(String arcListFileOrDirectory, String path) {
		try {
			File file = new File(arcListFileOrDirectory);
			List<File> files = new ArrayList<File>();
			if (file.isDirectory()) {
				log.info("Converting files from " + arcListFileOrDirectory
						+ " into " + path);
				for (File f : file.listFiles()) {
					if (f.isFile()) {
						files.add(f);
					}
				}
			} else {
				log.info("Converting " + arcListFileOrDirectory + " into "
						+ path);
				files.add(file);
			}
			// sorting based on the name
			Collections.sort(files);

			List<InputStream> streamsToProcess = new ArrayList<InputStream>(
					files.size());
			for (File f : files) {
				// distinguish inflated and deflated input files
				String ext = FilenameUtils.getExtension(f.getAbsolutePath());
				if (ext.equals("gz")) {
					// always add a the last position
					streamsToProcess.add(streamsToProcess.size(),
							new GZIPInputStream(new FileInputStream(f)));
				} else {
					// always add a the last position
					streamsToProcess.add(streamsToProcess.size(),
							new FileInputStream(f));
				}
			}
			InputStream is = new SequenceInputStream(
					Collections.enumeration(streamsToProcess));

			// output file name base
			String output = path;

			// convert and store in compressed form
			ProgressLogger pl = new ProgressLogger();
			BVGraph.store(ArcListASCIIGraph.loadOnce(is, 0), output, pl);
		} catch (FileNotFoundException e1) {
			log.error("File not found: " + arcListFileOrDirectory);
			System.exit(1);
		} catch (IOException e) {
			log.error("Error parsing file: " + arcListFileOrDirectory);
			e.printStackTrace();
			System.exit(1);
		}
		log.info("Conversion finished.");
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

			int batchSize = 1024 * 1024 * 1024;
			ImmutableGraph symGraph = Transform.symmetrizeOffline(graph,
					batchSize, tmpDir, pl0);

			BVGraph.store(symGraph, newName, pl0);

			log.info("Conversion finished");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void map(String baseName, String mappingFile,
			String outputBasename, int batchSize, String tmpDir) {
		try {
			// progress logger
			ProgressLogger pl0 = new ProgressLogger();

			// graph
			System.out.println("Loading graph");
			BVGraph graph = BVGraph.loadMapped(baseName, pl0);

			System.out.println("Loading mapping");
			long[][] mapping = LongBigArrays.newBigArray(graph.numNodes());
			System.out.println("Initializing map ...");
			for (long i = 0; i < graph.numNodes(); i++) {
				LongBigArrays.set(mapping, i, -1);
			}
			BufferedReader br = InputUtil.getBufferedReader(new File(
					mappingFile));
			long lineCnt = 0;
			long maxmap = 0;
			while (br.ready()) {
				String line = br.readLine();
				String tok[] = line.split("\t");
				long originalId = 0;
				long mapId = 0;
				if (tok.length > 1) {
					originalId = Long.parseLong(tok[0]);
					mapId = Long.parseLong(tok[1]);
				} else {
					originalId = lineCnt;
					mapId = Long.parseLong(tok[0]);
				}
				if (mapId > maxmap)
					maxmap = mapId;
				LongBigArrays.set(mapping, originalId, mapId);
				lineCnt++;
			}
			ImmutableSequentialGraph mappedGraph = Transform.mapOffline(graph,
					mapping, batchSize, new File(tmpDir), pl0);
			BVGraph.store(mappedGraph, outputBasename, pl0);
			System.out.println("Storing graph");

			log.info("Done.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static void shrink(String baseName, String mappingFile,
			String outputDir) {
		try {
			// progress logger
			ProgressLogger pl0 = new ProgressLogger();
			File outputFile = new File(outputDir);
			if (!outputFile.isDirectory()) {
				System.out.println("File is not a dir.");
				System.exit(0);
			}
			// graph
			System.out.println("Loading graph");
			BVGraph graph = BVGraph.loadMapped(baseName, pl0);

			System.out.println("Loading mapping");
			int[][] mapping = IntBigArrays.newBigArray(graph.numNodes());
			System.out.println("Initializing map ...");
			for (long i = 0; i < graph.numNodes(); i++) {
				IntBigArrays.set(mapping, i, -1);
			}
			BufferedReader br = InputUtil.getBufferedReader(new File(
					mappingFile));
			long lineCnt = 0;
			int maxmap = 0;
			while (br.ready()) {
				String line = br.readLine();
				String tok[] = line.split("\t");
				long originalId = 0;
				int shrinkId = 0;
				if (tok.length > 1) {
					originalId = Long.parseLong(tok[0]);
					shrinkId = Integer.parseInt(tok[1]);
				} else {
					originalId = lineCnt;
					shrinkId = Integer.parseInt(tok[0]);
				}
				if (shrinkId > maxmap)
					maxmap = shrinkId;
				IntBigArrays.set(mapping, originalId, shrinkId);
				lineCnt++;
			}
			// System.out.println("Initializing mapped graph");
			// ArrayListMutableGraph mappedGraph = new ArrayListMutableGraph(
			// maxmap);
			// mappedGraph.addNodes(maxmap);

			NodeIdReplacer nr = new NodeIdReplacer(mapping, graph, outputFile);
			nr.compute(0);
			System.out.println("Storing graph");
			// nr.storeGraph();

			log.info("Done.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

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

		options.addOption(OptionBuilder.hasArgs(2).create("topList"));
		options.addOption(OptionBuilder.hasArgs(1).create("aggregate"));
		options.addOption(OptionBuilder.hasArgs(1).create("sgodstat"));
		options.addOption(OptionBuilder.hasArgs(3).create("siblings"));
		options.addOption(OptionBuilder.hasArgs(2).create("convert"));
		options.addOption(OptionBuilder.hasArgs(3).create("shrink"));
		options.addOption(OptionBuilder.hasArgs(4).create("map"));

		options.addOption(OptionBuilder.hasArgs(3).create("weakcomponent"));

		options.addOption(OptionBuilder.hasArgs(2).create("strongcomponent"));

		options.addOption(OptionBuilder.hasArgs(2).create("degree"));
		options.addOption(OptionBuilder.hasArgs(3).create("symmetrize"));
		options.addOption(OptionBuilder.hasArgs(3).create("transpose"));

		options.addOption(OptionBuilder.hasArgs(4).create("sgod"));
		options.addOption(OptionBuilder.hasArgs(4).create("hyperball"));

		/* parse command line parameters */
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			String[] io = cmd.getOptionValues("sgod");
			if (io != null) {
				if (io.length != 4) {
					System.err
							.println("Usage: WebgraphTest -sgod <INPUTFILEORDIR> <SUBGRAPHIDINPUTFILEORDIR> <OUTPUTPATH> <MODE>");
					System.exit(1);
				}
				outdegreeOnSubgraph(io[0], io[1], io[2], io[3]);
			}

			io = cmd.getOptionValues("hyperball");
			if (io != null) {
				if (io.length != 4) {
					System.err
							.println("Usage: WebgraphTest -hyperball <GRAPH> <TRANSPOSEDGRAPH> <OUTPUTBASENAME> <LOG2M>");
					System.exit(1);
				}
				hyperBall(io[0], io[1], io[2], Integer.parseInt(io[3]));
			}

			io = cmd.getOptionValues("shrink");
			if (io != null) {
				if (io.length != 3) {
					System.err
							.println("Usage: WebgraphTest -shrink <GRAPHBASENAME> <MAPPINGFILE> <OUTPUTPATH>");
					System.exit(1);
				}
				shrink(io[0], io[1], io[2]);
			}

			io = cmd.getOptionValues("map");
			if (io != null) {
				if (io.length != 5) {
					System.err
							.println("Usage: WebgraphTest -map <GRAPHBASENAME> <MAPPINGFILE> <OUTPUTBASENAME> <BATCHSIZE> <TMPDIR>");
					System.exit(1);
				}
				map(io[0], io[1], io[2], Integer.parseInt(io[3]), io[4]);
			}

			io = cmd.getOptionValues("siblings");
			if (io != null) {
				if (io.length != 3) {
					System.err
							.println("Usage: WebgraphTest -siblings <TRANSPOSEDGRAPHBASENAME> <OUTDEGREEFILE> <OUTPUTPATH>");
					System.exit(1);
				}
				createSiblingInformation(io[0], io[1], io[2]);
			}

			// conversion?
			io = cmd.getOptionValues("convert");
			if (io != null) {
				if (io.length != 2) {
					System.err
							.println("Usage: WebgraphTest -convert <INPUTFILEORDIR> <OUTPUTPATH>");
					System.exit(1);
				}
				convert(io[0], io[1]);
			}
			io = cmd.getOptionValues("weakcomponent");
			if (io != null) {
				if (io.length < 2) {
					System.err
							.println("Usage: WebgraphTest -weakcomponent <INPUTFILE> <OUTPUTPATH> <NUMOFTHREADS>");
					System.exit(1);
				}
				if (io.length == 2) {
					connectedComponent(io[0], io[1]);
				} else {
					connectedComponent(io[0], io[1], Integer.parseInt(io[2]));
				}
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
			io = cmd.getOptionValues("topList");
			if (io != null) {
				if (io.length != 2) {
					System.err
							.println("Usage: WebgraphTest -topList <INPUTFILE> <NUMTOPITEMS>");
					System.exit(1);
				}
				getTopItems(io[0], Integer.parseInt(io[1]));
			}
			io = cmd.getOptionValues("sgodstat");
			if (io != null) {
				if (io.length != 1) {
					System.err
							.println("Usage: WebgraphTest -sgodstat <INPUTFILE>");
					System.exit(1);
				}
				OutdegreeOnSubgraph.calculateStatistics(io[0]);
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

			io = cmd.getOptionValues("degree");
			if (io != null) {
				if (io.length != 2) {
					System.err
							.println("Usage: WebgraphTest -degree <INPUTFILE> <OUTPUTPATH>");
					System.exit(1);
				}
				degree(io[0], io[1]);
			}

			io = cmd.getOptionValues("symmetrize");
			if (io != null) {
				if (io.length != 3) {
					System.err
							.println("Usage: WebgraphTest -symmetrize <INPUTFILE> <OUTPUTPATH> <TMPDIR>");
					System.exit(1);
				}
				symmetrize(io[0], io[1], io[2]);
			}

			io = cmd.getOptionValues("transpose");
			if (io != null) {
				if (io.length != 3) {
					System.err
							.println("Usage: WebgraphTest -transpose <INPUTFILE> <OUTPUTPATH> <TMPDIR>");
					System.exit(1);
				}
				transpose(io[0], io[1], io[2]);
			}
		} catch (ParseException e) {
			System.err.println("Usage: WebgraphTest [OPTIONS]");
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void getTopItems(String file, int topItems)
			throws IOException {

		BufferedReader br = InputUtil.getBufferedReader(new File(file));

		LimitedPriorityQueue<Tuple<Long, Long>> queue = new LimitedPriorityQueue<Tuple<Long, Long>>(
				topItems, new Comparator<Tuple<Long, Long>>() {
					@Override
					public int compare(Tuple<Long, Long> o1,
							Tuple<Long, Long> o2) {
						return Long.compare(o1.getSecondElement(),
								o2.getSecondElement());
					}
				});

		long lncnt = 0;
		while (br.ready()) {
			String line = br.readLine();
			Long key = Long.parseLong(line);
			queue.add(new Tuple<Long, Long>(lncnt, key));
			if (++lncnt % 100000 == 0) {
				System.out.println(new Date() + " Read " + lncnt + " lines.");
			}
		}
		System.out.println("Writing output ...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				(file.endsWith(".gz") ? file.replace(".gz", "") : file)
						+ "_top" + topItems)));
		for (int i = 0; i < topItems; i++) {
			Tuple<Long, Long> t = queue.poll();
			if (t != null) {
				bw.write(t.getFirstElement() + "\t" + t.getSecondElement()
						+ "\n");
			}
		}
		bw.close();
		System.out.println("Done.");
	}

	// aggregate list of components
	private static void aggregateList(String file) throws IOException {
		BufferedReader br = InputUtil.getBufferedReader(new File(file));
		HashMap<Long, Integer> aggregatedList = new HashMap<Long, Integer>();
		long lncnt = 0;
		while (br.ready()) {
			lncnt++;
			if (lncnt % 100000 == 0) {
				System.out.println(new Date() + " Read " + lncnt + " lines.");
			}
			String line = br.readLine();
			Long key = Long.parseLong(line);
			int num = 1;
			if (aggregatedList.containsKey(key)) {
				num += aggregatedList.get(key);
			}
			aggregatedList.put(key, num);
		}
		br.close();
		List<Long> orderedList = new ArrayList<Long>(aggregatedList.keySet());
		System.out.println("Sorting data ...");
		Collections.sort(orderedList);

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				(file.endsWith(".gz") ? file.replace(".gz", "") : file)
						+ "_agg")));
		System.out.println("Writing output ...");
		for (Long key : orderedList) {
			bw.write(key + "\t" + aggregatedList.get(key) + "\n");
		}
		bw.close();
		System.out.println("Done.");
	}
}
