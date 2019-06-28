package edu.kit.aifb.webdatacommons.hadoop.extract;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import org.commoncrawl.protocol.shared.ArcFileItem;


/**
 * Hadoop Mapper. SendsArcFileItems to the RDFExtractor, collects statistics and passes them to the Reducer. 
 * 
 * @author Steffen Stadtmüller <steffen.stadtmueller@kit.edu>
 */
public class ExtractMapper extends MapReduceBase 
  implements Mapper<Text, ArcFileItem, Text, Text> {

  public void map(Text key, ArcFileItem value,
      OutputCollector<Text, Text> output, Reporter reporter)
      throws IOException {
    try {
    
    	RDFExtractor extractor = new RDFExtractor(output, new Text(value.getArcFileName()), true);
    	if (extractor.supports(value.getMimeType())) {
    		
    		// parse item and return stats
    		//Map<String, Object> stats = extractor.extract(value);
    		StringBuilder sb = extractor.extract(value);
    		
    		if (!sb.toString().equals("hadExtractionError")) {
	    		// data about the file where this data came from
	    		sb.append(value.getArcFileName()+",");
	    		sb.append(value.getArcFilePos()+",");
	    		sb.append(value.getRecordLength()+",");
	    		
	    		// data about the web page crawled
	    		sb.append(value.getUri()+",");
	    		sb.append(value.getHostIP()+",");
	    		sb.append(value.getTimestamp()+",");
	    		sb.append(value.getMimeType()+",");
	    		
	    		output.collect(new Text("stats4"+value.getUri()), new Text(sb.toString()));
    		}
    		
//    		// data about the file where this data came from
//    		stats.put("arcFileName", value.getArcFileName());
//    		stats.put("arcFilePos", value.getArcFilePos());
//    		stats.put("recordLength", new Integer(value.getRecordLength()));
//
//    		// data about the web page crawled
//    		stats.put("uri", value.getUri());
//    		stats.put("hostIp", value.getHostIP());
//    		stats.put("timestamp", new Long(value.getTimestamp()));
//    		stats.put("mimeType", value.getMimeType());
    	
	    	// only write entries that had results
			// TODO: do we want this?
//			if (!(Boolean) stats.get("hadExtractionError")) {
//				StringBuffer sb = new StringBuffer();
//				for(Entry<String, Object> entry : stats.entrySet()){
//					sb.append(entry.getKey()+" : "+entry.getValue().toString()+", ");
//				}
//				output.collect(new Text("stats4"+value.getUri()), new Text(sb.toString()));
//			}
    	}
    }
    catch (Exception e) {
      reporter.getCounter("ExtractMapper.exception",
          e.getClass().getSimpleName()).increment(1);
    }
  }

}
