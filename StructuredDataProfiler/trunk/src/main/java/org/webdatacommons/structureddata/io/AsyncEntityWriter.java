package org.webdatacommons.structureddata.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.webdatacommons.structureddata.model.Entity;

/**
 * Based on Stackoverflow:
 * (http://stackoverflow.com/questions/6206472/what-is-the-best-way-to-write-to-
 * a-file-in-a-parallel-thread-in-java)
 * 
 * @author Robert Meusel (robert@dwslab.de)
 *
 */
public class AsyncEntityWriter implements EntityWriter, Runnable {

	// private final File file;
	private final Writer out;
	private final BlockingQueue<Entity> queue = new LinkedBlockingQueue<Entity>();
	private volatile boolean started = false;
	private volatile boolean stopped = false;

	public AsyncEntityWriter(File file) throws IOException {
		// this.file = file;
//		this.out = OutputUtil.getGZIPBufferedWriter(file);
		int buffer = 1024 * 8 * 1024;
		this.out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(
				new FileOutputStream(file)), "UTF-8"), buffer); 
	}

	public EntityWriter append(Entity e) {
		if (!started) {
			throw new IllegalStateException("open() call expected before append()");
		}
		try {
			queue.put(e);
		} catch (InterruptedException ignored) {
		}
		return this;
	}

	@Override
	public EntityWriter append(Collection<Entity> es) {
		if (!started) {
			throw new IllegalStateException("open() call expected before append()");
		}
		try {
			for (Entity e : es) {
				queue.put(e);
			}
		} catch (InterruptedException ignored) {
		}
		return this;
	}

	public void open() {
		this.started = true;
		new Thread(this).start();
	}

	public void run() {
		while (!stopped) {
			try {
				Entity e = queue.poll(100, TimeUnit.MICROSECONDS);
				if (e != null) {
					try {
						out.write(e.toLines());
					} catch (IOException logme) {
					}
				}
			} catch (InterruptedException e) {
			}
		}
		try {
			out.close();
		} catch (IOException ignore) {
		}
	}

	public void close() {
		this.stopped = true;
	}

}