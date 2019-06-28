package org.webdatacommons.structureddata.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import de.dwslab.dwslib.util.io.InputUtil;

public class ScrappyWarcReader {

	private BufferedReader br;
	private static String WARC_BEGIN = "WARC/1.0";
	private static String HEADER_URL = "WARC-Target-URI";
	private boolean next;

	private int BUFFER_SIZE = 1024;

	public ScrappyWarcReader(File f) throws Exception {
		br = InputUtil.getBufferedReader(f);

		// FileInputStream fileInputStream = new FileInputStream(f);
		//
		//
		// BufferedInputStream bufferedInputStream = new BufferedInputStream(
		// fileInputStream);
		// GzipCompressorInputStream gzipInputStream = new
		// GzipCompressorInputStream(
		// bufferedInputStream);

		//
		// br = new BufferedReader(new InputStreamReader(gzipInputStream,
		// "UTF-8"));
		// if (br.ready()){
		// System.out.println(br.readLine());
		// }else{
		// System.out.println("BR not ready");
		// }

		// int byteReadCount = 0;
		// final byte[] data = new byte[BUFFER_SIZE];
		// try {
		// while ((byteReadCount = gzipInputStream.read(data, 0, BUFFER_SIZE))
		// != -1) {
		// System.out.println(new String(data));
		// }
		// } finally {
		//
		// gzipInputStream.close();
		// }

		// br = new BufferedReader(new InputStreamReader(
		// new GzipCompressorInputStream(new FileInputStream(f))));

		if (br.ready()) {
			String line = br.readLine();
			if (line.startsWith(WARC_BEGIN)) {
				next = true;
			} else {
				throw new Exception("Not a valid WARC file.");
			}
		} else {
			throw new Exception("Empty file");
		}

		while (br.ready()) {
			System.out.println(br.readLine());
		}

	}

	public boolean hasNext() {
		return next;
	}

	public void close() throws IOException {
		br.close();
	}

	public ScrappyWarcRecord getNext() throws Exception {
		if (next) {
			next = false;
			boolean contentReached = false;
			String content = "";
			String url = "";
			while (br.ready()) {
				String line = br.readLine();
				if (line.startsWith(WARC_BEGIN)) {
					next = true;
					break;
				}
				if (!contentReached) {
					if (line.startsWith(HEADER_URL)) {
						url = line.replace(HEADER_URL + ": ", "");
					} else if (line.trim().length() < 1) {
						contentReached = true;
					}
				} else {
					content += line + "\n";
				}
			}
			return new ScrappyWarcRecord(url, content.getBytes());
		} else {
			throw new Exception("No new record.");
		}
	}
}
