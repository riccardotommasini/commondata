package org.webdatacommons.analyze.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class Helper {

	public static void main(String[] args) throws IOException {
		extractAndChunk(args[0], args[1], 10000000);
	}

	public static void extractAndChunk(String inputfile, String outputDirName,
			int lineChunks) throws IOException {
		int fileCnt = 0;

		File outputDir = new File(outputDirName);
		if (!outputDir.exists() || outputDir.isFile()) {
			System.out
					.println("Output directory does not exist or is not a directory.");
			return;
		}
		BufferedReader br = InputUtil.getBufferedReader(new File(inputfile));
		int linesWritten = lineChunks;
		BufferedWriter bw = null;
		System.out.println("Start parsing file ...");
		while (br.ready()) {
			if (linesWritten >= lineChunks) {

				if (bw != null) {
					bw.close();
					System.out.println("Written "
							+ ((long) fileCnt * lineChunks) + " lines.");
				}
				bw = new BufferedWriter(new OutputStreamWriter(
						new GZIPOutputStream(new FileOutputStream(new File(
								outputDir, "part-"
										+ String.format("%05d", fileCnt++)
										+ ".gz")))));
				linesWritten = 0;
			}
			String line = br.readLine();
			String lineToWrite = null;
//			lineToWrite = getUriFromPageLine(line);
//			lineToWrite = getRdfaUriFromPageLine(line);
//			lineToWrite = getMicrodataUriFromPageLine(line);
//			lineToWrite = getUriAndTripleCntFromPageLine(line);
			lineToWrite = getCompressedUriFromPageLine(line);
			if (lineToWrite != null) {
				bw.write(lineToWrite);
				linesWritten++;
			}
		}
		bw.close();
		br.close();
		System.out.println("... done.");
	}
	
	private static String getUriFromPageLine(String line){
		return line.substring(line.lastIndexOf("\t") + 1)
				+ "\n";
	}
	
	private static String getCompressedUriFromPageLine(String line){
		return DomainUtil.compress(line.substring(line.lastIndexOf("\t") + 1))
				+ "\n";
	}
	
	private static String getUriAndTripleCntFromPageLine(String line){
		String tok[] = line.split("\t");
		int mfTrip = 0;
		for (int i = 4; i < 13; i++){
			mfTrip += Integer.parseInt(tok[i]);
		}
		return tok[20] + "\t" + tok[13] + "\t" + tok[14] + "\t" + mfTrip + "\n"; 
	}
	
	private static String getRdfaUriFromPageLine(String line){
		String tok[] = line.split("\t");
		if (Integer.parseInt(tok[14])>0){
			return getUriFromPageLine(line);
		}
		return null;
	}
	
	private static String getMicrodataUriFromPageLine(String line){
		String tok[] = line.split("\t");
		if (Integer.parseInt(tok[13])>0){
			return getUriFromPageLine(line);
		}
		return null;
	}
	
}
