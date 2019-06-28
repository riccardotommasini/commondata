package org.fuberlin.wbsg.ccrdf;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.commoncrawl.protocol.shared.ArcFileItem;
import org.commoncrawl.util.shared.FlexBuffer;
import org.fuberlin.wbsg.ccrdf.RDFExtractor.ExtractorResult;

import org.webdatacommons.utils.WARCRecordUtils;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * Uses the {@link org.archive.io.ArchiveReaderFactory} from the UKWA codebase
 * to read entries from WARC file. For each entry we then call the Any23 RDF
 * extractor and collect statistics.
 * 
 * @author Hannes Muehleisen (hannes@muehleisen.org)
 * @author Petar Petrovski (petar@informatik.uni-mannheim.de)
 */
public class WarcProcessor {


    private static Logger log = Logger.getLogger(WarcProcessor.class);

	public Map<String, String> processArcData(
			final ReadableByteChannel gzippedArcFileBC, String arcFileName,
			RDFExtractor extractor, StatHandler statHandler,
			boolean logRegexErrors) throws IOException {

		final ArchiveReader reader = ArchiveReaderFactory.get(
				arcFileName, Channels.newInputStream(gzippedArcFileBC), true);

		log.info("Extracting data from " + arcFileName + " ...");

		// number of pages visited for extraction
		long pagesTotal = 0;
		// number of pages parsed based on supported mime-type
		long pagesParsed = 0;
		// number of pages which contain an error
		long pagesErrors = 0;
		// number of pages which are likely to include triples
		long pagesGuessedTriples = 0;
		// number of pages including at least one triple
		long pagesTriples = 0;
		// current time of the system when starting process.
		long start = System.currentTimeMillis();

		// TODO LOW write regex detection errors into SDB
		BufferedWriter bwriter = null;
		if (logRegexErrors) {
			FileWriter writer = new FileWriter(new File("regexFail_"
					+ arcFileName.replace("/", "_") + ".txt"));
			bwriter = new BufferedWriter(writer);
		}

		Iterator<ArchiveRecord> readerIt = reader.iterator();

		// read all entries in the ARC file
		while (readerIt.hasNext()) {

			if(pagesTotal%1000==0){
                log.info(pagesTotal + " / " + pagesParsed + " / " + pagesTriples + " / " + pagesErrors);
            }
			ArchiveRecord record = readerIt.next();
            ArchiveRecordHeader header = record.getHeader();
			ArcFileItem item = new ArcFileItem();
            URI uri;






			item.setArcFileName(arcFileName);

            // WARC contains lots of stuff. We only want HTTP responses
            if (!header.getMimetype().equals("application/http; msgtype=response")) {
                continue;
            }

           try{
            uri = new URI(header.getUrl());
            String host = uri.getHost();
            if (host == null) {
               continue;
            }
           } catch (URISyntaxException e) {
               log.error("Invalid URI!!!", e);
               continue;
           }


            String headers[] = WARCRecordUtils.getHeaders(record, true).split("\n");
            if (headers.length < 1) {
                pagesTotal++;
                continue;
            }

                // only consider HTML responses
            String contentType = headerKeyValue(headers, "Content-Type","text/html");
            if (!contentType.contains("html")){
                pagesTotal++;
                continue;
            }

            byte[] bytes = IOUtils.toByteArray(WARCRecordUtils.getPayload(record));

            if(bytes.length > 0){




            item.setMimeType(contentType);
			item.setContent(new FlexBuffer(bytes, true));
            item.setUri(uri.toString());

			
			/*
			 * try { reader.getNextItem(item); } catch (Exception e) {
			 * log.warn(e, e.fillInStackTrace()); pagesErrors++; continue; }
			 */

			// log.info(item.getContent().toString("UTF-8").substring(0,500));

                if (extractor.supports(item.getMimeType())) {
                    // do extraction (woo ho)
                    pagesParsed++;

                    ExtractorResult result = extractor.extract(item);


                    // if we had an error, increment error count
                    if (result.hadError()) {
                        pagesErrors++;
                        pagesTotal++;
                        continue;
                    }
                    // if we found no triples, continue
                    if (result.hadResults()) {
                        // collect some other statistics
                        Map<String, String> stats = new HashMap<String, String>();
                        stats.putAll(itemStats(item));
                        stats.putAll(result.getExtractorTriples());
                        stats.putAll(result.getReferencedData());
                        stats.put("detectedMimeType", result.getMimeType());
                        stats.put("totalTriples",
                                Long.toString(result.getTotalTriples()));

                        if (result.getTotalTriples() > 0) {
                            pagesTriples++;
                        } else {
                            log.warn("Could not find any triple in file, although guesser found something.");
                            if (logRegexErrors) {
                                String documentContent = item.getContent()
                                        .toString("UTF-8");
                                bwriter.write("[Item without triple on position: "
                                        + item.getArcFilePos() + "]\n\n");
                                for (String key : result.getReferencedData()
                                        .keySet()) {
                                    bwriter.write(key + " : "
                                            + result.getReferencedData().get(key)
                                            + "\n");
                                }
                                bwriter.write("\n");
                                bwriter.write(documentContent);
                                bwriter.write("\n\n\n#########################\n\n\n");
                            }
                        }

                        // write statistics
                        statHandler.addStats(item.getUri(), stats);
                        pagesGuessedTriples++;
                    }
                }
                pagesTotal++;

            }
        }
		if (logRegexErrors) {
			bwriter.flush();
			try {
				bwriter.close();
			} catch (IOException e) {
				// do nothing;
			}
		}
		statHandler.flush();

		double duration = (System.currentTimeMillis() - start) / 1000.0;
		double rate = (pagesTotal * 1.0) / duration;

		// create data file statistics and return
		Map<String, String> dataStats = new HashMap<String, String>();
		dataStats.put("duration", Double.toString(duration));
		dataStats.put("rate", Double.toString(rate));
		dataStats.put("pagesTotal", Long.toString(pagesTotal));
		dataStats.put("pagesParsed", Long.toString(pagesParsed));
		dataStats
				.put(PAGES_GUESSED_TRIPLES, Long.toString(pagesGuessedTriples));
		dataStats.put(PAGES_TRIPLES, Long.toString(pagesTriples));
		dataStats.put("pagesErrors", Long.toString(pagesErrors));

		log.info("Extracted data from " + arcFileName + " - parsed "
				+ pagesParsed + " pages in " + duration + " seconds, " + rate
				+ " pages/sec");

		reader.close();
		return dataStats;
	}

