package org.webdatacommons.structureddata.processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ldif.datasources.dump.parser.ParseException;
import ldif.entity.NodeTrait;
import ldif.local.datasources.dump.QuadFileLoader;
import ldif.runtime.Quad;

import org.webdatacommons.structureddata.model.Entity;
import org.webdatacommons.structureddata.model.EntityFileLoader;

import scala.actors.threadpool.Arrays;
import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;

public abstract class EntityProcessorWithOutput extends Processor<File> {

	protected File folderWithFilesToProcess;
	protected File outputFolder;

	public EntityProcessorWithOutput(String inputFolder, String outputFolder,
			int threads) throws Exception {
		super(threads);
		this.folderWithFilesToProcess = new File(inputFolder);
		if (this.folderWithFilesToProcess == null
				|| this.folderWithFilesToProcess.isFile()) {
			throw new Exception(
					"Folder with files to process is not a folder or null.");
		}
		this.outputFolder = new File(outputFolder);
		if (this.outputFolder == null || this.outputFolder.isFile()) {
			throw new Exception("Output folder is not a folder or null.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<File> fillListToProcess() {
		return Arrays.asList(folderWithFilesToProcess.listFiles());
	}

	@Override
	protected void process(File object) throws Exception {
		QuadFileLoader qfl = new QuadFileLoader();
		EntityFileLoader efl = new EntityFileLoader();
		BufferedReader br = InputUtil.getBufferedReader(object);
		BufferedWriter bw = OutputUtil.getGZIPBufferedWriter(new File(
				outputFolder, object.getName()));
		NodeTrait currentSubject = null;
		List<Quad> quads = new ArrayList<Quad>();
		while (br.ready()) {
			String line = br.readLine();
			if (line == null || line.length() < 1) {
				continue;
			}
			Quad q;
			try {
				q = qfl.parseQuadLine(line);
			} catch (ParseException ex) {
				continue;
			}
			// TODO subject might be the same, whenever those people have add it
			// manually. Think if this is correct.
			if (q.subject().equals(currentSubject)) {
				quads.add(q);
			} else {
				// create entity
				if (quads.size() > 0) {
					Entity e = efl.loadEntityFromQuads(quads);
					processEntity(e, bw);
				}
				// clear list
				quads.clear();
				quads.add(q);
				currentSubject = q.subject();
			}
		}
		// do one final if the end is there
		// create entity
		if (quads.size() > 0) {
			Entity e = efl.loadEntityFromQuads(quads);
			processEntity(e, bw);
		}
		br.close();
		bw.close();
	}

	protected abstract void processEntity(Entity e, BufferedWriter bw);

}
