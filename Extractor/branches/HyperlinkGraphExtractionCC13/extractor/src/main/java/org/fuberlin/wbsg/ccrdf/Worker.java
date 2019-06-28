package org.fuberlin.wbsg.ccrdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.jets3t.service.model.S3Object;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

/**
 * Worker node implementation, connects to queue, takes file name to process,
 * loads file, extracts rdf, and writes extracted n-quads and statistics back to
 * s3. If anything goes wrong, the message is not removed from the queue and
 * another node can have a shot.
 * 
 * @author Hannes Muehleisen (hannes@muehleisen.org)
 */
public class Worker extends ProcessingNode {
	private static Logger log = Logger.getLogger(Worker.class);

	// TODO include configuration reader with typed getters
	// hard configuration, unchanged for all threads
	private final String dataBucket = getOrCry("dataBucket");
	private final String resultBucket = getOrCry("resultBucket");
	private final int retryLimit = Integer.parseInt(getOrCry("jobRetryLimit"));

	private StatHandler dataStatHandler = null;
	private StatHandler errorStatHandler = null;

	public static class WorkerThread extends Thread {
		private Timer timer = new Timer();
		int timeLimit = 0;

		public WorkerThread() {
		}

		public WorkerThread(int timeLimitMsec) {
			this.timeLimit = timeLimitMsec;
		}

		public void run() {
			Worker worker = new Worker();
			if (timeLimit < 1) {
				timeLimit = Integer.parseInt(worker.getOrCry("jobTimeLimit")) * 1000;
			}
			while (true) {
				// cancel all old timers
				boolean success = false;
				timer = new Timer();
				// set a new timer killing us after the specified time limit
				// FIXME 
				final WorkerThread t = this;
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						log.warn("Killing worker thread, timeout expired.");
						t.interrupt();
					}
				}, timeLimit);
                //TODO create the and handle the queue here
				success = worker.getTaskAndProcess();

