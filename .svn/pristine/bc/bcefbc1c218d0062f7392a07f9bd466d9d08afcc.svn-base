package org.webdatacommons.analyze.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.framework.Processor;
import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;

/**
 * This class will read an list of URLs and calculate the domain (SD), the
 * first-subdomain (SD1) and the pay-level-domain (PLD) for each input URL.
 * Especially it will not remove duplicates but create for each input URL on
 * output line with the format <PLD> <SD1> <SD> using <TAB> as default
 * separator. If one of the aggregation levels could not be calculated there
 * will be a NULL in the corresponding line. Input URL list has to have each
 * line one URL. If input URLs are compressed (Using
 * {@link DomainUtil#compress(String)}) set decompress option.
 * 
 * @author Robert Meusel
 * 
 */
public class UrlProcessor extends Processor<File> {

	private String inputDir;
	private File outputDir;
	private boolean decompress;

	public UrlProcessor(int threads, String inputDir, String outputDir,
			boolean decompress) {
		super(threads);
		this.inputDir = inputDir;
		this.outputDir = new File(outputDir);
		if (!this.outputDir.exists() || this.outputDir.isFile()) {
			System.out.println("Output directory is not a directory.");
			System.exit(0);
		}
		this.decompress = decompress;
	}

	@Override
	protected List<File> fillListToProcess() {
		return InputUtil.getFileList(inputDir);
	}

	@Override
	public void process(File object) {
		try {
			BufferedReader br = InputUtil.getBufferedReader(object);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new GZIPOutputStream(new FileOutputStream(new File(
							outputDir, object.getName())))));
			StringBuilder sb;
			while (br.ready()) {
				String url = br.readLine();
				if (decompress) {
					url = DomainUtil.uncompress(url);
				}
				String pld = DomainUtil.getPayLevelDomainFromWholeURL(url);
				String sd1 = DomainUtil.getFirstSubDomainFromWholeUrl(url);
				String sd = DomainUtil.getSubDomainFromWholeUrl(url);
				sb = new StringBuilder();
				sb.append(pld);
				sb.append("\t");
				sb.append(sd1);
				sb.append("\t");
				sb.append(sd);
				sb.append("\n");
				bw.write(sb.toString());
				sb.setLength(0);
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
