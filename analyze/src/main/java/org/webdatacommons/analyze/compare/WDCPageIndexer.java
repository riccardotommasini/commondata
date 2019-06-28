package org.webdatacommons.analyze.compare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.util.BufferedChunkingWriter;
import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class WDCPageIndexer {

	/**
	 * Compares the values of items in a two lists (input and sub) where the
	 * sub-list includes a subset of the items of the input list. And the input
	 * lists has indexes. Both lists have to have the item in the first place and
	 * the value in the second place separated by tab. Both list have to be
	 * ordered. If multiple files are used, the order of files must match the
	 * order of items in the separate files. Output is a list of indexes of the
	 * sublist in the index list and a negative list of the index list.
	 * 
	 * @param inputFileOrDir
	 *            the file or dir of the list of items.
	 * @param subFileOrDir
	 *            the file or dir of the sub list of items
	 * @param uncompress
	 *            set if the negative list has to be uncompressed before writing
	 * @throws IOException
	 *             throws an exception when on of the files could not be read.
	 */
	public static void compare(String inputFileOrDir, String subFileOrDir,
			String outputDirName, boolean uncompress) throws IOException {
		File outputDir = new File(outputDirName);
		if (outputDir.isFile()) {
			System.out.println("Output has to be a dir.");
			return;
		}
		List<File> inputList = InputUtil.getFileList(inputFileOrDir);
		Collections.sort(inputList);
		BufferedReader inputlistReader = InputUtil.getBufferedReader(inputList);
		List<File> subList = InputUtil.getFileList(subFileOrDir);
		Collections.sort(subList);
		BufferedReader sublistReader = InputUtil.getBufferedReader(subList);

		// initialize with first line
		String inputTok[];
		String sublistTok;
		if (inputlistReader.ready() && sublistReader.ready()) {
			inputTok = inputlistReader.readLine().split("\t");
			sublistTok = sublistReader.readLine().trim();
		} else {
			System.out
					.println("List are not ready ... maybe one of them is empty.");
			return;
		}

		BufferedWriter bwIndex = new BufferedWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(new File(outputDir,
						"index.gz")))));
		BufferedChunkingWriter bwNeg = new BufferedChunkingWriter(outputDir,
				"neglist", 100);
		int cnt = 1;
		int failcnt = 0;
		while (inputlistReader.ready() && sublistReader.ready()) {
			if (sublistTok.equals(inputTok[0])) {
				bwIndex.write(inputTok[1] + "\n");
				sublistTok = sublistReader.readLine().trim();
				inputTok = inputlistReader.readLine().split("\t");
				cnt++;
			} else if (sublistTok.compareTo(inputTok[0]) > 0) {
				if (uncompress) {
					bwNeg.write(DomainUtil.uncompress(inputTok[0]) + "\n");
				} else {
					bwNeg.write(inputTok[0] + "\n");
				}
				inputTok = inputlistReader.readLine().split("\t");
			} else {
				// should not happen
				failcnt++;
				sublistTok = sublistReader.readLine().trim();
			}
			if (cnt % 1000000 == 0) {
				System.out.println("Parsed first " + cnt
						+ " lines of input list.");
			}
		}
		System.out.println("Done with " + failcnt + " errors.");
		bwIndex.close();
		bwNeg.close();
		inputlistReader.close();
		sublistReader.close();
	}

	public static void main(String[] args) throws IOException {
		compare(args[0], args[1], args[2], Boolean.parseBoolean(args[3]));
	}
}
