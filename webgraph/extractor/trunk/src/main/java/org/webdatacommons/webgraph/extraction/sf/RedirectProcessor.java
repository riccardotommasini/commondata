package org.webdatacommons.webgraph.extraction.sf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.webdatacommons.webgraph.extraction.sf.model.Link;
import org.webdatacommons.webgraph.extraction.sf.model.MetaDataReader;
import org.webdatacommons.webgraph.extraction.sf.model.Metadata;
import org.webdatacommons.webgraph.extraction.sf.model.Redirect;
import org.webdatacommons.webgraph.extraction.sf.model.SequenceFileStatistics;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;

public class RedirectProcessor {

	private static Logger log = Logger.getLogger("RedirectProcessor.class");
	private int threads;
	private String inputFileOrDir;
	private File statFile;
	private List<SequenceFileStatistics> stats = new ArrayList<SequenceFileStatistics>();
	private RestS3Service s3;
	private AWSCredentials awsCreds;
	private String awsKey;
	private String awsSecret;
	private S3Bucket s3bucket;
	private String outputS3BucketName;
	private boolean s3Mode = false;
	private File outputDir;
	private String queueName;
	private String queueEndpoint;
	private boolean isSingleFile=false;

	public RedirectProcessor(int threads, String inputFileOrFileListOrDir,
			String outputDir) {
		if (threads < 0) {
			System.out
					.println("Number of threads invalid. Must be 0 or larger.");
			System.exit(0);
		}
		if (threads == 0) {
			this.threads = Runtime.getRuntime().availableProcessors();
		} else {
			this.threads = threads;
		}
		File fout = new File(outputDir);
		if (!fout.exists() || !fout.isDirectory()) {
			System.out.println("Output dir does not exist or is no dir.");
			System.exit(0);
		} else {
			this.outputDir = fout;
		}

		File f = new File(inputFileOrFileListOrDir);
		if (!f.exists()) {
			System.out.println("Input does not exist.");
			System.exit(0);
		} else {
			if(f.isFile())
				this.isSingleFile=true;
			this.inputFileOrDir = inputFileOrFileListOrDir;
			this.statFile = new File(outputDir, "stats");
		}

	}

	/*
	 * 
	 * Uses s3OutputBucket to store output files and the file list. The initial
	 * file list (inputFileList) must also be stored in this Bucket After
	 * processing a file, the inputFileList is changed to only include not
	 * (completely) processed files
	 */
	public RedirectProcessor(int threads, String queueName,
			String queueEndpoint, String outputDir, String s3BucketName,
			String key, String secKey, String s3OutputBucket) {
		s3Mode = true;
		awsKey = key;
		awsSecret = secKey;
		this.queueName = queueName;
		this.queueEndpoint = queueEndpoint;
		if (threads < 0) {
			System.out
					.println("Number of threads invalid. Must be 0 or larger.");
			System.exit(0);
		}
		if (threads == 0) {
			this.threads = Runtime.getRuntime().availableProcessors();
		} else {
			this.threads = threads;
		}

		File fout = new File(outputDir);
		if (!fout.exists() || !fout.isDirectory()) {
			System.out.println("Output dir does not exist or is no dir.");
			System.exit(0);
		} else {
			this.outputDir = fout;
		}

		outputS3BucketName = s3OutputBucket;
		s3bucket = new S3Bucket(s3BucketName);
		awsCreds = new AWSCredentials(key, secKey);
		// File f = new File(inputFileList);
		/*
		 * if (!f.exists() || f.isDirectory() ||
		 * !inputFileList.endsWith(".list")) { System.out .println(
		 * "Input does not exist or is a directory or not a list file where only a list file is allowed."
		 * ); System.exit(0); } else {
		 */
		// this.inputFileOrDir = inputFileList;
		/*
		 * if (f.isDirectory()) { this.statFile = new File(f, "stats"); } else {
		 * this.statFile = new File(f.getParentFile(), "stats"); }
		 */
		this.statFile = new File(outputDir, "stats");
		// }

	}

