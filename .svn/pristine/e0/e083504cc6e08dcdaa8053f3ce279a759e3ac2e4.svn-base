package org.webdatacommons.structureddata.extractor;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.any23.Any23;
import org.apache.any23.ExtractionReport;
import org.apache.any23.extractor.ExtractionParameters;
import org.apache.any23.extractor.ExtractorFactory;
import org.apache.any23.extractor.ExtractorGroup;
import org.apache.any23.extractor.microdata.MicrodataExtractorFactory;
import org.apache.any23.mime.MIMEType;
import org.apache.any23.source.ByteArrayDocumentSource;
import org.apache.any23.source.DocumentSource;
import org.apache.any23.vocab.HRecipe;
import org.apache.any23.vocab.SINDICE;
import org.apache.any23.vocab.XHTML;
import org.apache.log4j.Logger;
import org.commoncrawl.protocol.shared.ArcFileItem;
import org.webdatacommons.structureddata.util.FilterableTripleHandler;

/**
 * Wraps a Any23 instance, defines a specific set of extractors, a list of
 * property namespaces to be filtered. It extracts RDF, collects several
 * statistics on the extraction process, and writes triples to files. For added
 * performance, regex guessers are used to find out whether the document
 * contains data for the specific formats at all.
 * 
 * @author Hannes Muehleisen (hannes@muehleisen.org)
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 */
public class RDFExtractor {

	public final static List<String> EXTRACTORS = Arrays.asList(
			"html-microdata", "html-rdfa");

	private static ExtractorGroup extractorGroup;
	static {
		List<ExtractorFactory<?>> factories = new ArrayList<ExtractorFactory<?>>();
		factories.add(new MicrodataExtractorFactory());
		factories.add(new BaselessRDFaExtractorFactory());
		extractorGroup = new ExtractorGroup(factories);
	}

	public final static Map<String, Pattern> dataGuessers = new HashMap<String, Pattern>();
	static {
		Map<String, String> guessers = new HashMap<String, String>();

		guessers.put("html-microdata", "(itemscope|itemprop\\s*=)");
		guessers.put("html-rdfa", "(property|typeof|about|resource)\\s*=");

		for (Map.Entry<String, String> guesser : guessers.entrySet()) {
			dataGuessers.put(guesser.getKey(), Pattern.compile(
					guesser.getValue(), Pattern.CASE_INSENSITIVE));
		}
	}

	public final static Map<String, Pattern> formatGuessers = new HashMap<String, Pattern>();
	static {
		String rdfFormatsRegex = "((text/(rdf+nq|nq|nquads|n-quads|nt|ntriples|rdf|rdf+xml|rdf+n3|n3|turtle))|(application/(rdf+xml|rdf|n3|x-turtle|turtle)))";
		formatGuessers.put("scriptData", Pattern
				.compile("<script[^>]*type\\s*=\\s*(\"|')" + rdfFormatsRegex
						+ "[^\"']*", Pattern.CASE_INSENSITIVE));
		formatGuessers.put("linkRelData", Pattern.compile(
				"<link[^>]*type\\s*=\\s*(\"|')" + rdfFormatsRegex + "[^\"']*",
				Pattern.CASE_INSENSITIVE));
	}

	// exclude namesspaces which cause title and css links to be included as
	// triples
	public final static List<String> evilNamespaces = Arrays.asList(XHTML.NS,
			SINDICE.NS);
	// allow namespaces which are subsets of evilNamespaces to be included
	public final static List<String> notSoEvilNamespaces = Arrays
			.asList(HRecipe.NS);

	private static Logger log = Logger.getLogger(RDFExtractor.class);

	private Any23 any23Parser;
	ExtractionParameters any23ExParams;
	private OutputStreamWriter outputStreamWriter;

	public RDFExtractor(OutputStream output)
			throws UnsupportedEncodingException {
		any23ExParams = ExtractionParameters.newDefault();
		any23ExParams.setFlag("any23.extraction.metadata.timesize", false);
		any23ExParams.setFlag("any23.extraction.head.meta", false);

		any23Parser = new Any23(extractorGroup);

		this.outputStreamWriter = new OutputStreamWriter(output, "UTF-8");
	}

	public static class ExtractorResult {

		private static String REFERENCED_DATA_FORMAT = "referencedData";
		private String detectedMimeType = "";
		private boolean hadError = false;
		private Map<String, Long> extractorTriples = new HashMap<String, Long>();
		private String referencedData = "";
		private boolean hadResults = false;

