package edu.kit.aifb.webdatacommons.hadoop.extract;

import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;


/**
 * Hadoop Reducer. Writes RDf from the extractor and stat entries
 * 
 * @author Steffen Stadtmüller <steffen.stadtmueller@kit.edu>
 */
public class ExtractReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
  
	public void reduce(Text key, Iterator<Text> values,
	      OutputCollector<Text, Text> output, Reporter reporter)
	      throws IOException {

	if(key.toString().startsWith("stats4")){
		while(values.hasNext()){
			output.collect(key, values.next());
		}
	}else{
		Text empt = new Text("");
		while(values.hasNext()){
			output.collect(empt, values.next());
		}
	}
	  
	
  }
}
