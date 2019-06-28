package org.webdatacommons.structureddata.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.commoncrawl.protocol.shared.ArcFileItem;
import org.webdatacommons.structureddata.extractor.RDFExtractor;
import org.webdatacommons.structureddata.extractor.RDFExtractor.ExtractorResult;
import org.webdatacommons.structureddata.io.CSVStatWriter;
import org.webdatacommons.structureddata.util.WARCRecordUtils;

import de.dwslab.dwslib.framework.Processor;

public class Extractor extends Processor<String> {

	private String inputFolderName;
	private File outputDataFolder;
	private File outputStatFolder;

	public static final String PAGES_GUESSED_TRIPLES = "pagesGuessedTriples";
	public static final String PAGES_TRIPLES = "pagesTriples";

	public CSVStatWriter statWriter;

	public static void main(String[] args) {

		if (args == null || args.length < 3) {
			System.out
					.println("Usage: <inputfolder> <outputfolder> <numthreads>");
			System.exit(0);
		}

		Extractor ex = new Extractor(args[0], args[1],
				Integer.parseInt(args[2]));
		ex.process();
	}

	@Override
	protected void afterProcess() {
		statWriter.close();
	}

	public Extractor(String inputFolderName, String outputFolderName,
			int threads) {
		super(threads);
		this.inputFolderName = inputFolderName;
		File outputFolder = new File(outputFolderName);
		try {
			statWriter = new CSVStatWriter(new File(outputFolder,
					"global_stats.gz"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(0);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		outputDataFolder = new File(outputFolder, "data");
		outputDataFolder.mkdir();
		outputStatFolder = new File(outputFolder, "stats");
		outputStatFolder.mkdir();
	}

	@Override
	protected List<String> fillListToProcess() {
		File inputFolder = new File(inputFolderName);
		return Arrays.asList(inputFolder.list());
	}

	@Override
	protected void process(String object) throws Exception {

		// some STATS
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
		// number of triples found on all pages
		long triplesTotal = 0;
		// current time of the system when starting process.
		long start = System.currentTimeMillis();

		File file = new File(inputFolderName, object);
		if (file.isDirectory()){
			return;
		}
		// the WARC FILE

		// WARCReader reader = WARCReaderFactory.get(file);
		// Iterator<ArchiveRecord> readerIt = reader.iterator();

		// ScrappyWarcReader reader = new ScrappyWarcReader(file);

		final ArchiveReader reader = ArchiveReaderFactory.get(object,
				new FileInputStream(file), true);
		Iterator<ArchiveRecord> readerIt = reader.iterator();

		// Extractor
		File outputFile = new File(outputDataFolder, file.getName().replace(
				".gz", ".nq.gz"));
		outputFile.createNewFile();
		GZIPOutputStream stream = new GZIPOutputStream(new FileOutputStream(
				outputFile));
		RDFExtractor extractor = new RDFExtractor(stream);

		// get output handler
		// get handler for page stats
		File statsFile = new File(outputStatFolder, file.getName().replace(
				".gz", ".stat.gz"));
		statsFile.createNewFile();
		CSVStatWriter pageStatHandler = new CSVStatWriter(statsFile);

		// read all entries in the ARC file
		// while (reader.hasNext()) {
		while (readerIt.hasNext()) {

			// ScrappyWarcRecord record = reader.getNext();
			ArchiveRecord record = readerIt.next();

			// we want only valid URIs and HOSTS
			String uri = record.getHeader().getUrl();
			if (uri == null || uri.length() < 7){
				// skip if th eURL is unknown.
				continue;
			}

			// if there is more than nothing

			byte[] bytes = IOUtils.toByteArray(WARCRecordUtils
					.getPayload(record));
			if (bytes.length > 0) {
				// item.setMimeType(contentType);
				// item.setContent(new FlexBuffer(bytes, true));
				// item.setUri(uri.toString());

				// if (extractor.supports(item.getMimeType())) {
				if (true) {
					// do extraction (woo ho)
					pagesParsed++;

					ExtractorResult result = extractor.extract(uri,
							"text/html", bytes);

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
						// stats.putAll(itemStats(item));
						stats.putAll(result.getExtractorTriples());
						stats.putAll(result.getReferencedData());
						stats.put("detectedMimeType", result.getMimeType());
						stats.put("totalTriples",
								Long.toString(result.getTotalTriples()));
						stats.put("URI", uri.toString());
						if (result.getTotalTriples() > 0) {
							pagesTriples++;
							triplesTotal += result.getTotalTriples();
						}

						// write statistics about pages without errors but not
						// necessary with triples
						pageStatHandler.write(stats);
						pagesGuessedTriples++;
					}
				}
				pagesTotal++;
			}

		}

		// close stats
		pageStatHandler.close();
		// close stream
		stream.close();
		// close reader
		reader.close();

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
		dataStats.put("TriplesTotal", Long.toString(triplesTotal));
		dataStats.put("pagesErrors", Long.toString(pagesErrors));
		dataStats.put("FileName", file.getName());
		statWriter.write(dataStats);
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

}
