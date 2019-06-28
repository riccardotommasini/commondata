package org.webdatacommons.structureddata.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;


@Parameters(commandDescription = "Cleans Subsets from non Schema.org data")
public class WDCSubsetStatsCleaner extends Processor<File> {

	@Parameter(names = { "-out",
			"-outputDir" }, required = true, description = "Folder where the outputfile(s) are written to.", converter = FileConverter.class)
	private File outputDirectory;

	@Parameter(names = { "-in",
			"-inputDir" }, required = true, description = "Folder where the input is read from.", converter = FileConverter.class)
	private File inputDirectory;

	@Parameter(names = "-threads", required = true, description = "Number of threads.")
	private Integer threads;



	@Override
	protected List<File> fillListToProcess() {
		List<File> files = new ArrayList<File>();
		for (File f : inputDirectory.listFiles()) {		
				files.add(f);
		}
		
		return files;
	}


	@Override
	protected int getNumberOfThreads() {
		return this.threads;
	}	

	@Override
	protected void process(File object) throws Exception {
		BufferedWriter replaceItem = OutputUtil.getGZIPBufferedWriter(new File(outputDirectory+"/"+object.getName()));
		
		// read the file
		BufferedReader br = InputUtil.getBufferedReader(object);
		String subjectToNeglect="thisIsTheSubjectNodeNotToBeCopied";
		while (br.ready()) {
			String line = br.readLine();
			if (line.contains("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")){
				String lineParts[] = line.split(" ");
				String qobject = lineParts[2];
				if (qobject.contains("<http://schema.org/")){
					replaceItem.write(line);
					replaceItem.newLine();
				}
				else subjectToNeglect = lineParts[0];
			}
			else if(line.contains(subjectToNeglect)) continue;
			else {
				replaceItem.write(line);
				replaceItem.newLine();
			}
			
		}
				
		br.close();
		replaceItem.close();

	}

	

	@Override
	protected void afterProcess() {
		// write the collected statistics to file
		try {
			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		WDCQuadStatsCalculator cal = new WDCQuadStatsCalculator();
		try {
			new JCommander(cal, args);
			cal.process();
		} catch (ParameterException pe) {
			pe.printStackTrace();
			new JCommander(cal).usage();
		}
	}

}