				// on failures sleep a bit
				timer.cancel();
				if (!success) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						log.warn("Interrupted", e);
					}
				}
			}
		}
	}

	public boolean getTaskAndProcess() {
		File tempInputFile = null;
		File unpackedFile = null;
		File tempOutputFile = null;
		String inputFileKey = "";
		CSVStatHandler pageStatHandler = null;
        boolean messageIsDeleted = false;


        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
                getQueueUrl())
                .withAttributeNames("ApproximateReceiveCount");

		try {
			// receive task message from queue

			receiveMessageRequest.setMaxNumberOfMessages(1);
			ReceiveMessageResult queueRes = getQueue().receiveMessage(
					receiveMessageRequest);
			if (queueRes.getMessages().size() < 1) {
				log.warn("Queue is empty");
				return false;
			}
			Message jobMessage = queueRes.getMessages().get(0);

			/**
			 * messages which went back to the queue more than the amount of
			 * times defined in the configuration entry "jobRetryLimit" are
			 * discarded, probably contain nasty data we cannot parse.
			 */

			if (Integer.parseInt(jobMessage.getAttributes().get(
					"ApproximateReceiveCount")) > retryLimit) {
				log.warn("Discarding message " + jobMessage.getBody());
				getQueue().deleteMessage(
						new DeleteMessageRequest(getQueueUrl(), jobMessage
								.getReceiptHandle()));

				// store this information in sdb
				Map<String, String> statData = new HashMap<String, String>();
				statData.put("message", "Message Discarded");

				try {
					statData.put("node", InetAddress.getLocalHost()
							.getHostName());
				} catch (UnknownHostException e1) {
					// ignore
				}
				statData.put("file", jobMessage.getBody());
				statData.put("datetime", Calendar.getInstance().getTime()
						.toString());

				getErrorStatHandler().addStats(UUID.randomUUID().toString(),
						statData);
				getErrorStatHandler().flush();

				return false;
			}

			/**
			 * retrieve data file from s3, and unpack it using gzip
			 */
			inputFileKey = jobMessage.getBody();
			log.info("Now working on " + inputFileKey);

			/**
			 * get file from s3 and process with zipped arc.
			 */
			S3Object inputObject = getStorage().getObject(dataBucket,
					inputFileKey);

			ReadableByteChannel gzippedArcFileBC = Channels
					.newChannel(inputObject.getDataInputStream());

			// tempInputFile = File.createTempFile("data", ".arc.gz");
			// storeStreamToFile(inputObject.getDataInputStream(),
			// tempInputFile);
			// inputObject.closeDataInputStream();
			//
			// Process p = Runtime.getRuntime().exec(
			// new String[] { "gunzip", tempInputFile.toString() });
			// p.waitFor();
			// try {
			// p.getErrorStream().close();
			// p.getInputStream().close();
			// p.getOutputStream().close();
			// } catch (Exception e1) {
			// log.warn("Process handling error", e1);
			// }
			// p.destroy();
			//
			// unpackedFile = new File(tempInputFile.toString().replace(".gz",
			// ""));
			// if (!unpackedFile.exists()) {
			// log.warn("unable to find " + unpackedFile);
			// throw new RuntimeException("Unable to unpack " + tempInputFile);
			// }
			// Reader arcFileReader = new InputStreamReader(new FileInputStream(
			// unpackedFile), "ASCII");

			/**
			 * Prepare temporary local output files
			 */
			tempOutputFile = File.createTempFile("ccrdf-extraction", ".nq.gz");
			tempOutputFile.deleteOnExit();
			String outputFileKey = "data/ex_" + inputFileKey.replace("/", "_")
					+ ".nq.gz";

			String outputStatsKey = "stats/ex_"
					+ inputFileKey.replace("/", "_") + ".csv.gz";

			pageStatHandler = new CSVStatHandler();

			OutputStream tempOutputStream = new GZIPOutputStream(
					new FileOutputStream(tempOutputFile));
			RDFExtractor extractor = new RDFExtractor(tempOutputStream);

			/**
			 * Read all page entries from file and run extractor on them
			 */

			// default is false
			boolean logRegexError = Boolean
					.parseBoolean(getOrCry("logRegexFailures"));

            //deprecated
			//ArcProcessor processor = new ArcProcessor();

            WarcProcessor processor = new WarcProcessor();
			Map<String, String> dataStats = processor.processArcData(
					gzippedArcFileBC, inputFileKey, extractor, pageStatHandler,
					logRegexError);

			// UltraFailArcProcessor processor = new UltraFailArcProcessor();
			// Map<String, String> dataStats = processor.processUnpackedArcData(
			// arcFileReader, inputFileKey, extractor, pageStatHandler);

			// not needed, close() flushes.
			tempOutputStream.flush();
			tempOutputStream.close();

			/**
			 * Create overall statistics for this data file
			 */
			dataStats
					.put("size", Long.toString(inputObject.getContentLength()));
			getDataStatHandler().addStats(inputFileKey, dataStats);

			/**
			 * force statistics being persisted
			 */
			getDataStatHandler().flush();
			pageStatHandler.flush();

			/**
			 * write extraction results to s3, if at least one included item was
			 * guessed to include triples
			 */
			String triplesStr = dataStats
					.get(WarcProcessor.PAGES_GUESSED_TRIPLES);
			if (triplesStr != null && Long.parseLong(triplesStr) > 0) {
				S3Object dataFileObject = new S3Object(tempOutputFile);
				dataFileObject.setKey(outputFileKey);
				getStorage().putObject(resultBucket, dataFileObject);

				S3Object statsFileObject = new S3Object(
						pageStatHandler.getFile());
				statsFileObject.setKey(outputStatsKey);
				getStorage().putObject(resultBucket, statsFileObject);
			}

			/**
			 * remove message from queue. If an Exception is thrown or the node
			 * dies before finishing its task, this does not occur and the
			 * message is re-queued for another node
			 */
			getQueue().deleteMessage(
					new DeleteMessageRequest(getQueueUrl(), jobMessage
							.getReceiptHandle()));
            messageIsDeleted = true;
			log.debug("Finished processing file " + inputFileKey);

			return true;

		} catch (Exception e) {
			log.warn("Unable to finish processing ("
					+ e.getClass().getSimpleName() + ": " + e.getMessage()
					+ ")");
			log.debug("Stracktrace", e.fillInStackTrace());

			// put error information into sdb for later analyis
			Map<String, String> statData = new HashMap<String, String>();
			statData.put("exception", e.getClass().getSimpleName());
			String message = e.getMessage();
			if (message == null) {
				message = e.getClass().getName();
			}
			statData.put("message", message);
			String st = getStackTrace(e);
			statData.put("stacktrace",
					st.substring(0, Math.min(1024, st.length())));

			try {
				statData.put("node", InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException e1) {
				// ignore
			}
			statData.put("file", inputFileKey);
			statData.put("datetime", Calendar.getInstance().getTime()
					.toString());

			getErrorStatHandler().addStats(UUID.randomUUID().toString(),
					statData);
			getErrorStatHandler().flush();

		} finally {

			if (tempInputFile != null && tempInputFile.exists()) {
				tempInputFile.delete();
			}
			if (unpackedFile != null && unpackedFile.exists()) {
				unpackedFile.delete();
			}
			if (tempOutputFile != null && tempOutputFile.exists()) {
				tempOutputFile.delete();
			}

			if (pageStatHandler != null) {
				File statFile = pageStatHandler.getFile();
				if (statFile != null && statFile.exists()) {
					statFile.delete();
				}
			}
            if(!messageIsDeleted){
                receiveMessageRequest.setVisibilityTimeout(0);
            }
		}
		return false;
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

	public void setDataStatHandler(StatHandler h) {
		dataStatHandler = h;
	}

	public void setErrorStatHandler(StatHandler h) {
		errorStatHandler = h;
	}

	public static class CSVStatHandler implements StatHandler {

		private File csvFile;

		public CSVStatHandler() {
			try {
				csvFile = File.createTempFile("stats", "csv.gz");
			} catch (IOException e) {
				log.warn(e);
			}
		}

		@Override
		public void addStats(String key, Map<String, String> data) {
			Map<String, Object> writeMap = new HashMap<String, Object>();
			writeMap.putAll(data);
			CSVExport.writeToFile(writeMap, csvFile);
		}

		@Override
		public void flush() {
			CSVExport.closeWriter(csvFile);
		}

		public File getFile() {
			return csvFile;
		}

	}

	public StatHandler getDataStatHandler() {
		if (dataStatHandler == null) {
			dataStatHandler = new AmazonStatHandler(getDbClient(),
					getOrCry("sdbdatadomain"));
		}
		return dataStatHandler;
	}

	public StatHandler getErrorStatHandler() {
		if (errorStatHandler == null) {
			errorStatHandler = new AmazonStatHandler(getDbClient(),
					getOrCry("sdberrordomain"));
		}
		return errorStatHandler;
	}

	private static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	public static class ThreadGuard extends Thread {
		private List<Thread> threads = new ArrayList<Thread>();
        //can set thread limit to one for debugging
		private int threadLimit = 1;//Runtime.getRuntime().availableProcessors();
		private int threadSerial = 0;
		private int waitTimeSeconds = 1;

		private Class<? extends Thread> threadClass;

		public ThreadGuard(Class<? extends Thread> threadClass) {
			this.threadClass = threadClass;
		}

		public void run() {
			while (true) {
				List<Thread> threadsCopy = new ArrayList<Thread>(threads);
				for (Thread t : threadsCopy) {
					if (!t.isAlive()) {
						log.warn("Thread " + t.getName() + " died.");
						threads.remove(t);
					}
				}
				while (threads.size() < threadLimit) {
					Thread newThread;
					try {
						newThread = threadClass.newInstance();
						newThread.setName("#" + threadSerial);
						threads.add(newThread);
						newThread.start();
						log.info("Started new WorkerThread, "
								+ newThread.getName());
						threadSerial++;
					} catch (Exception e) {
						log.warn("Failed to start new Thread of class "
								+ threadClass);
					}

				}
				try {
					Thread.sleep(waitTimeSeconds * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		new ThreadGuard(WorkerThread.class).start();
	}

}
