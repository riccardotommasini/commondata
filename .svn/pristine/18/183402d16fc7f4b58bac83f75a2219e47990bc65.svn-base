package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class CSVGeneratorReducer extends Reducer<Text, Text, NullWritable, Text>{
	@Override
    public void reduce(Text key, Iterable<Text> values, Context context){		
		for(Text tx : values){
			try {
				context.write(NullWritable.get(), tx);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
	}

	
}