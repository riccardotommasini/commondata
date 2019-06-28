package org.webdatacommons.webgraph.process.pajek;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class Sparse2Pajek {

	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			System.out
					.println("Missing input values. Input should be minimum sparse vector file. Optional give the mode (int). Mode 1 = all vertices (default), Mode 2 = crawled vertices.");
			return;
		}
		String inputFile = args[0];
		int mode = 1;
		if (args.length > 1) {
			mode = Integer.parseInt(args[1]);
		}
		boolean weight = true;
		if (args.length > 2) {
			weight = Boolean.parseBoolean(args[2]);
		}
		if (mode == 1) {
			transformAll(inputFile, weight);
		} else if (mode == 2) {
			transformCrawled(inputFile, weight);
		} else {
			System.out.println("Unknown mode.");
		}
		// transform(args[0]);
	}

	private static void transformAll(String inputFile, boolean weight) {
		System.out.println(new Date()
				+ " Transformation of all vertices started ...");
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					inputFile)));
			BufferedWriter verticesWriter = new BufferedWriter(new FileWriter(
					new File(inputFile + "_vertices")));
			BufferedWriter arcWriter = new BufferedWriter(new FileWriter(
					new File(inputFile + "_arcs")));
			arcWriter.write("*Arcs :1 \"Links\"\n");
			HashMap<Long, Boolean> crawled = new HashMap<Long, Boolean>();
			HashMap<String, Long> vertices = new HashMap<String, Long>();
			long vertexCnt = 0;
			long lineCnt = 0;
			while (br.ready()) {
				lineCnt++;
				if (lineCnt % 100000 == 0) {
					System.out.println(new Date() + " Processed " + lineCnt
							+ " lines.");
				}
				String line = br.readLine();
				// each line is one sparse represented node
				// pld\t{(pld,number),(pld,number)}\tnumber
				if (line != null && line.length() > 1) {
					String[] parts = line.split("\t");
					// origin
					String origin = parts[0];
					long originInd;
					if (!vertices.containsKey(origin)) {
						vertexCnt++;
						vertices.put(origin, vertexCnt);
						originInd = vertexCnt;
						verticesWriter.write(originInd + " \"" + origin
								+ "\"\n");
						crawled.put(vertexCnt, true);
					} else {
						originInd = vertices.get(origin);
						crawled.put(originInd, true);
					}
					// targets
					double pldNum = Integer.parseInt(parts[2]);
					String targets = parts[1];
					targets = targets.replace("{", "").replace("}", "");
					if (targets.length() > 0) {
						String[] plds = targets.split("\\),\\(");
						for (String pld : plds) {
							String[] p = pld.split(",");
							long targetInd;
							if (p.length != 2) {
								continue;
							}
							String targetPld = p[0];
							targetPld = targetPld.replace("(", "").replace(")",
									"");
							if (!vertices.containsKey(targetPld)) {
								vertexCnt++;
								vertices.put(targetPld, vertexCnt);
								targetInd = vertexCnt;
								verticesWriter.write(targetInd + " \""
										+ targetPld + "\"\n");
								crawled.put(vertexCnt, false);
							} else {
								targetInd = vertices.get(targetPld);
							}
							double targetPldCnt = Integer.parseInt(p[1]
									.replace(")", ""));
							if (targetInd < 1) {
								System.out
										.println("Target Id is smaller 1 for "
												+ targetPld
												+ " with vertex cnt "
												+ vertexCnt);
							}
							if (weight) {
								arcWriter.write(originInd + " " + targetInd
										+ " " + targetPldCnt / pldNum + "\n");
							} else {
								arcWriter.write(originInd + " " + targetInd
										+ " 1\n");
							}
						}
					}

				}
			}
			System.out.println(new Date() + " Document processing done.");
			System.out.println(new Date() + " Writing output files.");
			BufferedWriter partWriter = new BufferedWriter(new FileWriter(
					new File(inputFile + "_crawled.clu")));
			partWriter.write("*Vertices " + crawled.size() + "\n");
			for (long i = 1; i <= vertexCnt; i++) {
				partWriter.write(crawled.get(i).compareTo(false) + "\n");
			}
			partWriter.close();
			br.close();
			arcWriter.close();
			verticesWriter.close();
			System.out.println(new Date() + " Done.");
		} catch (FileNotFoundException e) {
			System.out.println("Could not find input file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not access input file.");
			e.printStackTrace();
		}

	}

	/**
	 * Transforms the sparce documents to a Pajek Readable File (.net) and
	 * includes only crawled vertices
	 * 
	 * @param inputFile
	 */
	private static void transformCrawled(String inputFile, boolean weight) {
		System.out.println(new Date()
				+ " Transformation of crawled vertices started ...");
		try {
			System.out.println(new Date() + " Reading crawled vertices ...");
			BufferedReader br = new BufferedReader(new FileReader(new File(
					inputFile)));
			long vertexCnt = 0;
			long lineCnt = 0;
			HashMap<String, Long> vertices = new HashMap<String, Long>();

			long originInd;
			while (br.ready()) {
				lineCnt++;
				if (lineCnt % 1000000 == 0) {
					System.out.println(" ... " + new Date() + " Processed "
							+ lineCnt + " lines.");
				}
				String line = br.readLine();
				// each line is one sparse represented node
				// pld\t{(pld,number),(pld,number)}\tnumber
				if (line != null && line.length() > 1) {
					String[] parts = line.split("\t");
					// origin
					String origin = parts[0];
					if (!vertices.containsKey(origin)) {
						vertexCnt++;
						vertices.put(origin, vertexCnt);
					}
				}
			}

			br.close();
			// now that we have a list of all accepted plds we rerun through the
			// file and create the arcs
			br = new BufferedReader(new FileReader(new File(inputFile)));
			BufferedWriter verticesWriter = new BufferedWriter(new FileWriter(
					new File(inputFile + "_crawled_vertices")));
			verticesWriter.write("*Vertices " + vertices.size() + "\n");
			System.out.println(new Date() + " Calculating arcs ...");
			BufferedWriter arcWriter = new BufferedWriter(new FileWriter(
					new File(inputFile + "_crawled_arcs")));
			arcWriter.write("*Arcs :1 \"Links\"\n");
			lineCnt = 0;
			while (br.ready()) {
				lineCnt++;
				if (lineCnt % 100000 == 0) {
					System.out.print(".");
				}
				if (lineCnt % 1000000 == 0) {
					System.out.println(" Processed " + lineCnt + " lines. "
							+ new Date());
				}
				String line = br.readLine();
				// each line is one sparse represented node
				// pld\t{(pld,number),(pld,number)}\tnumber
				if (line != null && line.length() > 1) {
					String[] parts = line.split("\t");
					// origin
					String origin = parts[0];
					// if it fly out in the sampling
					if (vertices.containsKey(origin)) {
						originInd = vertices.get(origin);
						verticesWriter.write(originInd + " \"" + origin
								+ "\"\n");
						// this should not happen
						if (originInd == 0) {
							continue;
						}

						// targets
						double pldNum = Integer.parseInt(parts[2]);
						String targets = parts[1];
						targets = targets.replace("{", "").replace("}", "");
						if (targets.length() > 0) {
							String[] plds = targets.split("\\),\\(");
							for (String pld : plds) {
								String[] p = pld.split(",");
								long targetInd;
								if (p.length != 2) {
									continue;
								}
								String targetPld = p[0];
								targetPld = targetPld.replace("(", "").replace(
										")", "");
								if (vertices.containsKey(targetPld)) {
									targetInd = vertices.get(targetPld);
									double targetPldCnt = Integer.parseInt(p[1]
											.replace(")", ""));
									if (targetInd < 1) {
										// this should not happen
										System.out
												.println("Target Id is smaller 1 for "
														+ targetPld
														+ " with vertex cnt "
														+ vertexCnt);
									}
									if (weight) {
										arcWriter.write(originInd + " "
												+ targetInd + " "
												+ targetPldCnt / pldNum + "\n");
									} else {
										arcWriter.write(originInd + " "
												+ targetInd + " 1\n");
									}
								}
							}
						}
					}
				}
			}
			System.out.println(new Date() + " Document processing done.");
			br.close();
			verticesWriter.close();
			arcWriter.close();
		} catch (FileNotFoundException e) {
			System.out.println("Could not find input file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not access input file.");
			e.printStackTrace();
		}
	}


	
	

}
