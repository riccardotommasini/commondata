package org.fuberlin.wbsg.ccrdf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.fuberlin.wbsg.ccrdf.Statistics;
import org.fuberlin.wbsg.ccrdf.tablegen.TripleStats;
import org.junit.Test;

import com.amazonaws.util.json.JSONException;

public class TripleStatTest {
	@Test
	public void tripleStatTest() throws JSONException, IOException {
		TripleStats
				.generateStats(
						new InputStreamReader(
								new GZIPInputStream(
										new FileInputStream(
												new File(
														"/home/hannes/Desktop/ccdata/dataf/ccrdf.html-mf-xfn.7.nq.gz")))),
						"foo");
	}

	@Test
	public void domainParserTest() {
		System.out.println(Statistics
				.getDomain("http://gesundheit-und-praevention.blogspot.com/\n\nhttp://hartz4forall.blogspot.com/"));
	}
}
