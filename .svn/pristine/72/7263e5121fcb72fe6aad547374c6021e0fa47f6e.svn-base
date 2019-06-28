package org.webdatacommons.webgraph.mapper;

import it.unimi.dsi.fastutil.ints.IntBigArrays;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.uni_mannheim.informatik.dws.dwslib.framework.Processor;
import de.uni_mannheim.informatik.dws.dwslib.util.BufferedChunkingWriter;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class FeatureMapper extends Processor<String> {

	public FeatureMapper(String inputFile, String parentInformationFile,
			String siblingInformationFile, BufferedChunkingWriter writer,
			long numOfIds, int threads) throws IOException {
		super(threads);
		bw = writer;
		this.inputFileOrDir = inputFile;
		this.numOfIds = numOfIds;
		numNonSemParent = IntBigArrays.newBigArray(numOfIds);
		numNonSemSiblings = IntBigArrays.newBigArray(numOfIds);
		numSemParent = IntBigArrays.newBigArray(numOfIds);
		numSemSiblings = IntBigArrays.newBigArray(numOfIds);
		loadFileParents(parentInformationFile);
		loadFileSiblings(siblingInformationFile);
	}

	private void loadFileSiblings(String file) throws IOException {
		BufferedReader br = InputUtil.getBufferedReader(new File(file));
		System.out.println(new Date() + " Start loading file " + file);
		long ercnt = 0;
		long lineCnt = 0;
		while (br.ready()) {
			if (++lineCnt % 100000000 == 0) {
				System.out.println(new Date() + " loaded " + lineCnt
						+ " lines.");
			}
			String tok[] = br.readLine().split("\t");
			if (tok.length > 2) {
				IntBigArrays.set(numSemSiblings, Long.parseLong(tok[0]),
						Integer.parseInt(tok[1]));
				IntBigArrays.set(numNonSemSiblings, Long.parseLong(tok[0]),
						Integer.parseInt(tok[2]));
			} else {
				ercnt++;
			}
		}
		System.out.println(new Date() + " Loaded " + file + ". Could not read "
				+ ercnt + " lines.");
	}

	private void loadFileParents(String file) throws IOException {
		BufferedReader br = InputUtil.getBufferedReader(new File(file));
		System.out.println(new Date() + " Start loading file " + file);
		long ercnt = 0;
		long lineCnt = 0;
		while (br.ready()) {
			if (++lineCnt % 100000000 == 0) {
				System.out.println(new Date() + " loaded " + lineCnt
						+ " lines.");
			}
			String tok[] = br.readLine().split("\t");
			if (tok.length > 2) {
				IntBigArrays.set(numSemParent, Long.parseLong(tok[0]),
						Integer.parseInt(tok[1]));
				IntBigArrays.set(numNonSemParent, Long.parseLong(tok[0]),
						Integer.parseInt(tok[2]));
			} else {
				ercnt++;
			}
		}
		System.out.println(new Date() + " Loaded " + file + ". Could not read "
				+ ercnt + " lines.");
	}

	private int[][] numSemParent;
	private int[][] numNonSemParent;
	private int[][] numSemSiblings;
	private int[][] numNonSemSiblings;
	private String inputFileOrDir;
	private BufferedChunkingWriter bw;
	private long numOfIds;

	@Override
	protected List<String> fillListToProcess() {
		try {
			return InputUtil.getFileReferenceList(inputFileOrDir);
		} catch (IOException e) {
			System.out.println("Could not read input file.");
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	@Override
	protected void process(String file) {
		try {
			BufferedReader br = InputUtil.getBufferedReader(new File(file));
			int ercnt = 0;
			while (br.ready()) {
				String line = br.readLine();
				if (line.equals("")) {
					continue;
				}
				String tok[] = line.split("\t");
				// at least we need id, domain, graph, query, fragment, label
				if (tok != null && tok.length > 5) {
					try {
						String outputLine = createOutputLine(tok);
						bw.write(outputLine);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Could not create output for line "
								+ tok.toString());
					}
				} else {
					ercnt++;
				}
			}
			System.out.println("Read file " + file + ". But could not read "
					+ ercnt + " lines.");
		} catch (IOException e) {
			System.out.println("Could not read file " + file);
			e.printStackTrace();
		}

	}

	// HEADER
	// ID\tLABEL\tDOMAIN\tPATH\tQUERY\tFRAGMENT\tRDFa\tMD\tMF\tPsem\tPnonsem\tSsem\tSnonsem\n
	private String createOutputLine(String tok[]) {
		StringBuilder sb = new StringBuilder();
		// get id
		long id = Long.parseLong(tok[0]);
		sb.append(id);
		sb.append("\t");
		// label
		sb.append(tok[5] == null ? "" : tok[5]);
		sb.append("\t");
		// domain
		sb.append(tok[1] == null ? "" : tok[1]);
		sb.append("\t");
		// graph
		sb.append(tok[2] == null ? "" : tok[2]);
		sb.append("\t");
		// query
		sb.append(tok[3] == null ? "" : tok[3]);
		sb.append("\t");
		// fragment
		sb.append(tok[4] == null ? "" : tok[4]);
		sb.append("\t");
		// rdfa
		sb.append(tok.length < 7 ? "0" : tok[6]);
		sb.append("\t");
		// md
		sb.append(tok.length < 8 ? "0" : tok[7]);
		sb.append("\t");
		// mf
		sb.append(tok.length < 9 ? "0" : tok[8]);
		sb.append("\t");
		// parent sem
		sb.append(IntBigArrays.get(numSemParent, id));
		sb.append("\t");
		// parent nonsem
		sb.append(IntBigArrays.get(numNonSemParent, id));
		sb.append("\t");
		// sibling sem
		sb.append(IntBigArrays.get(numSemSiblings, id));
		sb.append("\t");
		// sibling nonsem
		sb.append(IntBigArrays.get(numNonSemSiblings, id));
		sb.append("\n");
		return sb.toString();
	}

	public static void main(String[] args) throws NumberFormatException,
			IOException {
		if (args == null || args.length < 6) {
			System.out
					.println("USAGE: inputFile parentFile siblingFile outputDir outputPrefix numofids");
			System.exit(0);
		}
		BufferedChunkingWriter bw = new BufferedChunkingWriter(
				new File(args[3]), args[4], 100);
		FeatureMapper fm = new FeatureMapper(args[0], args[1], args[2], bw,
				Long.parseLong(args[5]), 0);
		fm.process();
		bw.close();
	}
}
