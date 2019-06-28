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

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class SemanticPageDeploymentCalculator {

	/**
	 * Compares the values of items in a two lists (input and sub) where the
	 * sub-list includes a subset of the items of the input list. Both lists
	 * have to have the item in the first place and the value in the second
	 * place separated by tab. Both list have to be ordered. If multiple files
	 * are used, the order of files must match the order of items in the
	 * separate files. Creates a list of all pages included in both lists and
	 * the ratio of their values.
	 * 
	 * @param inputFileOrDir
	 *            the file or dir of the list of items.
	 * @param subFileOrDir
	 *            the file or dir of the sub list of items
	 * @throws IOException
	 *             throws an exception when on of the files could not be read.
	 */
	public static void compare(String inputFileOrDir, String subFileOrDir,
			String outputFile) throws IOException {
		List<File> inputList = InputUtil.getFileList(inputFileOrDir);
		Collections.sort(inputList);
		BufferedReader inputlistReader = InputUtil.getBufferedReader(inputList);
		List<File> subList = InputUtil.getFileList(subFileOrDir);
		Collections.sort(subList);
		BufferedReader sublistReader = InputUtil.getBufferedReader(subList);

		// initialize with first line
		String inputTok[];
		String sublistTok[];
		if (inputlistReader.ready() && sublistReader.ready()) {
			inputTok = inputlistReader.readLine().split("\t");
			sublistTok = sublistReader.readLine().split("\t");
		} else {
			System.out
					.println("List are not ready ... maybe one of them is empty.");
			return;
		}
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(new File(outputFile)))));
		bw.write("ITEM\tValueInSubList\tValueInList\tRatio\n");
		int cnt = 1;
		int failcnt = 0;
		while (inputlistReader.ready() && sublistReader.ready()) {
			if (sublistTok[0].equals(inputTok[0])) {

				double ratio = Double.parseDouble(sublistTok[1])
						/ Double.parseDouble(inputTok[1]);
				bw.write(sublistTok[0] + "\t" + sublistTok[1] + "\t"
						+ inputTok[1] + "\t" + ratio + "\n");
				sublistTok = sublistReader.readLine().split("\t");
				inputTok = inputlistReader.readLine().split("\t");
				cnt++;
			} else if (sublistTok[0].compareTo(inputTok[0]) > 0) {
				inputTok = inputlistReader.readLine().split("\t");
				cnt++;

			} else {
				// should not happen
				failcnt++;
				sublistTok = sublistReader.readLine().split("\t");

			}
			if (cnt % 1000000 == 0) {
				System.out.println("Parsed first " + cnt
						+ " lines of input list.");
			}
		}
		System.out.println("Done with " + failcnt + " errors.");
		bw.close();
		inputlistReader.close();
		sublistReader.close();
	}

	public static void main(String[] args) throws IOException {
		compare(args[0], args[1], args[2]);
	}
}