	public void process() throws IOException {
		System.out.println("Starting ...");
		long startTime = new Date().getTime();
		log.log(Level.INFO, new Date() + " " + "Starting.");

		/*
		 * System.out.println("Getting files to process..."); File list = new
		 * File(outputDir, inputFileOrDir);
		 * LoadFileFromS3(list.getAbsolutePath(), inputFileOrDir,
		 * outputS3BucketName); List<String> filesToProcess = InputUtil
		 * .getFileReferenceList(list.getAbsolutePath());
		 * log.info(filesToProcess.size() + " files to process.");
		 * ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
		 * .newFixedThreadPool(threads);
		 */

		Thread[] t = new Thread[threads];

		for (int i = 0; i < threads; i++) {
			t[i] = new Thread(new Worker());
			t[i].start();
		}

		for (int i = 0; i < threads; i++) {
			try {
				t[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/*
		 * ArrayList<Worker> workers = new ArrayList<Worker>(); for (String f :
		 * filesToProcess) { Worker w = new Worker(f); workers.add(w);
		 * executor.submit(w); }
		 */
		/*
		 * long stillTodo = printState(executor, startTime); long last; while
		 * (stillTodo != 0) { try { Thread.sleep(10000); } catch
		 * (InterruptedException e) { throw new RuntimeException(e); } last =
		 * stillTodo; stillTodo = printState(executor, startTime);
		 * 
		 * if(stillTodo!=last) { SaveFilesToProcess(workers, outputS3BucketName,
		 * inputFileOrDir); } }
		 * 
		 * executor.shutdown();
		 */
		System.out.println("Writing statistics...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(statFile));
		bw.write(SequenceFileStatistics.HEADER + "\n");
		for (SequenceFileStatistics sfs : stats) {
			bw.write(sfs.toString() + "\n");
		}
		bw.close();
		log.log(Level.INFO, new Date() + " " + "Done.");
		System.out.println("Done.");
	}

	private long printState(ThreadPoolExecutor executor, long startTime) {
		int activeThreadNum = executor.getActiveCount();
		long total = executor.getTaskCount();
		long finished = executor.getCompletedTaskCount();
		long runtime = (System.currentTimeMillis() - startTime) / 1000;
		System.out
				.printf("Runtime: %ds --> Total: %d, Done: %d, %ss / item, Finished in: %ds \n, Threads alive: %d",
						runtime,
						total,
						finished,
						String.format("%.2f", ((float) runtime) / finished),
						(int) ((float) runtime / finished) * (total - finished),
						activeThreadNum);

		return total - finished;
	}

	private class Worker implements Runnable {

		public boolean finished = false;
		private QueueManager qm;

		public Worker() {
			if(!isSingleFile)
				qm = new QueueManager(awsKey, awsSecret, queueEndpoint, queueName);
		}

		public void run() {
			finished = false;
			
			if(isSingleFile)
			{
				File f = new File(inputFileOrDir);
				try {
					processFile(f, f.getName());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finished=true;
			}
			else
			{
				String fileName = qm.nextFile();
	
				while (fileName != null && !fileName.isEmpty()) {
					log.log(Level.INFO, new Date() + " " + "Start processing "
							+ fileName);
					try {
						File file;
						// check mode
						if (s3Mode) {
							// download first
							// TODO check if an ending is necessary
							// extract filename
							String s = "s3://" + s3bucket.getName() + "/";
							if (fileName.contains(s)) {
								fileName = fileName.substring(fileName.indexOf(s)
										+ s.length());
								System.out.println("s3 file name is " + fileName);
							}
	
							file = new File(outputDir, "tmp_"
									+ fileName.replace("/", "_"));
							S3Object inputObject = getStorage().getObject(s3bucket,
									fileName);
	
							System.out.println(Thread.currentThread().getName()
									+ ": Downloading file " + file.getName());
							storeStreamToFile(inputObject.getDataInputStream(),
									file);
							inputObject.closeDataInputStream();
						} else {
							file = new File(fileName);
						}
						if (!file.exists() || !file.isFile()) {
							System.out
									.println("File does not exists or is a directory.");
							return;
						}
	
						processFile(file, fileName);
	
						qm.setFileProcessed();
					} catch (IOException e) {
						log.log(Level.WARNING, "Could not process file " + fileName);
						// TODO think about something clever to come up here - at
						// least log the error somewhere
						e.printStackTrace();
	
						// just try again next time ...
						// finished = false;
					} catch (Exception e) {
						e.printStackTrace();
					}
					fileName = qm.nextFile();
				}// while
			}
		}

		private void processFile(File file, String fileName) throws Exception {
			System.out.println(Thread.currentThread().getName() + ": "
					+ new Date() + " " + "Start reading " + file.getName());
			File outputFile = new File(outputDir, fileName.replace("/", "_")
					+ "_redir.gz");
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new GZIPOutputStream(new FileOutputStream(outputFile))));
			SequenceFileReader reader = new SequenceFileReader(
					file.getAbsolutePath());

			int cnt = 0;
			int fourofour = 0;
			int crossRedir = 0;
			int redircnt = 0;

			try {
				StringBuilder sb = new StringBuilder();
				while (reader.next()) {
					//MetadataSmall data = MetaDataReader.readSmall(reader.getValue());
					Metadata data = MetaDataReader.read(reader.getValue());
					if (data.disposition.equals("SUCCESS")
							&& data.mime_type != null
							&& data.mime_type.equals("text/html")) {
						cnt++;
						
						if(data.content != null && data.content.links != null)
						{
							// Process links
							Link[] links = data.content.links;
							
							for(Link l : links)
							{
								sb.append(DomainUtil.compress(reader.getKey()));
								sb.append("\t");								
								sb.append(DomainUtil.compress(l.href));
								sb.append("\t");
								sb.append(data.http_result);
								sb.append("\n");
								bw.write(sb.toString());
								sb.setLength(0);
							}
						}
						
						// Process redirect
						//RedirectSmall redir = data.redirect_from;
						Redirect redir = data.redirect_from;
						if (redir != null) {
							sb.append(DomainUtil.compress(redir.source_url));
							sb.append("\t");
							sb.append(DomainUtil.compress(reader.getKey()));
							sb.append("\t");
							sb.append(redir.http_result);
							sb.append("\n");
							bw.write(sb.toString());
							sb.setLength(0);
							// bw.write(reader.getKey() + "\t" +
							// redir.source_url
							// + "\t" + redir.http_result + "\n");
							if (DomainUtil.getPayLevelDomainFromWholeURL(reader
									.getKey()) != DomainUtil.INVALID_URL
									&& DomainUtil
											.getPayLevelDomainFromWholeURL(redir.source_url) != DomainUtil.INVALID_URL
									&& !DomainUtil
											.getPayLevelDomainFromWholeURL(
													reader.getKey())
											.equals(DomainUtil
													.getPayLevelDomainFromWholeURL(redir.source_url))) {
								crossRedir++;
							}

							redircnt++;
						}
						if (data.http_result != null
								&& data.http_result.equals("404")) {
							fourofour++;
						}
					}
				}
			} catch (Exception ex) {
				System.out.println(Thread.currentThread().getName()
						+ ": Error processing " + file.getName());
				ex.printStackTrace();
				throw ex;
			} finally {
				reader.close();
				bw.close();
			}

			if (s3Mode) {
				// upload file to S3
				SaveFileToS3(outputFile.getAbsolutePath(),
						outputFile.getName(), outputS3BucketName);

				// destroy tmp file
				if (!file.delete()) {
					System.out.println("Could not delete file. "
							+ file.getAbsolutePath());
				}
			}
			stats.add(new SequenceFileStatistics(file.getAbsolutePath(),
					outputFile.getAbsolutePath(), cnt, redircnt, crossRedir,
					fourofour));
		}
	}

	public void storeStreamToFile(InputStream in, File outFile)
			throws IOException {
		OutputStream out = new FileOutputStream(outFile);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		out.close();
	}

	protected RestS3Service getStorage() {
		if (s3 == null) {
			try {
				s3 = new RestS3Service(awsCreds);
			} catch (S3ServiceException e1) {
				log.warning("Unable to connect to S3");
			}
		}
		return s3;
	}

	protected void SaveFileToS3(String localFile, String S3FileKey,
			String S3Bucket) {
		S3Object dataFileObject = new S3Object(localFile);
		dataFileObject.setKey(S3FileKey);
		dataFileObject.setDataInputFile(new File(localFile));
		try {
			getStorage().putObject(S3Bucket, dataFileObject);
			dataFileObject.closeDataInputStream();
		} catch (S3ServiceException e) {
			log.warning("Error saving output to S3: " + localFile);
			e.printStackTrace();
		} catch (IOException e) {
			log.warning("Error saving output to S3: " + localFile);
			e.printStackTrace();
		}
	}

	protected void LoadFileFromS3(String localFile, String S3FileKey,
			String S3Bucket) {
		File file = new File(localFile);
		S3Object inputObject;
		try {
			inputObject = getStorage().getObject(new S3Bucket(S3Bucket),
					S3FileKey);
			storeStreamToFile(inputObject.getDataInputStream(), file);
			inputObject.closeDataInputStream();
		} catch (S3ServiceException e) {
			e.printStackTrace();
			log.warning("Unable to load file from S3: " + S3Bucket + "/"
					+ S3FileKey);
		} catch (IOException e) {
			e.printStackTrace();
			log.warning("Unable to save local file: " + localFile);
		} catch (ServiceException e) {
			e.printStackTrace();
			log.warning("Unable to load file from S3: " + S3Bucket + "/"
					+ S3FileKey);
		}
	}
}
