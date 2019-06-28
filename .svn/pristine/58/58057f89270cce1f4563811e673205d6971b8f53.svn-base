package org.webdatacommons.webgraph.extraction.sf.model;

public class SequenceFileStatistics {

	String inputFile;
	String outputFile;
	int processedKeys;
	int foundRedirects;
	int foundCrossDomainRedirects;
	int found404;

	public static String HEADER = "INPUTFILE\tOUTPUTFILE\tPROCESSEDKEYS\tFOUNDREDIRECTS\tCROSSDOMAINREDIR\t404";

	public SequenceFileStatistics(String inputFile, String outputFile,
			int processedKeys, int foundRedirects,
			int foundCrossDomainRedirects, int found404) {
		super();
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.processedKeys = processedKeys;
		this.foundRedirects = foundRedirects;
		this.foundCrossDomainRedirects = foundCrossDomainRedirects;
		this.found404 = found404;
	}

	@Override
	public String toString() {
		return inputFile + "\t" + outputFile + "\t" + processedKeys + "\t"
				+ foundRedirects + "\t" + foundCrossDomainRedirects + "\t"
				+ found404;
	}

}
