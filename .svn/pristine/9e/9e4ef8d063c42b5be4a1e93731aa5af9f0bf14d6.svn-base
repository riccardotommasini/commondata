package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.parser.NxParser;

public class NQuadRecordReader extends RecordReader<Text, Text>{

//	private CompressionCodecFactory compressionCodecs = null;
	private long start;
	private long pos;
	private long end;
	private FSDataInputStream fileIn;
	private NxParser nxp;
	private Text key;
	private Text value;
	private Node[] runner;

	@Override
	public void initialize(InputSplit genericSplit, TaskAttemptContext cont)
			throws IOException, InterruptedException {
		
		FileSplit split = (FileSplit) genericSplit;
		Configuration job = cont.getConfiguration();

	    start = split.getStart();
	    pos = 0;
	    end = start + split.getLength();
	    final Path file = split.getPath();
//	    compressionCodecs = new CompressionCodecFactory(job);
//	    final CompressionCodec codec = compressionCodecs.getCodec(file);
	    
	    // open the file
	    FileSystem fs = file.getFileSystem(job);
	    fileIn = fs.open(split.getPath());
		nxp = new NxParser(new GZIPInputStream(fileIn));
	    runner = nxp.next(); pos++;
	    
	}
	
	@Override
	public void close() throws IOException {
		if(fileIn !=null){
		    fileIn.close();
		}
	}

	@Override
	public Text getCurrentKey() throws IOException,
			InterruptedException {
		return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return (fileIn.getPos() - start) / (float) (end - start);
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if(!nxp.hasNext()){
			return false;
		}
		
		if (key == null) {
		      key = new Text();
		}
		
		if (value == null) {
		      value = new Text();
		}
		
		
		String currentContext = runner[3].toString();
		String stringValue = Nodes.toN3(runner);
		
		while(nxp.hasNext()){
			runner = nxp.next(); pos++;
			if(!currentContext.equals(runner[3].toString())){
				key.set(currentContext);
				value.set(stringValue);
				return true;
			}else{
				stringValue += " \n"+Nodes.toN3(runner);
			}
		}
		key.set(currentContext);
		value.set(stringValue);
		return true;
	}

}
