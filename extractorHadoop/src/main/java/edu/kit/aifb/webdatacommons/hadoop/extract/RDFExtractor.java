package edu.kit.aifb.webdatacommons.hadoop.extract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.log4j.Logger;
import org.commoncrawl.protocol.shared.ArcFileItem;
import org.deri.any23.Any23;
import org.deri.any23.ExtractionReport;
import org.deri.any23.extractor.ExtractionParameters;
import org.deri.any23.extractor.ExtractionParameters.ValidationMode;
import org.deri.any23.extractor.ExtractorGroup;
import org.deri.any23.extractor.ExtractorRegistry;
import org.deri.any23.mime.MIMEType;
import org.deri.any23.source.ByteArrayDocumentSource;
import org.deri.any23.source.DocumentSource;
import org.deri.any23.vocab.SINDICE;
import org.deri.any23.vocab.XHTML;

/**
 * Wraps a Any23 instance, defines a specific set of extractors, a list of
 * property namespaces to be filtered. It extracts RDF, collects several
 * statistics on the extraction process, and writes triples to files. For added
 * performance, regex guessers are used to find out whether the document
 * contains data for the specific formats at all.
 * 
 * @author Hannes Muehleisen (hannes@muehleisen.org)
 */
public class RDFExtractor {

	public final static List<String> EXTRACTORS = Arrays.asList("html-rdfa",
			"html-microdata", "html-mf-geo", "html-mf-hcalendar",
			"html-mf-hcard", "html-mf-hlisting", "html-mf-hresume",
			"html-mf-hreview", "html-mf-species", "html-mf-license",
			"html-mf-hrecipe", "html-mf-xfn");

	public final static Map<String, Pattern> dataGuessers = new HashMap<String, Pattern>();
	OutputCollector<Text, Text> output;
	Text key;

	static {
		Map<String, String> guessers = new HashMap<String, String>();

		// TODO: check performance penalty of * operators and possible
		// workarounds

		guessers.put("html-rdfa", "(property|typeof|about|resource)\\s*=");
		guessers.put("html-microdata", "(itemscope|itemprop\\s*=)");

		// microdata guessers
		guessers.put("html-mf-geo", "class\\s*=\\s*(\"|')[^\"']*geo");
		guessers.put("html-mf-species", "class\\s*=\\s*(\"|')[^\"']*species");
		guessers.put("html-mf-license", "rel\\s*=\\s*(\"|')license");
		guessers.put(
				"html-mf-xfn",
				"<a[^>]*rel\\s*=\\s*(\"|')[^\"']*(contact|acquaintance|friend|met|co-worker|colleague|co-resident|neighbor|child|parent|sibling|spouse|kin|muse|crush|date|sweetheart|me)");

		// following formats define unique enough main CSS class names
		guessers.put("html-mf-hcalendar", "(vcalendar|vevent)");
		guessers.put("html-mf-hcard", "vcard");
		guessers.put("html-mf-hlisting", "hlisting");
		guessers.put("html-mf-hresume", "hresume");
		guessers.put("html-mf-hreview", "hreview");
		guessers.put("html-mf-recipe", "hrecipe");

		for (Map.Entry<String, String> guesser : guessers.entrySet()) {
			dataGuessers.put(guesser.getKey(),
					Pattern.compile(guesser.getValue()));
		}
	}

	public final static List<String> evilNamespaces = Arrays.asList(XHTML.NS,
			SINDICE.NS);

	private static Logger log = Logger.getLogger(RDFExtractor.class);

	private Any23 any23Parser;
	private ExtractorGroup any23extractorGroup;
	ExtractionParameters any23ExParams;
	boolean useGuessers;

	public RDFExtractor(OutputCollector<Text, Text> output, Text key, boolean useGuessers)
			throws FileNotFoundException {
		this.output=output;
		this.key=key;
		
		any23extractorGroup = ExtractorRegistry.getInstance()
				.getExtractorGroup(EXTRACTORS);

		any23ExParams = new ExtractionParameters(ValidationMode.None);
		any23ExParams.setFlag("any23.extraction.metadata.timesize", false);
		any23ExParams.setFlag("any23.extraction.head.meta", false);

		any23Parser = new Any23(any23extractorGroup);
		this.useGuessers = useGuessers;
	}

	public StringBuilder extract(ArcFileItem item) {
		//Map<String, Object> extractionStats = new HashMap<String, Object>();
		StringBuilder sb = new StringBuilder();

		try {
			String documentContent = item.getContent().toString("UTF-8");

			if (this.useGuessers) {
				// check if document contains any structured data
				// possible extension: create ad-hoc extractor list from matches
				boolean foundSth = false;
				for (Map.Entry<String, Pattern> guesser : dataGuessers
						.entrySet()) {
					Matcher m = guesser.getValue().matcher(documentContent);
					if (m.find()) {
						foundSth = true;
						break;
					}
				}

				if (!foundSth) {
					sb.append("hadExtractionError");
					//extractionStats.put("hadExtractionError", Boolean.TRUE);
					return sb;
				}
			}

			DocumentSource any23Source = new ByteArrayDocumentSource(
					documentContent.getBytes(), item.getUri(),
					item.getMimeType());

			HadoopTripleFilterWriter writer = new HadoopTripleFilterWriter(output, key, evilNamespaces);

			/**
			 * Call extractor
			 * */
			ExtractionReport report = any23Parser.extract(any23ExParams,
					any23Source, writer);
			
			sb.append(report.getDetectedMimeType()+ ",");
			//extractionStats.put("extractorMimeType", report.getDetectedMimeType());
			
			sb.append(writer.getTotalTriplesFound()+ ",");
			//extractionStats.put("triplesFound", new Long(writer.getTotalTriplesFound()));
			
			sb.append(writer.getTriplesPerExtractor()+",");
			//extractionStats.putAll(writer.getTriplesPerExtractor());
			
			//sb.append("false");
			//extractionStats.put("hadExtractionError", Boolean.FALSE);
			
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Unable to parse " + item.getUri(), e);
			}
			return new StringBuilder("hadExtractionError");
			//sb.append("hadExtractionError");
			//extractionStats.put("hadExtractionError", Boolean.TRUE);
		}
		return sb;
	}

	public boolean supports(String mimeType) {
		MIMEType type = MIMEType.parse(mimeType);
		return !any23extractorGroup.filterByMIMEType(type).isEmpty();
	}
}
