package org.fuberlin.wbsg.ccrdf.pigsupport;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.fuberlin.wbsg.ccrdf.tablegen.SparseMatrix.LongAggregationEntry;
import org.fuberlin.wbsg.ccrdf.tablegen.SparseMatrix.SparseStringCooccurenceMatrix;

public class CoocGroupAggregator {
	private static Logger log = Logger.getLogger(CoocGroupAggregator.class);

	public static void main(String[] args) throws IOException {
		CoocGroupAggregator c = new CoocGroupAggregator();
		c.readFile(new FileInputStream(
				"/home/hannes/Desktop/wdc-results/2012-rdfa/classCoocPlds.csv"));
		c.printGroups(System.out);
	}

	private SparseStringCooccurenceMatrix m = new SparseStringCooccurenceMatrix();
	private long transactions = 0;
	public static final String SEPARATOR = "\t";
	public final static int INDEX_A = 0;
	public final static int INDEX_B = 1;
	public final static int INDEX_S = 2;

	public void readFile(InputStream fstream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			try {
				String[] fields = strLine.split(SEPARATOR);
				String a = fields[INDEX_A];
				String b = fields[INDEX_B];
				long s = Long.parseLong(fields[INDEX_S]);
				add(a, b, s);
				transactions += s;
			} catch (Exception e) {
				log.debug(e);
				log.debug(strLine);
			}
		}
		br.close();
	}

	public void add(String a, String b, long s) {
		m.put(a, b, s);
	}

	public void printGroups(OutputStream out) throws IOException {
		StringWriter writer = new StringWriter();
		List<Entry<Set<String>, LongAggregationEntry>> res = m.getCoocSorted(
				transactions, 0.001, 3);
		for (Entry<Set<String>, LongAggregationEntry> e : res) {
			writer.write(e.getKey().toString());
			writer.write(e.getValue().toString());
			writer.write("\n");
		}
		out.write(writer.toString().getBytes());
	}
}
