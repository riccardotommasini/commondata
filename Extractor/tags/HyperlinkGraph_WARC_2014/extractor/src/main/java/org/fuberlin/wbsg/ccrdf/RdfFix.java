package org.fuberlin.wbsg.ccrdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.fuberlin.wbsg.ccrdf.tablegen.RobustNquadsParser;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.apache.any23.io.nquads.NQuadsWriter;

public class RdfFix {
	private static Logger log = Logger.getLogger(RdfFix.class);

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 */
	public static void main(String[] args) {
		File inDir = new File("/home/hannes/webdatacommons/cc2010/data");
		File outDir = new File("/home/hannes/webdatacommons/cc2010/datac");

		if (!inDir.exists()) {
			log.warn("Input directory " + inDir + " missing.");
			return;
		}
		if (!outDir.exists()) {
			log.info("Output directory " + outDir
					+ " missing, trying to create...");
			outDir.mkdirs();
		}
		String[] files = inDir.list();
		for (String file : files) {
			if (!file.endsWith(".nq.gz")) {
				continue;
			}
			File inputFile = new File(inDir + File.separator + file);
			File outputFile = new File(outDir + File.separator
					+ inputFile.getName());
			if (outputFile.exists()) {
				log.info("Skipping " + outputFile);
				continue;
			}
			try {
				fix(inputFile, outputFile);
			} catch (Exception e) {
				log.warn("Failed to fix " + file,e);
			}
		}
	}

	public static void fix(File inputFile, File outputFile)
			throws FileNotFoundException, IOException, RDFHandlerException,
			RDFParseException {
		RDFParser parser = new RobustNquadsParser() {
			protected BNode createBNode(String nodeID) throws RDFParseException {
				return ValueFactoryImpl.getInstance().createBNode(nodeID);
			}
		};
		parser.setDatatypeHandling(DatatypeHandling.IGNORE);

		OutputStream os = new GZIPOutputStream(new FileOutputStream(outputFile));

		final RDFWriter writer = new NQuadsWriter(os);
		writer.startRDF();
		parser.setRDFHandler(new RDFHandlerBase() {
			ValueFactory vf = ValueFactoryImpl.getInstance();
			Pattern languageTagPattern = Pattern.compile("^[a-z0-9]+-[a-z0-9]+$");
			
			@Override
			public void handleStatement(Statement orgS)
					throws RDFHandlerException {
				try {
					Resource subject;
					if (orgS.getSubject() instanceof URI) {

						subject = fixUri((URI) orgS.getSubject());

					} else {
						subject = orgS.getSubject();
					}

					URI predicate = fixUri(orgS.getPredicate());

					// object may be uri, bnode or literal, so check
					Value object;
					if (orgS.getObject() instanceof URI) {
						object = fixUri((URI) orgS.getObject());
					} else {
						object = orgS.getObject();
					}
					
					if (object instanceof Literal) {
						Literal l = (Literal) object;
						if (l.getLanguage() != null) {
							String tag = l.getLanguage();
							Matcher m = languageTagPattern.matcher(tag);
							if (!m.matches()) {
								tag = tag.replace('_','-').toLowerCase();
								Matcher m2 = languageTagPattern.matcher(tag);
								if (m2.matches()) {
									object = vf.createLiteral(l.getLabel(), tag);
								}
								else object = vf.createLiteral(l.getLabel());
							}
						}
					}

					// context may be uri or bnode
					Resource context;
					if (orgS.getContext() instanceof URI) {
						context = fixUri((URI) orgS.getContext());

					} else {
						context = orgS.getContext();
					}

					Statement cleanStatement = vf.createStatement(subject,
							predicate, object, context);
					writer.handleStatement(cleanStatement);
				} catch (URISyntaxException e) {
					log.warn("Unable to create nice URIs for " + orgS);
				}
			}

			private URI fixUri(URI oldUri) throws URISyntaxException {
				if (oldUri == null) {
					throw new URISyntaxException("URI is null", null);
				}
				String u = oldUri.stringValue();
				if (!u.startsWith("http")){
					return oldUri;
				}
				if (u.contains(" ") || u.contains("<") || u.contains(">")) {
					u = u.replace(' ', '+');
					u = u.replace("<", "");
					u = u.replace(">", "");
					return vf.createURI(u);
				}
				return oldUri;
			}
		});
		parser.setStopAtFirstError(false);
		parser.setParseErrorListener(new ParseErrorListener() {
			@Override
			public void warning(String msg, int lineNo, int colNo) {
				log.info("RDF parser warning: " + msg + " (" + lineNo + ":"
						+ colNo + ")");
			}

			@Override
			public void fatalError(String msg, int lineNo, int colNo) {
				log.info("RDF parser fatal error: " + msg + " (" + lineNo + ":"
						+ colNo + ")");
			}

			@Override
			public void error(String msg, int lineNo, int colNo) {
				log.info("RDF parser error: " + msg + " (" + lineNo + ":"
						+ colNo + ")");
			}
		});
		
		log.info("Parsing " + inputFile);
		parser.parse(new InputStreamReader(new GZIPInputStream(
				new FileInputStream(inputFile))), "p:base");
		writer.endRDF();
		os.close();
	}
}
