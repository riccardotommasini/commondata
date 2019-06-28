package org.webdatacommons.webgraph.extraction.sf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.webdatacommons.webgraph.extraction.sf.model.Content;
import org.webdatacommons.webgraph.extraction.sf.model.Link;
import org.webdatacommons.webgraph.extraction.sf.model.Location;
import org.webdatacommons.webgraph.extraction.sf.model.MetaDataReader;
import org.webdatacommons.webgraph.extraction.sf.model.Metadata;
import org.webdatacommons.webgraph.extraction.sf.model.MetadataSmall;
import org.webdatacommons.webgraph.extraction.sf.model.RedirectSmall;


public class Main {

	private final static String ENTRY_PARTITIONER = "\n#######NEW ENTRY#######\n";

	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 5){
			System.out.println("USAGE: Main.java <Queue Name> <Queue Endpoint> <OutputDir> <AWS_KEY> <AWS_SECURE_KEY> <OUTPUT-BUCKET> <THREADS>");
			return;
		}
		int thread = 0;
		if (args.length > 6){
			thread = Integer.parseInt(args[6]);
		}
		
		String queueName, queueEndpoint, outputDir, awsKey, awsSecret, outputBucket;
		queueName = args[0];
		queueEndpoint = args[1];
		outputDir = args[2];
		awsKey = args[3];
		awsSecret = args[4];
		outputBucket = args[5];
		
		RedirectProcessor p = new RedirectProcessor(thread,
				queueName, queueEndpoint, outputDir,
				"aws-publicdatasets", 
				awsKey, awsSecret, outputBucket);
		
		p.process();
		System.out.println("Done.");

	}

	public static ArrayList<String> loadUrlList(String fileName)
			throws IOException {
		System.out.println(new Date() + " " + "Start reading ...");
		File f = new File(fileName);

		BufferedReader reader = new BufferedReader(new FileReader(f));

		ArrayList<String> result = new ArrayList<String>();
		String line;

		while ((line = reader.readLine()) != null) {
			Location l = MetaDataReader.readLocation(line);
			result.add(l.url);
		}
		reader.close();
		return result;
	}

	public static void transformSequenceFileToPlainText(String fileName)
			throws Exception {

		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			System.out.println("File does not exists or is a directory.");
			return;
		}
		System.out.println(new Date() + " " + "Start reading ...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName
				+ ".txt")));
		SequenceFileReader reader = new SequenceFileReader(
				file.getAbsolutePath());
		long cnt = 0;
		while (reader.next()) {
			cnt++;
			bw.write(ENTRY_PARTITIONER);
			bw.write(reader.getKey() + "\n" + reader.getValue() + "\n");
		}
		bw.close();
		System.out.println(new Date() + " " + "Done reading.");
		System.out.println("Read " + cnt + " Key - Value - Pairs.");
	}

	public static void extractRedirectsFromSequencesFiles(String fileName)
			throws Exception {
		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			System.out.println("File does not exists or is a directory.");
			return;
		}
		System.out.println(new Date() + " " + "Start reading ...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName
				+ "_redir.txt")));
		SequenceFileReader reader = new SequenceFileReader(
				file.getAbsolutePath());
		long cnt = 0;
		long fourofour = 0;
		long redircnt = 0;
		StringBuilder sb = new StringBuilder();
		while (reader.next()) {
			//Metadata data = MetaDataReader.read(reader.getValue());
			MetadataSmall data = MetaDataReader.readSmall(reader.getValue());
			if (data.disposition.equals("SUCCESS")) {
				cnt++;
				//Redirect redir = data.redirect_from;
				RedirectSmall redir = data.redirect_from;
				if (redir != null) {
					sb.append(reader.getKey());
					sb.append("\t");
					sb.append(redir.source_url);
					sb.append("\t");
					sb.append(redir.http_result);
					sb.append("\n");
					bw.write(sb.toString());
					sb.setLength(0);
//					bw.write(reader.getKey() + "\t" + redir.source_url + "\t"
//							+ redir.http_result + "\n");
					redircnt++;
				}

				if (data.http_result != null && data.http_result.equals("404")) {
					fourofour++;
				}
			}

		}
		bw.close();
		System.out.println(new Date() + " " + "Done reading.");
		System.out.println("Read " + cnt + " Key - Value - Pairs.");
		System.out.println("Found " + redircnt + " redirects.");
		System.out.println("Found " + fourofour + " 404.");
	}

	public static void extractLinksFromSequenceFiles(String fileName)
			throws Exception {

		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			System.out.println("File does not exists or is a directory.");
			return;
		}
		System.out.println(new Date() + " " + "Start reading ...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName
				+ "_links.txt")));
		SequenceFileReader reader = new SequenceFileReader(
				file.getAbsolutePath());
		long cnt = 0;
		while (reader.next()) {
			cnt++;
			bw.write(ENTRY_PARTITIONER);
			bw.write(reader.getKey() + "\n");
			Metadata data = MetaDataReader.read(reader.getValue());
			Content c = data.content;
			if (c != null && c.links != null) {
				for (Link l : data.content.links) {
					bw.write(l.type + "\t" + l.href + "\n");
				}
			}
		}
		bw.close();
		System.out.println(new Date() + " " + "Done reading.");
		System.out.println("Read " + cnt + " Key - Value - Pairs.");
	}

}
