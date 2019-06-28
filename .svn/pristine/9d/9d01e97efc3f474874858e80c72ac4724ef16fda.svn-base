package org.webdatacommons.analyze.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.framework.Processor;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class StatsExtractor extends Processor<File> {

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		System.out.println("USAGE: StatsExtractor Folder NumProcessors");
		StatsExtractor stats = new StatsExtractor(
				Integer.parseInt(args[1]), args[0]);
		stats.process();
	}

	public StatsExtractor(int arg0, String folder) {
		super(arg0);
		inputFolder = folder;
	}

	private String inputFolder;

	@Override
	protected List<File> fillListToProcess() {
		return InputUtil.getFileList(inputFolder);
	}

	@Override
	protected void process(File arg0) {
		int linesWithError = 0;

		try {
			BufferedReader br = InputUtil.getBufferedReader(arg0);
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new GZIPOutputStream(
							new FileOutputStream(new File(arg0.getParentFile(),
									"prepro_" + arg0.getName())))));
			// skip first line
			if (br.ready()) {
				br.readLine();
			}
			// go through the rest of the lines.
			StringBuilder sb = new StringBuilder();
			while (br.ready()) {
				String line = br.readLine();
				String tok[] = line.split("\",\"");
				if (tok.length != 23) {
					linesWithError++;
					System.out.println("Unparsable page: " + line);
				} else {
					sb.append(tok[22].replace("\"", ""));
					for (int i = 5; i < 15; i++) {
						sb.append("\t");
						sb.append(tok[i]);
					}
					sb.append("\n");
					bw.write(sb.toString());
					sb.setLength(0);
				}
			}
			bw.close();
			br.close();
			System.out.println("Found " + linesWithError + " in file: "
					+ arg0.getName());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Could not parse file: "
					+ arg0.getAbsolutePath());
		}

	}

}
