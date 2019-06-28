package org.webdatacommons.webgraph.util;

import it.unimi.dsi.fastutil.longs.LongBigArrays;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class IndexUtils {

	public static List<Long> readIdsFromFile(String file, int pos)
			throws IOException {
		System.out.println("Reading ids ...");
		BufferedReader br = InputUtil.getBufferedReader(new File(file));
		List<Long> ids = new ArrayList<Long>();
		while (br.ready()) {
			String line = br.readLine();
			String tok[] = line.split("\t");
			ids.add(Long.parseLong(tok[pos]));
		}
		br.close();
		System.out.println("Read " + ids.size() + " ids from inputfile.");
		return ids;
	}

	public static void index2URL(String indexFileOrDir, String file, int pos,
			String outputFile, long max) throws IOException {
		System.out.println("Detecting URLs from Index Files ....");
		List<File> files = InputUtil.getFileList(indexFileOrDir);
		System.out.println("Found " + files.size() + " index files.");
		// make output writer ready
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(new File(outputFile)))));
		// everything is 0
		System.out.println("Reading ids ...");
		BufferedReader br = InputUtil.getBufferedReader(new File(file));
		long pageIdArray[][] = LongBigArrays.newBigArray(max + 1);
		long lineCnt = 0;
		long numOfIds = 0;
		while (br.ready()) {
			if (++lineCnt % 1000000 == 0) {
				System.out.println("Read " + lineCnt + " ids from file.");
			}
			String line = br.readLine();
			String tok[] = line.split("\t");
			LongBigArrays.set(pageIdArray, Long.parseLong(tok[pos]), 1);
			numOfIds++;
		}
		br.close();
		lineCnt = 0;
		for (File f : files) {
			br = InputUtil.getBufferedReader(f);
			
			while (br.ready() && numOfIds > 0) {
				if (++lineCnt % 10000000 == 0) {
					System.out.println(new Date() + " Parsed " + lineCnt
							+ " lines form index files.");
				}
				String line = br.readLine();
				String[] tok = line.split("\t");
				if (tok.length != 2) {
					continue;
				}
				try {
					long id = 0;
					if ((LongBigArrays.get(pageIdArray,
							id = Long.parseLong(tok[1]))) == 1) {
						// as the index might be compressed we uncompress it
						// first
						bw.write(id + "\t" + DomainUtil.uncompress(tok[0])
								+ "\n");
						numOfIds--;
					}
				} catch (ArrayIndexOutOfBoundsException ex) {
					// do nothing
				}
			}
			br.close();
		}

		bw.close();
		System.out.println("Done.");
	}

	public static void index2URLSmall(String indexFileOrDir,
			List<Long> indexIds, String outputFile) throws IOException {
		System.out.println("Detecting URLs from Index Files ....");
		List<File> files = InputUtil.getFileList(indexFileOrDir);
		System.out.println("Found " + files.size() + " index files.");
		// make output writer ready
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(new File(outputFile)))));

		Set<Long> ids = new TreeSet<Long>();

		for (Long i : indexIds) {
			ids.add(i);
		}
		long lineCnt = 0;
		for (File f : files) {
			BufferedReader br = InputUtil.getBufferedReader(f);

			while (br.ready()) {
				System.out.println("Reading file " + f);
				if (++lineCnt % 10000000 == 0) {
					System.out.println(new Date() + " Parsed " + lineCnt
							+ " lines from index files.");
				}
				String line = br.readLine();
				String[] tok = line.split("\t");
				if (tok.length != 2) {
					continue;
				}
				try {
					long id = 0;
					if (ids.contains(id = Long.parseLong(tok[1]))) {
						// as the index might be compressed we uncompress it
						// first
						bw.write(id + "\t" + DomainUtil.uncompress(tok[0])
								+ "\n");

					}
				} catch (ArrayIndexOutOfBoundsException ex) {
					// do nothing
				}
			}
			br.close();
		}

		bw.close();
		System.out.println("Done.");
	}

	public static void main(String[] args) throws NumberFormatException,
			IOException {
		if (args == null || args.length < 5) {
			System.out
					.println("USAGE: org.webdatacommons.webgraph.util.IndexUtils <IDFILE> <IDCOLUMN> <INDEXFILEORDIR> <OUTPUTFILE> <(0 = SMALL | NUMBER OF MAX ID)>");
		} else {

			if (Long.parseLong(args[4]) == 0l) {
				List<Long> ids = readIdsFromFile(args[0],
						Integer.parseInt(args[1]));
				index2URLSmall(args[2], ids, args[3]);
			} else {
				index2URL(args[2], args[0], Integer.parseInt(args[1]), args[3],
						Long.parseLong(args[4]));
			}

		}
	}

}
