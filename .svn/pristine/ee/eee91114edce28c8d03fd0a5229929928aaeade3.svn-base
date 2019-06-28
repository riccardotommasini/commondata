package edu.kit.aifb.webdatacommons.hadoop.extract;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.deri.any23.parser.NQuads;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.ntriples.NTriplesUtil;

public class HadoopNQuadWriter implements RDFWriter{
	
	private Map<String,String> namespaceTable;
	private OutputCollector<Text, Text> output;
	private Text key;
	private boolean started = false;
	
	public HadoopNQuadWriter(OutputCollector<Text, Text> output, Text key){
		this.output=output;
		this.key=key;
	}
	 
	public void endRDF() throws RDFHandlerException {
		if(!started) {
            throw new IllegalStateException("Parsing never started.");
        }
		started = false;
        if(namespaceTable != null) {
            namespaceTable.clear();
        }
	}

	public void handleComment(String arg0) throws RDFHandlerException {
		// TODO Auto-generated method stub
		
	}

	public void handleNamespace(String ns, String uri)
			throws RDFHandlerException {
		if(!started) {
            throw new IllegalStateException("Parsing never started.");
        }

        if(namespaceTable == null) {
            namespaceTable = new HashMap<String, String>();
        }
        namespaceTable.put(ns, NTriplesUtil.escapeString(uri) );
		
	}

	public void handleStatement(Statement statement) throws RDFHandlerException {
		if(!started) {
            throw new IllegalStateException("Cannot handle statement without start parsing first.");
        }

        try {
        	output.collect(key, new Text( printQuad(statement) )) ;
       
        } catch (IOException ioe) {
            throw new RDFHandlerException("An error occurred while printing statement.", ioe);
        }
		
	}

	public void startRDF() throws RDFHandlerException {
		if(started) {
            throw new IllegalStateException("Parsing already started.");
        }
        started = true;
		
	}

	public RDFFormat getRDFFormat() {
		return NQuads.FORMAT;
	}

	private String printQuad(Statement s) throws IOException{
		StringBuilder quad = new StringBuilder();
		quad.append(printSubject(s));
		quad.append(' ');
		quad.append(printPredicate(s));
		quad.append(' ');
		quad.append(printObject(s));
		quad.append(' ');
		quad.append(printGraph(s));
		quad.append(" .");
		return quad.toString();
	}
	
	private StringBuilder printSubject(Statement s) throws IOException{
        return printResource( s.getSubject() );
    }
	
	private StringBuilder printPredicate(Statement s) throws IOException{
		return printURI( s.getPredicate() );
	}
	
	private StringBuilder printObject(Statement s) throws IOException{
		Value v = s.getObject();
        if(v instanceof Resource) {
            return printResource((Resource) v);
        }
        return printLiteral( (Literal) v );
	}
	
	private StringBuilder printGraph(Statement s) throws IOException{
		Resource graph = s.getContext();
        StringBuilder sb = new StringBuilder();
		if(graph != null) {
           sb.append( printResource( s.getContext() ) );
        }
		return sb;
	}
	
	private StringBuilder printLiteral(Literal l)throws IOException {
        StringBuilder writer = new StringBuilder();
		writer.append( NTriplesUtil.toNTriplesString(l) );
		return writer;
		
	}
	
	private StringBuilder printResource(Resource r) throws IOException {
        if(r instanceof BNode) {
           return printBNode((BNode) r);
        } else if(r instanceof URI) {
            return printURI((URI) r);
        } else {
            throw new IllegalStateException();
        }
    }
	
	private StringBuilder printURI(URI uri) throws IOException {
        final String uriString = uri.stringValue();
        int splitIdx = 0;
        String namespace = null;
        if(namespaceTable != null) {
            splitIdx = uriString.indexOf(':');
            if (splitIdx > 0) {
                String prefix = uriString.substring(0, splitIdx);
                namespace = namespaceTable.get(prefix);
            }
        }
        
        StringBuilder writer = new StringBuilder();
        if (namespace != null) {
            writer.append('<');
            writer.append(namespace);
            writer.append( NTriplesUtil.escapeString(uriString.substring(splitIdx)) );
            writer.append('>');
        } else {
            writer.append('<');
            writer.append( NTriplesUtil.escapeString(uriString) );
            writer.append('>');
        }
        return writer;
    }
	
	private StringBuilder printBNode(BNode b) throws IOException {
		StringBuilder writer = new StringBuilder();
        writer.append( NTriplesUtil.toNTriplesString(b) );
        return writer;
    }

}
