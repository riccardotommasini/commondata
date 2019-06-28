package edu.kit.aifb.webdatacommons.hadoop.extract;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.deri.any23.extractor.ExtractionContext;
import org.deri.any23.rdf.RDFUtils;
import org.deri.any23.writer.NQuadsWriter;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;



public class HadoopTripleHandler implements TripleHandler {

    private final RDFWriter writer;

    private boolean closed = false;

    HadoopTripleHandler(RDFWriter destination) {
        writer = destination;
        try {
            writer.startRDF();
        } catch (RDFHandlerException e) {
            throw new RuntimeException(e);
        }
    }

    public void startDocument(URI documentURI) throws TripleHandlerException {
        // Empty.
    }

    public void openContext(ExtractionContext context) throws TripleHandlerException {
        // Empty.
    }

    public void receiveTriple(Resource s, URI p, Value o, URI g, ExtractionContext context)
    throws TripleHandlerException {
        final URI graph = g == null ? context.getDocumentURI() : g;
        try {
            writer.handleStatement(
                    RDFUtils.quad(s, p, o, graph));
        } catch (RDFHandlerException ex) {
            throw new TripleHandlerException(
                    String.format("Error while receiving triple: %s %s %s %s", s, p, o, graph),
                    ex
            );
        }
    }

    public void receiveNamespace(String prefix, String uri, ExtractionContext context)
    throws TripleHandlerException {
        try {
            writer.handleNamespace(prefix, uri);
        } catch (RDFHandlerException ex) {
            throw new TripleHandlerException(String.format("Error while receiving namespace: %s:%s", prefix, uri),
                    ex
            );
        }
    }

    public void closeContext(ExtractionContext context) throws TripleHandlerException {
        // Empty.
    }

    public void close() throws TripleHandlerException {
        if (closed) return;
        closed = true;
        try {
            writer.endRDF();
        } catch (RDFHandlerException e) {
            throw new TripleHandlerException("Error while closing the triple handler.", e);
        }
    }

    public void endDocument(URI documentURI) throws TripleHandlerException {
        // Empty.
    }

    public void setContentLength(long contentLength) {
        // Empty.
    }


}
