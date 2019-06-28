package org.webdatacommons.analyze.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

/**
 * This class extracts from the statistic file of the WDC extraction a
 * customized set of sub lists possibly including additional data as the number
 * of extracted statements
 * 
 * @author Robert Meusel
 * 
 */
public class CustomizedURLExtractor {

	public static void main(String[] args) throws IOException {
		System.out.println("Usage: CustomizedURLExtractor <INPUTFILE>");
		// reader
		BufferedReader br = InputUtil.getBufferedReader(new File(args[0]));
		// writer
		BufferedWriter statementCntWriter = new BufferedWriter(
				new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(args[0].replace(".gz", "").concat(
								".statementcnt.gz")))));
		BufferedWriter mdCntWriter = new BufferedWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(args[0].replace(
						".gz", "").concat(".mdcnt.gz")))));
		BufferedWriter rdfaCntWriter = new BufferedWriter(
				new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(args[0].replace(".gz", "").concat(
								".rdfacnt.gz")))));
		int lineErrCnt = 0;
		long lineCnt = 0;
		try {
			while (br.ready()) {
				if (++lineCnt % 100000 == 0) {
					System.out.println(new Date() + " Read " + lineCnt
							+ " lines.");
				}
				String line = br.readLine();
				String[] tok = line.split("\t");
				// sum statements: 19
				// md statements: 13
				// rdfa statements: 14
				// url: 20
				if (tok.length != 21) {
					System.out
							.println("Token length does not match 20. Skipping!");
					lineErrCnt++;
				} else {
					int cnt = 0;
					// statements
					if ((cnt = Integer.parseInt(tok[19])) > 0) {
						statementCntWriter.write(tok[20] + "\t" + cnt + "\n");
					}
					// md
					if ((cnt = Integer.parseInt(tok[13])) > 0) {
						mdCntWriter.write(tok[20] + "\t" + cnt + "\n");
					}
					// rdfa
					if ((cnt = Integer.parseInt(tok[14])) > 0) {
						rdfaCntWriter.write(tok[20] + "\t" + cnt + "\n");
					}
				}
			}
			statementCntWriter.close();
			mdCntWriter.close();
			rdfaCntWriter.close();
			br.close();
			System.out.println("Done. Found " + lineErrCnt
					+ " lines which are to short or long.");
		} catch (Exception e) {
			// if something goes wrong catch it
			e.printStackTrace();
		} finally {
			// at least close streams
			statementCntWriter.close();
			mdCntWriter.close();
			rdfaCntWriter.close();
			br.close();

		}
	}

}
