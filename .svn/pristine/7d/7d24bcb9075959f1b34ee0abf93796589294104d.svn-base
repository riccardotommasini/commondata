package org.webdatacommons.structureddata.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.webdatacommons.structureddata.model.Entity;
import org.webdatacommons.structureddata.model.EntityFileLoader;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.util.io.InputUtil;
import ldif.entity.NodeTrait;
import ldif.local.datasources.dump.QuadFileLoader;
import ldif.runtime.Quad;

/**
 * 
 * @author Anna Primpeli
 * 
 */
@Parameters(commandDescription = "Creates subsets of the original dataset, based on a given set of classes.")
public class SubsetCreator extends Processor<File> {

	@Parameter(names = { "-out",
			"-outputDir" }, required = true, description = "Folder where the outputfile(s) are written to.", converter = FileConverter.class)
	private File outputDirectory;

	@Parameter(names = { "-in",
			"-inputDir" }, required = true, description = "Folder where the input is read from.", converter = FileConverter.class)
	private File inputDirectory;

	@Parameter(names = { "-p",
			"-prefix" }, description = "Prefix of files in the input folder which will be processed.")
	private String filePrefix = "";

	@Parameter(names = "-threads", required = true, description = "Number of threads.")
	private Integer threads;

	@Parameter(names = { "-cff",
			"classFilterFile" }, required = true, description = "File containing the class names, class prefixes and number of entities per file.")
	private String classFilterFile = null;

	@Parameter(names = "-sep", required = false, description = "Separator for class filter file. (Default \t)")
	private String sep = "\t";

	@Parameter(names = "-global", required = false, description = "Defines if one global writer per class is used, or mutiple per class per input file per thread. (Defaul: false)")
	private boolean globalWriter = false;

	// NOTE you run into problems, if you cannot write fast enough
	// private Map<String, AsyncEntityWriter> writer = new HashMap<String,
	// AsyncEntityWriter>();
	private Map<String, BufferedWriter> writer = new HashMap<String, BufferedWriter>();

	private Map<String, String> names = new HashMap<String, String>();
	private int errorCount = 0;
	private int parsedLines = 0;

	
	@Override
	protected int getNumberOfThreads() {
		return this.threads;
	}

	@Override
	protected void beforeProcess() {
		try {
			BufferedReader br = InputUtil.getBufferedReader(new File(classFilterFile));
			while (br.ready()) {
				String line = br.readLine();
				String tok[] = line.split(sep);
				// AsyncEntityWriter aWriter = new AsyncEntityWriter(new
				// File(this.outputDirectory, tok[1] + ".gz"));
				// aWriter.open();
				// writer.put(tok[0], aWriter);
				int buffer = 1024 * 8 * 1024;
				if (globalWriter) {
					writer.put(tok[0],
							new BufferedWriter(new OutputStreamWriter(
									new GZIPOutputStream(
											new FileOutputStream(new File(this.outputDirectory, tok[1] + ".gz"))),
									"UTF-8"), buffer));
				} else {
					names.put(tok[0], tok[1]);
				}
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Could not read class filter file");
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	protected List<File> fillListToProcess() {
		List<File> files = new ArrayList<File>();
		Collections.sort(files);
		for (File f : inputDirectory.listFiles()) {
			if (!f.isDirectory()) {
				if (filePrefix.length() > 0) {
					if (!f.getName().startsWith(filePrefix)) {
						continue;
					}
				}
				files.add(f);
			}
		}
		return files;
	}

	@Override
	protected void process(File object) throws Exception {
		Map<String, BufferedWriter> writerLocal = null;
		
		
		if (!globalWriter) {
			writerLocal = new HashMap<String, BufferedWriter>();
			// init thread based writers
			for (String s : this.names.keySet()) {
				writerLocal.put(s,
						new BufferedWriter(new OutputStreamWriter(
								new GZIPOutputStream(new FileOutputStream(new File(this.outputDirectory,
										this.names.get(s) + "_" + object.getName().replace(".gz", "") + ".gz"))),
								"UTF-8")));
			}
		}
		QuadFileLoader qfl = new QuadFileLoader();
		EntityFileLoader etl = new EntityFileLoader();
		BufferedReader br = InputUtil.getBufferedReader(object);
		String currentURL = "";
		NodeTrait currentSubject = null;
		List<Entity> entities = new ArrayList<Entity>();
		List<Quad> quads = new ArrayList<Quad>();
		while (br.ready()) {
			try{
				Quad q = qfl.parseQuadLine(br.readLine());
				parsedLines ++;
				if (q.graph().equals(currentURL)) {
					if (q.subject().equals(currentSubject)) {
						quads.add(q);
					} else {
						// create entity
						if (quads.size() > 0) {
							Entity e = etl.loadEntityFromQuads(quads);
							entities.add(e);
						}
						// clear list
						quads.clear();
						quads.add(q);
						currentSubject = q.subject();
					}
				} else {
					// create entity
					if (quads.size() > 0) {
						Entity e = etl.loadEntityFromQuads(quads);
						entities.add(e);
					}
					quads.clear();
					quads.add(q);
					currentSubject = q.subject();
	
					// create entity
					if (entities.size() > 0) {
						if (!globalWriter) {
							processEntities(entities, writerLocal);
						} else {
							processEntities(entities, writer);
						}
					}
					// clear list
					entities.clear();
					currentURL = q.graph();
				}
			} catch (Exception e) {
				errorCount++;
				// TODO make this an option
				// e.printStackTrace();
			}
		}
		// one final time:
		// create entity
		if (quads.size() > 0) {
			Entity e = etl.loadEntityFromQuads(quads);
			entities.add(e);
		}
		// create entity
		if (entities.size() > 0) {
			if (!globalWriter) {
				processEntities(entities, writerLocal);
			} else {
				processEntities(entities, writer);
			}
		}
		quads = null;
		entities = null;
		br.close();

		if (!globalWriter) {
			for (BufferedWriter w : writerLocal.values()) {				
				w.close();
			}
			writerLocal = null;
		}
	}

	protected void processEntities(List<Entity> entities, Map<String, BufferedWriter> writer) {
		Set<String> types = new HashSet<String>();
		for (Entity e : entities) {
			if (e.getType() != null) {
				if (writer.containsKey(e.getType().value())) {
					types.add(e.getType().value());
				}
			}
		}
		for (String type : types) {
			// writer.get(type).append(entities);
			for (Entity e : entities) {
				try {
					writer.get(type).write(e.toLines());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void afterProcess() {
		if (globalWriter) {
			for (String s : writer.keySet()) {
				try {
					writer.get(s).close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			// we need combine the data
			for (String s : names.values()) {
				try {
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
							new GZIPOutputStream(new FileOutputStream(new File(this.outputDirectory, s + ".gz")))));

					for (File f : outputDirectory.listFiles()) {
						if (!f.isDirectory()) {
							if (filePrefix.length() > 0) {
								if (f.getName().startsWith(s)) {
									BufferedReader br = InputUtil.getBufferedReader(f);
									while (br.ready()) {
										bw.write(br.readLine() + "\n");
									}
									br.close();
									f.delete();
								}
							}
						}
					}
					bw.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Error Lines: "+errorCount);
		System.out.println("Parsed Lines: " + parsedLines);
	}

	public static void main(String[] args) {
		SubsetCreator cal = new SubsetCreator();
		try {
			new JCommander(cal, args);
			cal.process();
		} catch (ParameterException pe) {
			pe.printStackTrace();
			new JCommander(cal).usage();
		}
	}

}
