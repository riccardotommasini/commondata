package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

/**
 * Hadoop InputFormat for NQuads
 * 
 * @author Steffen Stadtmüller (Steffen.Stadtmueller@kit.edu)
 */

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;


public class NQuadInputFormat extends FileInputFormat<Text, Text> {

	
  @Override
  protected boolean isSplitable(JobContext context, Path filename) {
      return false;
  }

  @Override
  public RecordReader<Text, Text> createRecordReader(
    InputSplit split, TaskAttemptContext context) {
      return new NQuadRecordReader();
  }
}