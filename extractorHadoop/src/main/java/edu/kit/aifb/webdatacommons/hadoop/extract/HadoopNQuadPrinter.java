package edu.kit.aifb.webdatacommons.hadoop.extract;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.deri.any23.writer.FormatWriter;
import org.openrdf.rio.RDFWriter;


public class HadoopNQuadPrinter extends HadoopTripleHandler implements FormatWriter{

	HadoopNQuadPrinter(OutputCollector<Text, Text> output, Text key) {
		super(new HadoopNQuadWriter(output, key));
		
	}

	public String getMIMEType() {
		return "text/plain";
	}

}
