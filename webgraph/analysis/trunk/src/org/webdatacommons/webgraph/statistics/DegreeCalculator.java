package org.webdatacommons.webgraph.statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;

public class DegreeCalculator {

	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			System.out.println("No Input file given.");
		}
		String inputFile = args[0];
		String outputFile = args[0] + "-degree.csv";
		if (args.length > 1){
			outputFile = args[1];
		}
		createVertexDegrees(inputFile, outputFile);
//		createVertexDegrees(
//				"/home/rmeusel/data/crawl/cc/pldNetwork/de/pld-network-de",
//				"/home/rmeusel/data/crawl/cc/pldNetwork/de/pld-network-de-degree.csv");
	}

	private static void createVertexDegrees(String inputFile, String outputFile) {
		Date start = new Date();
		System.out.println(new Date()
				+ " Creating of degree file for crawled vertices started ...");
		try {
			System.out.println(new Date() + " Reading crawled vertices ...");
			BufferedReader br = new BufferedReader(new FileReader(new File(
					inputFile)));
			long lineCnt = 0;
			HashMap<String, Degree> vertices = new HashMap<String, Degree>();

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
					long pldCnt = Integer.parseInt(parts[2]);

					if (!vertices.containsKey(origin)) {
						Degree d = new Degree();
						d.addNum(pldCnt);
						vertices.put(origin, d);
					}
				}
			}
			System.out.println(new Date() + " ... index creation done.");
			br.close();

			br = new BufferedReader(new FileReader(new File(inputFile)));
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
					long outDegree = 0;
					// if it fly out in the sampling
					if (vertices.containsKey(origin)) {

						// targets
						String targets = parts[1];
						targets = targets.replace("{", "").replace("}", "");
						if (targets.length() > 0) {
							String[] plds = targets.split("\\),\\(");
							for (String pld : plds) {
								String[] p = pld.split(",");

								if (p.length != 2) {
									continue;
								}
								String targetPld = p[0];
								targetPld = targetPld.replace("(", "").replace(
										")", "");
								if (vertices.containsKey(targetPld)) {
									long targetPldCnt = Integer.parseInt(p[1]
											.replace(")", ""));
									outDegree += targetPldCnt;
									vertices.get(targetPld).addIn(targetPldCnt);
									vertices.get(targetPld).addInUnw(1l);
								}
							}
							vertices.get(origin).addOutUnw(plds.length);
							vertices.get(origin).addOut(outDegree);
							
						}
					}
				}
			}
			System.out.println(" Processed " + lineCnt + " lines. "
					+ new Date());
			System.out.println(new Date() + " Document processing done.");
			br.close();
			System.out.println("Write output file ...");
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					outputFile)));
			bw.write("TLD\tPLD\t" + Degree.getCSVHeader() + "\n");
			for (String key : vertices.keySet()) {
				bw.write(DomainUtil.getTopLevelDomain(key) + "\t" + key + "\t" + vertices.get(key).toCSVString() + "\n");
			}
			bw.close();
			System.out.println(new Date() + " writing done.");
			Date end = new Date();
			long span = end.getTime() - start.getTime();
			System.out.println("Done after " + span/1000 + " seconds.");

		} catch (FileNotFoundException e) {
			System.out.println("Could not find input file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not access input file.");
			e.printStackTrace();
		}
	}

}