		private long totalTriples = 0;

		public Map<String, String> getExtractorTriples() {
			Map<String, String> extractionStats = new HashMap<String, String>();
			for (Map.Entry<String, Long> statEntry : extractorTriples
					.entrySet()) {
				extractionStats.put(statEntry.getKey(),
						Long.toString(statEntry.getValue()));
			}
			return extractionStats;
		}

		/**
		 * Gets a Map with one entry which includes the referenced data format
		 * which was indicate to parse the file
		 * 
		 * @return Map where REFERENCED_DATA_FORMAT is the key and the data type
		 *         the value.
		 */
		public Map<String, String> getReferencedData() {
			Map<String, String> extractionStats = new HashMap<String, String>();
			extractionStats.put(REFERENCED_DATA_FORMAT, referencedData);
			return extractionStats;
		}

		public boolean hadResults() {
			return hadResults;
		}

		public boolean hadError() {
			return hadError;
		}

		public String getMimeType() {
			return detectedMimeType;
		}

		public long getTotalTriples() {
			return totalTriples;
		}
	}

	/**
	 * Checks if the documentContent is likely to be interesting for the
	 * extraction of structured data.
	 * 
	 * @param documentContent
	 *            content of the document
	 * @param result
	 *            the current extractur result
	 * @return true if structured data is likely to be included.
	 */
	private boolean interesting(String documentContent, ExtractorResult result) {
		// check if the document contains references to structured data,
		// e.g. as "<link rel"
		for (Map.Entry<String, Pattern> guesser : formatGuessers.entrySet()) {
			Matcher m = guesser.getValue().matcher(documentContent);
			boolean match = m.find();
			if (match) {
				result.referencedData = guesser.getKey();
				result.hadResults = true;
				return true;
			}
		}
		// check if document contains any structured data
		// possible extension: create ad-hoc extractor list from matches
		for (Map.Entry<String, Pattern> guesser : dataGuessers.entrySet()) {
			Matcher m = guesser.getValue().matcher(documentContent);
			boolean match = m.find();
			if (match) {
				result.referencedData = guesser.getKey();
				result.hadResults = true;
				return true;
			}
		}
		return false;
	}

	public ExtractorResult extract(String uri, String mimeType, byte[] content) {
		ExtractorResult result = new ExtractorResult();

		try {
			String documentContent = new String(content, "UTF-8");
			if (!interesting(documentContent, result)) {
				// if guessers do not match, return empty result
				return result;
			}

			DocumentSource any23Source = new ByteArrayDocumentSource(
					documentContent.getBytes("UTF-8"), uri, mimeType);

			FilterableTripleHandler writer = new FilterableTripleHandler(
					outputStreamWriter, evilNamespaces, notSoEvilNamespaces);

			/**
			 * Call any23 extractor
			 * */
			ExtractionReport report = any23Parser.extract(any23ExParams,
					any23Source, writer);

			result.detectedMimeType = report.getDetectedMimeType();
			result.totalTriples = writer.getTotalTriplesFound();
			result.extractorTriples = writer.getTriplesPerExtractor();

		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Unable to parse " + uri, e);
			}
			result.hadError = true;
		}
		return result;
	}

	public ExtractorResult extract(ArcFileItem item) {
		ExtractorResult result = new ExtractorResult();

		try {
			String documentContent = item.getContent().toString("UTF-8");
			if (!interesting(documentContent, result)) {
				// if guessers do not match, return empty result
				return result;
			}

			DocumentSource any23Source = new ByteArrayDocumentSource(
					documentContent.getBytes("UTF-8"), item.getUri(),
					item.getMimeType());

			FilterableTripleHandler writer = new FilterableTripleHandler(
					outputStreamWriter, evilNamespaces, notSoEvilNamespaces);

			/**
			 * Call any23 extractor
			 * */
			ExtractionReport report = any23Parser.extract(any23ExParams,
					any23Source, writer);

			result.detectedMimeType = report.getDetectedMimeType();
			result.totalTriples = writer.getTotalTriplesFound();
			result.extractorTriples = writer.getTriplesPerExtractor();

		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Unable to parse " + item.getUri(), e);
			}
			result.hadError = true;
		}
		return result;
	}

	public boolean supports(String mimeType) {
		try {
			MIMEType type = MIMEType.parse(mimeType);
			return !extractorGroup.filterByMIMEType(type).isEmpty();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

}
