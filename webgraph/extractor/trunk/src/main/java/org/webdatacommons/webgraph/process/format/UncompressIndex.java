package org.webdatacommons.webgraph.process.format;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.framework.Processor;
import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

public class UncompressIndex extends Processor<String> {

	public UncompressIndex(String inputFileOrDir, String outputDir, int threads) {
		super(threads);
		this.inputFileOrDir = inputFileOrDir;
		this.outputDir = new File(outputDir);
	}

	private String inputFileOrDir;
	private File outputDir;

	@Override
	protected List<String> fillListToProcess() {
		try {
			return InputUtil.getFileReferenceList(inputFileOrDir);
		} catch (IOException e) {
			return new ArrayList<String>();
		}

	}

	@Override
	protected void process(String file) {
		File inputFile = new File(file);
		try {
			BufferedReader br = InputUtil.getBufferedReader(inputFile);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new GZIPOutputStream(new FileOutputStream(new File(
							outputDir, inputFile.getName())))));
			while (br.ready()) {
				String line = br.readLine();
				String tok[] = line.split("\t");
				bw.write(DomainUtil.uncompress(tok[0]) + "\t" + tok[1] + "\n");
			}
			br.close();
			bw.close();
		} catch (IOException e) {
			System.out.println("Could not handel file " + file);
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (args.length != 3){
			System.out.println("USAGE: org.webdatacommons.webgraph.process.format.UncompressIndex <INDEXFILEORDIR> <OUTPUTDIR> <NUMTHREADS>");
			return;
		}
		UncompressIndex index = new UncompressIndex(args[0], args[1], Integer.parseInt(args[2]));
		index.process();
		System.out.println("Done.");
	}
}
