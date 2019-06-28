package org.webdatacommons.analyze.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class Sample {
	
	public static void main(String[] args) throws IOException {
		//TODO make a nice attribute selection
		sampleByRatio(args[0], args[1], Float.parseFloat(args[2]));
	}
	
	private static void sampleLinear(String inputFileOrDir, String outputFile, int takeEach) throws IOException{
		if (takeEach < 2) {
			System.out.println("Take-each is too small (>1).");
			return;
		}
		List<File> files = InputUtil.getFileList(inputFileOrDir);
		BufferedReader br = InputUtil.getBufferedReader(files);
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(new File(outputFile)))));
		long lineCnt = 0;
		long rdCnt = 0;
		int tmpCnt = 0;
		System.out.println("Start sampling ...");
		while (br.ready()) {
			rdCnt++;
			tmpCnt++;
			String line = br.readLine();
			if (tmpCnt == takeEach) {
				bw.write(line+ "\n");
				lineCnt++;
				tmpCnt = 0;
			}
		}
		bw.close();
		br.close();
		System.out.println("... sampling done. Written " + lineCnt + " lines from "
				+ rdCnt + " overall lines.");
	}

	private static void sampleByRatio(String inputFileOrDir, String outputFile,
			float ratio) throws IOException {
		if (ratio > 1 || ratio < 0) {
			System.out.println("Ratio is not between 0 and 1.");
			return;
		}
		System.out.println("Reading file list.");
		List<File> files = InputUtil.getFileList(inputFileOrDir);
		System.out.println("Getting Buffered Reader for files.");
		BufferedReader br = InputUtil.getBufferedReader(files);
		System.out.println("Start sampling.");
		Random rnd = new Random();
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(new File(outputFile)))));
		long lineCnt = 0;
		long rdCnt = 0;
		while (br.ready()) {
			rdCnt++;
			String line = br.readLine();
			if (rnd.nextFloat() < ratio) {
				bw.write(line+ "\n");
				lineCnt++;
			}
		}
		bw.close();
		br.close();
		System.out.println("Sampling done. Written " + lineCnt + " lines from "
				+ rdCnt + " overall lines.");
	}

}
