package org.webdatacommons.analyze.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

/**
 * INPUT: 1) Matrix (CSV) with or without header 2) Amount of Properties 3)
 * Amount of Types (NOTE: Properties must come before types in the csv) 4)
 * Optional: PLD List to be filtered OUTPUT: Broken CSV without header - order
 * of types and properties will be the same.
 * 
 * @author Robert
 * 
 */
public class MatrixProcessor {

	/**
	 * Reads items as string from a file and returns them in a list. If there
	 * are no files, null is returned.
	 * 
	 * @param fileName
	 * @return List of {@link String}s.
	 * @throws IOException
	 *             If file cannot be read, of does not exist.
	 */
	private static List<String> getFilter(String fileName) throws IOException {
		List<String> filter = new ArrayList<String>();
		BufferedReader br = InputUtil.getBufferedReader(new File(fileName));
		while (br.ready()) {
			filter.add(br.readLine());
		}
		br.close();
		if (filter.size() < 1) {
			filter = null;
		}
		return filter;
	}

	public static void main(String[] args) throws IOException {
		List<String> filterList = null;
		if (args.length > 4) {
			filterList = getFilter(args[4]);
		}

		int numberOfTypes = Integer.parseInt(args[2]);
		int numberOfProperties = Integer.parseInt(args[1]);
		int numberOfLines = Integer.parseInt(args[3]);
		boolean header = true;
		BufferedReader br = InputUtil.getBufferedReader(new File(args[0]));

		// init
		int[][] typeMatrix = new int[numberOfTypes][numberOfLines];
		int[][] propertyMatrix = new int[numberOfProperties][numberOfLines];

		int[][] outputMatrix = new int[numberOfTypes][numberOfProperties];

		int lineCnt = 0;
		if (header) {
			// skipping header
			br.readLine();
		}
		// read the file and put it into a matrix
		while (br.ready()) {
			String line = br.readLine();
			String[] tok = line.split(",");
//			System.out.println(lineCnt + " - " + tok.length);
			if (filterList == null || filterList.contains(tok[0])) {
				// we skip the pld as we do not care
				for (int i = 1; i < tok.length; i++) {
					if (i <= numberOfProperties) {
						propertyMatrix[i-1][lineCnt] = Integer
								.parseInt(tok[i]);
					} else if (i <= numberOfTypes + numberOfProperties) {
						typeMatrix[i - numberOfProperties - 1][lineCnt] = Integer
								.parseInt(tok[i]);
					}
				}
			}
			if (++lineCnt % 10000 == 0) System.out.println("Parsed " + lineCnt + " lines.");
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(args[0] + ".aggmatrix.csv")));
		
		// now we aggregate
		for (int type = 0; type < numberOfTypes; type++) {
			for (int prop = 0; prop < numberOfProperties; prop++) {
				int sum = 0;
				for (int line = 0; line < lineCnt; line++) {
					if (typeMatrix[type][line] == 1) {
						sum += propertyMatrix[prop][line];
					}
				}
				//outputMatrix[type][prop] = sum;
				bw.write(sum + ",");
			}
			bw.write("\n");
		}
		
		bw.close();
	}

}
