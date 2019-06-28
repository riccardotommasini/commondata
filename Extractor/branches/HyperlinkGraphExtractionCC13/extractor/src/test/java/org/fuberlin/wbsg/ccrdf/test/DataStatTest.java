package org.fuberlin.wbsg.ccrdf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.fuberlin.wbsg.ccrdf.Statistics;
import org.junit.Test;

public class DataStatTest {
	@Test
	public void dataStatTest() throws FileNotFoundException, IOException {
		System.out.println(Statistics.readDataStat(new InputStreamReader(
				new GZIPInputStream(new FileInputStream(new File(
						"/home/hannes/Desktop/ccdata2/stats/data.csv.gz"))))));
	}
	
	
}
