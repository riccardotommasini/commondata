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

public class NormalizeStatistics extends Processor<File> {

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		System.out.println("USAGE: NormalizeStatistics Folder NumProcessors");
		NormalizeStatistics stats = new NormalizeStatistics(
				Integer.parseInt(args[1]), args[0]);
		stats.process();
	}

	public NormalizeStatistics(int arg0, String folder) {
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
		try {
			BufferedReader br = InputUtil.getBufferedReader(arg0);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new GZIPOutputStream(new FileOutputStream(new File(arg0
							.getParentFile(), "cleaned_" + arg0.getName())))));
			while (br.ready()) {
				String line = br.readLine();
				String tok[] = line.split("\t");
				int rdfa = 0;
				int sum = 0;
				if (tok.length < 11) {
					System.out.println("Line within file "
							+ arg0.getAbsolutePath() + " could not be parsed: "
							+ line);
					continue;
				}
				// first is the uri
				for (int i = 1; i < 11; i++) {
					sum += Integer.parseInt(tok[i]);
				}
				if (tok.length > 11){
					rdfa = Integer.parseInt(tok[12]);
					sum += rdfa;
				}
				if (sum > 0) {
					bw.write(tok[0]);
					for (int i = 1; i < 11; i++) {
						bw.write("\t" + tok[i]);
					}
					bw.write("\t" + rdfa);
					bw.write("\t" + sum + "\n");
				}
			}
			br.close();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Could not parse file: "
					+ arg0.getAbsolutePath());
		}

	}

}