	public static final String PAGES_GUESSED_TRIPLES = "pagesGuessedTriples";
	public static final String PAGES_TRIPLES = "pagesTriples";

	protected static Map<String, String> itemStats(ArcFileItem item) {
		Map<String, String> stats = new HashMap<String, String>();
		// data about the file where this data came from
		stats.put("arcFileName", item.getArcFileName());
		stats.put("arcFilePos", Integer.toString(item.getArcFilePos()));
		stats.put("recordLength", Integer.toString(item.getRecordLength()));

		// data about the web page crawled
		stats.put("uri", item.getUri());
		stats.put("hostIp", item.getHostIP());
		stats.put("timestamp", Long.toString(item.getTimestamp()));
		stats.put("mimeType", item.getMimeType());
		return stats;
	}


    // some Hannes-TM HTTP header parsing kludges, way faster than libs
    public static String headerValue(String[] headers, String key, String dflt) {
        for (String hdrLine : headers) {
            if (hdrLine.toLowerCase().trim().startsWith(key.toLowerCase())) {
                return hdrLine.trim();
            }
        }
        return dflt;
    }

    public static String headerKeyValue(String[] headers, String key,
                                        String dflt) {
        String line = headerValue(headers, key, null);
        if (line == null)
            return dflt;
        String[] pces = line.split(":");
        if (pces.length != 2)
            return dflt;
        return pces[1].trim();
    }

	/**
	 * Main method to run the ARC extractor from the command line
	 * */
	public static void main(String[] args) throws IOException, JSAPException {
		JSAP jsap = new JSAP();
		UnflaggedOption arcFileParam = new UnflaggedOption("arcfile")
				.setStringParser(JSAP.STRING_PARSER).setRequired(true)
				.setGreedy(true);

		arcFileParam.setHelp("gzipped ARC files with web crawl data");

		jsap.registerParameter(arcFileParam);

		JSAPResult config = jsap.parse(args);

		if (!config.success()) {
			System.err.println("Usage: " + WarcProcessor.class.getName() + " "
					+ jsap.getUsage());
			System.err.println(jsap.getHelp());
			System.exit(1);
		}

		List<String> arcFiles = Arrays.asList(config.getStringArray("arcfile"));
		for (String arcFileS : arcFiles) {
			File arcFile = new File(arcFileS);
			if (!arcFile.exists() || !arcFile.canRead()) {
				System.err.println("Unable to open " + arcFile);
				continue;
			}

			File quadFile = new File(arcFile.toString() + ".nq");

			RDFExtractor extractor = new RDFExtractor(new FileOutputStream(
					quadFile));

			StatHandler pageStatHandler = new LoggingStatHandler();

			new WarcProcessor().processArcData(
					Channels.newChannel(new FileInputStream(arcFile)),
					arcFile.toString(), extractor, pageStatHandler, false);
		}
	}
}
