package edu.kit.aifb.webdatacommons.hadoop.extract;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.deri.any23.extractor.ExtractionContext;
import org.deri.any23.writer.NQuadsWriter;
import org.deri.any23.writer.TripleHandlerException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class HadoopTripleFilterWriter extends HadoopNQuadPrinter {

	private long totalTriples = 0;
	private Map<String, Long> triplesPerExtractor = new HashMap<String, Long>();
	private List<String> filterNamespaces;

	public HadoopTripleFilterWriter(OutputCollector<Text, Text> output, Text key, List<String> filterNamespaces) {
		super(output, key);
		this.filterNamespaces = filterNamespaces;
		for (String ex : RDFExtractor.EXTRACTORS) {
			triplesPerExtractor.put(ex, new Long(0));
		}
	}

	@Override
	public void receiveTriple(Resource s, URI p, Value o, URI g,
			ExtractionContext context) throws TripleHandlerException {
		for (String filterNamespace : filterNamespaces) {
			if (p.toString().startsWith(filterNamespace)) {
				return;
			}
		}

		super.receiveTriple(s, p, o, g, context);
		totalTriples++;
		String ex = context.getExtractorName();
		if (triplesPerExtractor.containsKey(ex)) {
			triplesPerExtractor.put(ex, new Long(
					triplesPerExtractor.get(ex) + 1));
		}
	}

	public long getTotalTriplesFound() {
		return totalTriples;
	}

	public Map<String, Long> getTriplesPerExtractor() {
		return triplesPerExtractor;
	}
}
