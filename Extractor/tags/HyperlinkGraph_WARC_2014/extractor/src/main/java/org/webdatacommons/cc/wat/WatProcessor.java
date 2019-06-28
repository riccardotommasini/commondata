package org.webdatacommons.cc.wat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.fuberlin.wbsg.ccrdf.StatHandler;
import org.webdatacommons.cc.wat.json.WatJsonReader;
import org.webdatacommons.cc.wat.json.model.JsonData;
import org.webdatacommons.cc.wat.json.model.Link;

public class WatProcessor {

	private static Logger log = Logger.getLogger(WatProcessor.class);

	public Map<String, String> processWatFile(
			ReadableByteChannel gzippedWatFileBC, String watFileName,
			BufferedWriter bw, StatHandler threeHundredHandler)
			throws IOException {
		// number of responses in the file
		long responsesTotal = 0;
		// number of 200 in the file
		long twohundredTotal = 0;
		// number of 3xx in the file
		long threehundredTotal = 0;
		// text/html in the file
		long textTotal = 0;
		// number of links in the file
		long linksTotal = 0;
		// href links in the file
		long hrefTotal = 0;
		// current time of the system when starting process.
		long start = System.currentTimeMillis();
		// errors
		long errorTotal = 0;

		final WARCReader reader = (WARCReader) WARCReaderFactory.get(
				watFileName, Channels.newInputStream(gzippedWatFileBC), true);
		// iterate over each record in the stream
		Iterator<ArchiveRecord> readerIt = reader.iterator();
		while (readerIt.hasNext()) {

			WARCRecord record = (WARCRecord) readerIt.next();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new BufferedInputStream(record)));
			try {
				while (br.ready()) {

					// in most cases we will only get the JSON here. The header
					// is
					// separated before hand
					String line = br.readLine();
					if (line.startsWith("{")) {

						JsonData jd = WatJsonReader.read(line);
						// check if its an response
						if (!jd.envelope.warcHeaderMetadata.warcType
								.equals("response")) {
							continue;
						}

						if (responsesTotal % 1000 == 0) {
							log.info(responsesTotal + " / " + errorTotal
									+ " / " + twohundredTotal + " / "
									+ threehundredTotal + " / " + textTotal
									+ " / " + linksTotal + " / " + hrefTotal);
						}
						responsesTotal++;
						// check for 200 status - most cases have this as
						// redirects
						// are not really common.
						if (jd.envelope.payLoadMetadata.httpResponseMetadata.responseMessage.status != 200) {
							// check for 3xx just for stats
							if (jd.envelope.payLoadMetadata.httpResponseMetadata.responseMessage.status >= 300
									&& jd.envelope.payLoadMetadata.httpResponseMetadata.responseMessage.status < 400) {
								threehundredTotal++;
								ArchiveRecordHeader header = record.getHeader();
								Map<String, String> threeHundredFile = new HashMap<String, String>();
								threeHundredFile.put("1uri", header.getUrl());
								threeHundredFile.put("2json", line);
								// write statistics
								threeHundredHandler.addStats(header.getUrl(),
										threeHundredFile);
							}
							continue;
						}
						twohundredTotal++;
						// we only want text/html pages
						if (jd.envelope.payLoadMetadata.httpResponseMetadata.headers.contentType == null
								|| !jd.envelope.payLoadMetadata.httpResponseMetadata.headers.contentType
										.startsWith("text/html")) {
							continue;
						}
						textTotal++;
						// now we go through all links, if there are any
						if (jd.envelope.payLoadMetadata.httpResponseMetadata.htmlMetadata.links != null
								&& jd.envelope.payLoadMetadata.httpResponseMetadata.htmlMetadata.links.length > 0) {
							ArchiveRecordHeader header = record.getHeader();
							StringBuilder sb = new StringBuilder();
							sb.append(escape(header.getUrl()));
							for (Link link : jd.envelope.payLoadMetadata.httpResponseMetadata.htmlMetadata.links) {
								if (link != null && link.path != null) {
									// only hrefs
									if (link.path.indexOf("href") > -1) {
										sb.append("\t");
										sb.append(escape(link.url));
										hrefTotal++;
									}
									linksTotal++;
								}
							}
							sb.append("\n");
							// write to stream
							bw.write(sb.toString());
						}
						// stop if we found the first
						break;
					}

				}
			} catch (Exception ex) {
				log.error(ex + " in " + watFileName + " for record "
						+ record.getHeader().getUrl(), ex.fillInStackTrace());
				ex.printStackTrace();
				errorTotal++;
			} finally {
				br.close();
			}

		}

		// runtime and rate calculation
		double duration = (System.currentTimeMillis() - start) / 1000.0;
		double rate = (responsesTotal * 1.0) / duration;

		// create data file statistics and return
		Map<String, String> dataStats = new HashMap<String, String>();
		dataStats.put("duration", Double.toString(duration));
		dataStats.put("rate", Double.toString(rate));
		dataStats.put("responsesTotal", Long.toString(responsesTotal));
		dataStats.put("twohundredTotal", Long.toString(twohundredTotal));
		dataStats.put("threehundredTotal", Long.toString(threehundredTotal));
		dataStats.put("textTotal", Long.toString(textTotal));
		dataStats.put("linksTotal", Long.toString(linksTotal));
		dataStats.put("hrefTotal", Long.toString(hrefTotal));
		dataStats.put("errorTotal", Long.toString(errorTotal));

		return dataStats;
	}

	// urls could contain \n and we need to escape this
	private static String escape(String input) {
		if (input != null) {
			return input.replace("\\", "\\\\");
		}
		return null;
	}
}
