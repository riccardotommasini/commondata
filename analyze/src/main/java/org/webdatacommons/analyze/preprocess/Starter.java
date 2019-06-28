package org.webdatacommons.analyze.preprocess;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Starter {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		startURLProcessing();
	}

	private static void startURLProcessing() throws FileNotFoundException, IOException {
		UrlProcessor p = new UrlProcessor(0,
				"/home/rmeusel/data/crawl/wdc/pages/rdfa/",
				"/home/rmeusel/data/crawl/wdc/wdcrdfaurldomains/", false);
		p.process();
	}

}
