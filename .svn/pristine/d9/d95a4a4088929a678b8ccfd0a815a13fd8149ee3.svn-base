package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;



import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Runner extends Configured implements Tool{
	
	public int run(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		
		conf.set("mapred.task.timeout", "7200000"); //timeout 2h

	    Path inputPath = new Path(args[0]);
	    Path outputPath = new Path(args[1]);
	    int c = Integer.valueOf(args[2]);

	    conf.set("edu.kit.aifb.lookingfor", args[3].toLowerCase());
	    
	    Job job = new Job(conf, "CSVGenerationJob");
	    job.setJarByClass(Runner.class);
	    
	    job.setInputFormatClass(NQuadInputFormat.class);
	    FileInputFormat.setInputPaths(job, inputPath);
	    FileOutputFormat.setOutputPath(job, outputPath);

	    job.setMapperClass(CSVGeneratorMapper.class);
	    job.setReducerClass(CSVGeneratorReducer.class);
	    
	    job.setNumReduceTasks(c);
	   
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
	    
	    job.waitForCompletion(true);
	 
		return 0;
	}
	
	/**
	 * Call with [inputpath] [outputpath] [Reducernumber] [FormatToExtract]
	 */
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Runner(), args);
		System.exit(res);
	}
	
	
	
}
