package org.webdatacommons.webgraph.statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.uni_mannheim.informatik.dws.dwslib.framework.Processor;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

/**
 * Calculates file-based statistics ... this can also be done using HADOOP PIG
 * if the files are too big.
 * 
 * @author Robert
 * 
 */
public class StatHelper extends Processor<File> {

	public StatHelper(String inputDirOrFile, String outputDir, int threads) {
		super(threads);
		this.inputDirOrFile = inputDirOrFile;
		this.outputDir = new File(outputDir);
		if (this.outputDir.isFile()) {
			System.out.println("Outputdir is not a dir.");
			System.exit(0);
		}
	}

	private String inputDirOrFile;
	private File outputDir;

	@Override
	protected List<File> fillListToProcess() {
		return InputUtil.getFileList(inputDirOrFile);
	}

	@Override
	protected void process(File object) {
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		BufferedReader br;
		try {
			br = InputUtil.getBufferedReader(object.getAbsoluteFile());
			while (br.ready()) {
				String line = br.readLine();
				String tok[] = line.split("\t");
				String key = tok[0];
				int cnt = 1;
				if (tok.length > 1) {
					cnt = Integer.parseInt(tok[1]);
				}
				if (counter.containsKey(key)) {
					cnt += counter.get(key);
				}
				counter.put(key, cnt);
			}
			br.close();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					outputDir, "stat_" + object.getName().replace(".gz", ""))));
			for (String key : counter.keySet()) {
				bw.write(key + "\t" + counter.get(key) + "\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		System.out.println("StatHelper <INPUTFILEORDIR> <OUTPUTDIR> <THREADS>");
		StatHelper s = new StatHelper(args[0], args[1],
				Integer.parseInt(args[2]));
		s.process();
	}

}
