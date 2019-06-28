package org.webdatacommons.structureddata.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVStatWriter {

	private CSVWriter writer;
	private boolean headerWritten;

	public CSVStatWriter(File inputFile) throws FileNotFoundException,
			IOException {
		writer = new CSVWriter(new OutputStreamWriter(new GZIPOutputStream(
				new FileOutputStream(inputFile, false))), ',');
	}

	public synchronized void write(Map<String, String> data) {
		String[] keys = {};
		keys = (String[]) data.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		if (!headerWritten) {
			writer.writeNext(keys);
			headerWritten = true;
		}
		// write the data
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < keys.length; i++) {
			Object value = data.get(keys[i]);
			if (value != null) {
				values.add(value.toString());
			} else {
				values.add("");
			}
		}
		writer.writeNext((String[]) values.toArray(new String[0]));

	}

	public void close() {
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
