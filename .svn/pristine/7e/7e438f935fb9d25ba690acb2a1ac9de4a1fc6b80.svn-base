package org.webdatacommons.structureddata.cleanser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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

import com.google.common.net.InternetDomainName;

import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;

/**
 * The class removes all syntactical duplicates (entities with exact same tree (incl. values))
 * within a website (PLD). Those occur whenever the site uses structured data
 * within the header/footer and the same real-world entity is extracted multiple times.
 * Class implements {@link Processor} in order to process Quad files in parallel.
 * Internally the  {@link QuadFileLoader} is used to load the quads.
 * Quads are combined into {@link Entity}s. Therefore the quads within the file need
 * to be sorted by the webpage they belong to. In addition the class allows
 * the RDF-normalization, which is not given by Any23.
 * 
 * @author Robert Meusel (robert@dwslab.de)
 * 
 */
public class DuplicateRemovalPerSiteUsingHashOfCanonicalRepresentation extends
		Processor<File> {

	public static void main(String[] args) throws NumberFormatException,
			Exception {
		if (args == null || args.length != 4) {
			System.out
					.println("USAGE: <INPUTFOLDER> <OUTPUTFOLDER> <RDFNORMALIZE> <NUMTHREADS>");
			System.exit(0);
		}
		DuplicateRemovalPerSiteUsingHashOfCanonicalRepresentation process = new DuplicateRemovalPerSiteUsingHashOfCanonicalRepresentation(
				args[0], args[1], Boolean.parseBoolean(args[2]),
				Integer.parseInt(args[3]));
		process.process();
	}

	protected File folderWithFilesToProcess;
	protected File outputFolder;
	protected boolean normalizeRDF = false;

	private HashMap<String, HashMap<Integer, String>> pldHashMap = new HashMap<String, HashMap<Integer, String>>();

	public DuplicateRemovalPerSiteUsingHashOfCanonicalRepresentation(String inputFolder,
			String outputFolder, boolean normalizeRDF, int threads)
			throws Exception {
		super(threads);
		this.normalizeRDF = normalizeRDF;
		if (normalizeRDF) {
			System.out.println("Running with RDF fixing");
		}
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
		while (br.ready()) {
			Quad q = qfl.parseQuadLine(br.readLine());
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
						if (normalizeRDF) {
							normalizeRDF(e);
						}
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
					if (normalizeRDF) {
						normalizeRDF(e);
					}
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
					processEntities(entities, objectEntities, currentURL, bw);
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
			if (normalizeRDF) {
				normalizeRDF(e);
			}
			entities.put(e.getSubject().value(), e);
		}
		// create entity
		if (entities.size() > 0) {
			processEntities(entities, objectEntities, currentURL, bw);
		}

		br.close();
		bw.close();
	}

	private void normalizeRDF(Entity e) {

		for (String s : e.getProperties().keySet()) {

			List<NodeTrait> vals = e.getProperties().get(s);
			if (vals.size() > 1) {
				HashSet<NodeTrait> values = new HashSet<NodeTrait>();
				values.addAll(vals);
				if (vals.size() != values.size()) {

					try {
						e.setProperty(s, new ArrayList<NodeTrait>(values));
					} catch (Exception e1) {

						e1.printStackTrace();
					}
				}
			}
		}

	}

	private void processEntities(HashMap<String, Entity> entities,
			HashSet<String> objectEntities, String url, BufferedWriter writer)
			throws IOException {
		// get PLD
		String pld = "";
		if (url.startsWith("http")) {
			try {

				URI uri = new URI(url);
				String host = uri.getHost();
				try {
					InternetDomainName fullDomainName = InternetDomainName
							.from(host);
					pld = fullDomainName.topPrivateDomain().toString();
				} catch (IllegalArgumentException iex) {
					pld = host;
				}
			} catch (Exception e) {
				pld = url;
			}
		} else {
			pld = url;
		}
		// add the pld
		if (!pldHashMap.containsKey(pld)) {
			addPLD(pld);
		}

		HashSet<String> visitedEntities = new HashSet<String>();

		for (String key : entities.keySet()) {
			// if its a root, as all what is non root is linked
			if (!objectEntities.contains(key)) {
				HashSet<String> tmpVisitedEntities = new HashSet<String>();
				String entityString = calculateCanonicalStringOfEntityWithValues(
						key, entities, visitedEntities, tmpVisitedEntities, pld);
				// here we have the entity ready to check.
				String node = addEntity(pld, entityString, entities.get(key)
						.getSubject().value());
				if (node == null) {
					// it does not exist, so we do not need to upodate
					// but to write
					tmpVisitedEntities.add(key);
				}

				for (String entityKey : tmpVisitedEntities) {
					Entity e = entities.get(entityKey);
					// set pld
					e.setGraph(pld);
					writer.write(e.toLines());
				}
			}
		}
	}

	private synchronized void addPLD(String pld) {
		if (!pldHashMap.containsKey(pld)) {
			pldHashMap.put(pld, new HashMap<Integer, String>());
		}
	}

	private String calculateCanonicalStringOfEntityWithValues(String entityKey,
			HashMap<String, Entity> entities, HashSet<String> visitedEntities,
			HashSet<String> tmpVisitedEntities, String pld) {
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
								tmpVisitedEntities, pld);
						if (entityString.trim().length() < 1) {
							entityString = value.value();
						} else {
							// here we have the entity ready to check.
							String node = addEntity(pld, entityString,
									value.value());
							if (node != null) {
								// it exists already, we need to update
								replaceMap.put(value.value(), node);
							} else {
								// it does not exist, so we do not need to
								// update
								// but to write
								tmpVisitedEntities.add(value.value());
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
				entity.getProperties().get(property)
						.remove(Node.createBlankNode(node, entity.getGraph()));
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
	private synchronized String addEntity(String pld, String entityString,
			String subject) {
		String node = pldHashMap.get(pld).get(entityString.hashCode());
		if (node == null) {
			pldHashMap.get(pld).put(entityString.hashCode(), subject);
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
