package org.webdatacommons.structureddata.cleanser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ldif.entity.Node;
import ldif.entity.NodeTrait;
import ldif.local.datasources.dump.QuadFileLoader;
import ldif.runtime.Quad;

import org.webdatacommons.structureddata.model.Entity;
import org.webdatacommons.structureddata.model.EntityFileLoader;

import scala.actors.threadpool.Arrays;
import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;

/**
 * * The class removes all syntactical duplicates (entities with exact same tree
 * (incl. values)) globally. Those occur whenever one company has multiple sites
 * using different domains presenting the same entities (e.g. products). It is
 * recommended, to run a duplicate removal per PLD upfront in order to reduce
 * runtime and resource need. Class implements {@link Processor} in order to
 * process Quad files in parallel. Internally the {@link QuadFileLoader} is used
 * to load the quads. Quads are combined into {@link Entity}s. Therefore the
 * quads within the file need to be sorted by the webpage they belong to.
 * 
 * @author Robert Meusel (robert@dwslab.de)
 * 
 */
public class DuplicateRemovalUsingHashOfCanonicalRepresentation extends
		Processor<File> {

	public static void main(String[] args) throws NumberFormatException,
			Exception {
		if (args == null || args.length != 3) {
			System.out
					.println("USAGE: <INPUTFOLDER> <OUTPUTFOLDER> <NUMTHREADS>");
			System.exit(0);
		}
		DuplicateRemovalUsingHashOfCanonicalRepresentation process = new DuplicateRemovalUsingHashOfCanonicalRepresentation(
				args[0], args[1], Integer.parseInt(args[2]));
		process.process();
	}

	protected File folderWithFilesToProcess;
	protected File outputFolder;

	private HashMap<Integer, String> hashSubjectMap = new HashMap<Integer, String>();

	public DuplicateRemovalUsingHashOfCanonicalRepresentation(
			String inputFolder, String outputFolder, int threads)
			throws Exception {
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

		BufferedWriter bw = OutputUtil.getGZIPBufferedWriter(new File(
				this.outputFolder, object.getName()));

		QuadFileLoader qfl = new QuadFileLoader();
		EntityFileLoader etl = new EntityFileLoader();
		BufferedReader br = InputUtil.getBufferedReader(object);
		String currentURL = "";
		NodeTrait currentSubject = null;
		HashMap<String, Entity> entities = new HashMap<String, Entity>();
		HashSet<String> objectEntities = new HashSet<String>();
		List<Quad> quads = new ArrayList<Quad>();
		try {
			while (br.ready()) {
				Quad q = null;
				try {
					q = qfl.parseQuadLine(br.readLine());
				} catch (Exception e) {
					continue;
				}
				if (q.graph().equals(currentURL)) {
					if (q.subject().equals(currentSubject)) {
						quads.add(q);
						if (q.value().isBlankNode()) {
							objectEntities.add(q.value().value());
						}
					} else {
						// create entity
						if (quads.size() > 0) {
							Entity e = etl.loadEntityFromQuads(quads);
							entities.put(e.getSubject().value(), e);
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
						entities.put(e.getSubject().value(), e);
					}
					quads.clear();
					quads.add(q);
					if (q.value().isBlankNode()) {
						objectEntities.add(q.value().value());
					}
					currentSubject = q.subject();

					// create entity
					if (entities.size() > 0) {
						processEntities(entities, objectEntities, currentURL,
								bw);
					}
					// clear list
					entities.clear();
					currentURL = q.graph();
				}
			}
			// one final time:
			// create entity
			if (quads.size() > 0) {
				Entity e = etl.loadEntityFromQuads(quads);
				entities.put(e.getSubject().value(), e);
			}
			// create entity
			if (entities.size() > 0) {
				processEntities(entities, objectEntities, currentURL, bw);
			}

		} catch (EOFException eof) {
			System.out.println("Unexpected end of file: " + object.getName());
		}

		br.close();
		bw.close();
	}

	private void processEntities(HashMap<String, Entity> entities,
			HashSet<String> objectEntities, String url, BufferedWriter writer)
			throws IOException {

		HashSet<String> visitedEntities = new HashSet<String>();

		for (String key : entities.keySet()) {
			// if its a root, as all what is non root is linked
			if (!objectEntities.contains(key)) {
				HashMap<String, Integer> tmpVisitedEntities = new HashMap<String, Integer>();
				String entityString = calculateCanonicalStringOfEntityWithValues(
						key, entities, visitedEntities, tmpVisitedEntities);
				// here we have the entity ready to check.
				String node = addEntity(entityString, entities.get(key)
						.getSubject().value());

				if (node == null) {
					// it does not exist, so we do not need to upodate
					// but to write
					tmpVisitedEntities.put(key, entityString.hashCode());
				}

				for (String entityKey : tmpVisitedEntities.keySet()) {
					Entity e = entities.get(entityKey);
					// set pld
					e.setGraph(tmpVisitedEntities.get(entityKey).toString());
					writer.write(e.toLines());
				}
			}
		}
	}

	private String calculateCanonicalStringOfEntityWithValues(String entityKey,
			HashMap<String, Entity> entities, HashSet<String> visitedEntities,
			HashMap<String, Integer> tmpVisitedEntities) {
		StringBuilder sb = new StringBuilder();
		Entity entity = entities.get(entityKey);
		// in case the entity is not included in the set
		if (entity == null) {
			return "";
		}
		// add the visited one to prevent loops
		visitedEntities.add(entity.getSubject().value());
		// type
		String type = "NULL";
		if (entity.getType() != null) {
			type = entity.getType().value();
		}
		ArrayList<String> propertyValuePairs = new ArrayList<String>();
		for (String property : entity.getProperties().keySet()) {
			HashMap<String, String> replaceMap = new HashMap<String, String>();
			for (NodeTrait value : entity.getProperties().get(property)) {
				String propertyValuePair = property + ":(";
				if (value.isBlankNode()) {
					if (!visitedEntities.contains(value.value())) {
						String entityString = calculateCanonicalStringOfEntityWithValues(
								value.value(), entities, visitedEntities,
								tmpVisitedEntities);
						// here we have the entity ready to check.
						if (entityString.trim().length() < 1) {
							entityString = value.value();
						} else {
							String node = addEntity(entityString, value.value());

							if (node != null) {
								// it exists already, we need to update
								replaceMap.put(value.value(), node);
							} else {
								// it does not exist, so we do not need to
								// update
								// but to write
								tmpVisitedEntities.put(value.value(),
										entityString.hashCode());
							}
						}
						propertyValuePair += entityString;
					}
				} else {
					propertyValuePair += value.value();
				}
				propertyValuePair += ")";
				propertyValuePairs.add(propertyValuePair);
			}
			// replace entity
			for (String node : replaceMap.keySet()) {
				entity.getProperties().get(property).remove(node);
				entity.getProperties()
						.get(property)
						.add(Node.createBlankNode(replaceMap.get(node),
								entity.getGraph()));
			}
		}

		// write output
		sb.append(type);
		sb.append("{");
		// sort for fixed output
		Collections.sort(propertyValuePairs);
		for (String s : propertyValuePairs) {
			sb.append(s);
			sb.append(",");
		}
		sb.append("}");

		return sb.toString();
	}

	/**
	 * This should not be called before {@link #addPLD(String)} was called for
	 * the given pld
	 * 
	 * @param pld
	 * @param string
	 * @param subject
	 * @return
	 */
	private synchronized String addEntity(String entityString, String subject) {
		Integer subjectHash = entityString.hashCode();

		String node = hashSubjectMap.get(subjectHash);
		if (node == null) {
			hashSubjectMap.put(subjectHash, subject);
		}
		return node;
	}

	public static String infusePrefix(String input, String prefix) {
		String output = (prefix.length() > 0 ? prefix + "|" : "")
				+ input.replaceFirst("http://schema.org/", "s:");
		if (output.endsWith("/")) {
			output = output.substring(0, output.length() - 2);
		}
		return output;
	}

	public static String getCanonicalLocalName(String prop, String type,
			String prefix) {
		if (type != null && type.contains("http://schema.org")) {
			return prefix + prop.replace(type, "");
		} else {
			return prefix + prop;
		}
	}

}
