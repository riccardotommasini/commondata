package org.webdatacommons.structureddata.cleanser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import ldif.entity.Node;
import ldif.entity.NodeTrait;

import org.webdatacommons.structureddata.processor.EntityProcessorWithOutput;
import org.webdatacommons.structureddata.model.Entity;

import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;
import de.mannheim.uni.types.ColumnTypeGuesser;
import de.mannheim.uni.types.ColumnTypeGuesser.ColumnDataType;

/**
 * This class is used to fix schema.org quads based on the Heuristics
 * presented in the ESWC 2015 Paper of Meusel and Paulheim. The fixes include:
 * <ul>
 * <li>Fixing namespaces</li>
 * <li>Fixing missing slashes and capitalization for properties and classes</li>
 * <li>Fixing ObjectProperties with Literal Values</li>
 * <li>Fixing Property Domain Violations</li>
 * </ul>
 * 
 * @author Robert Meusel (robert@dwslab.de)
 * 
 */
public class SchemaViolationCorrection extends EntityProcessorWithOutput {

	// first key is the class, value is the list of all properties
	private Map<String, HashSet<String>> classPropMap = new HashMap<String, HashSet<String>>();
	// collection of allowed types of Properties
	private Map<String, HashSet<ColumnDataType>> propValue = new HashMap<String, HashSet<ColumnDataType>>();

	// map of lower case type to correct spelling
	private Map<String, String> lowerCaseType = new HashMap<String, String>();

	// map of lower case properties to correct spelling
	private Map<String, String> lowerCaseProperty = new HashMap<String, String>();

	// map with least abstract common range for object properties with literals
	private Map<String, String> leastAbstractCommonRange = new HashMap<String, String>();

	// Domain Violation Fix Map
	private Map<String, Map<String, DomainViolationFix>> domainViolationsFixes = new HashMap<String, Map<String, DomainViolationFix>>();

	// Set of hosts which got fixed
	private Set<String> fixedHosts = new HashSet<String>();

	// Set of plds which got fixed
	private Set<String> fixedPlds = new HashSet<String>();

	// Fixed triple counter
	private AtomicLong fixedTriples = new AtomicLong();

	// number of added triples
	private AtomicLong addedTriples = new AtomicLong();

	public SchemaViolationCorrection(String inputFolder, String outputFolder,
			String classPropFile, String datatypePropFile,
			String objectPropFile, String domainViolationFixFile,
			String rangeViolationFixFile, int threads) throws Exception {
		super(inputFolder, outputFolder, threads);

		initSchemaFiles(classPropFile, datatypePropFile, objectPropFile,
				domainViolationFixFile, rangeViolationFixFile);
	}

	private void initSchemaFiles(String classPropFile, String datatypePropFile,
			String objectPropFile, String domainViolationFixFile,
			String rangeViolationFixFile) throws IOException {
		System.out.println("Loading schema to memory ...");
		String sep = " ";
		// adding class prop stuff
		BufferedReader br = InputUtil
				.getBufferedReader(new File(classPropFile));
		while (br.ready()) {
			String line = br.readLine();
			String tok[] = line.split(sep);
			HashSet<String> set = classPropMap.get(tok[0]);
			if (set == null) {
				set = new HashSet<String>();
			}
			set.add(tok[1]);
			classPropMap.put(tok[0], set);

			// adding mapping with lower case - not most time saving but will
			// work
			String lowP = tok[1].toLowerCase();
			lowerCaseProperty.put(lowP, tok[1]);

			// adding mapping with lowercase - not most time saving but will
			// work
			String lowT = tok[0].toLowerCase();
			lowerCaseType.put(lowT, tok[0]);
		}
		br.close();

		// creating the prop types, starting with datatypeProps
		br = InputUtil.getBufferedReader(new File(datatypePropFile));
		while (br.ready()) {
			String line = br.readLine();
			String tok[] = line.split(sep);
			ColumnDataType t = null;
			if (tok[1].equals("http://schema.org/Text")) {
				t = ColumnDataType.string;
			} else if (tok[1].equals("http://schema.org/URL")) {
				t = ColumnDataType.link;
			} else if (tok[1].equals("http://schema.org/Boolean")) {
				t = ColumnDataType.bool;
			} else if (tok[1].equals("http://schema.org/Date")
					|| tok[1].equals("http://schema.org/DateTime")
					|| tok[1].equals("http://schema.org/Time")) {
				t = ColumnDataType.date;
			} else if (tok[1].equals("http://schema.org/Number")
					|| tok[1].equals("http://schema.org/Integer")) {
				t = ColumnDataType.numeric;
			} else {
				System.out.println("Unknown datatype discovered: " + tok[1]);
				t = ColumnDataType.unknown;
			}
			HashSet<ColumnDataType> dataTypes = propValue.get(tok[0]);
			if (dataTypes == null) {
				dataTypes = new HashSet<ColumnTypeGuesser.ColumnDataType>();
			}
			dataTypes.add(t);
			propValue.put(tok[0], dataTypes);
		}
		br.close();
		// now adding object type props, and setting NULL for those included in
		// both lists.
		br = InputUtil.getBufferedReader(new File(objectPropFile));
		while (br.ready()) {
			String line = br.readLine();
			String tok[] = line.split(sep);
			HashSet<ColumnDataType> dataTypes = propValue.get(tok[0]);
			if (dataTypes == null) {
				dataTypes = new HashSet<ColumnTypeGuesser.ColumnDataType>();
			}
			dataTypes.add(ColumnDataType.object);
			propValue.put(tok[0], dataTypes);
		}
		br.close();

		// filling least abstract type for objectproperty range violations
		br = InputUtil.getBufferedReader(new File(rangeViolationFixFile));
		while (br.ready()) {
			String line = br.readLine();
			String tok[] = line.split("\t");
			leastAbstractCommonRange.put(tok[0], tok[1]);
		}
		br.close();

		// filling domain property fixes for uniq solutions
		br = InputUtil.getBufferedReader(new File(domainViolationFixFile));
		while (br.ready()) {
			String line = br.readLine();
			// 0 = type, 1 = prop, 2 = count (irrelevant), 3 = fixing string
			String tok[] = line.split("\t");
			Map<String, DomainViolationFix> fixes = domainViolationsFixes
					.get(tok[0]);
			if (fixes == null) {
				fixes = new HashMap<String, DomainViolationFix>();
			}
			DomainViolationFix fix = new DomainViolationFix(tok[3]);
			fixes.put(tok[1], fix);
			domainViolationsFixes.put(tok[0], fixes);
		}
		br.close();

		System.out.println("... done.");
	}

	private static String fixNamespace(String input) {
		// remove www.
		input = input.replace("www.", "");
		// lower case schema.org
		input = input.replaceAll("(.*)(?i)schema\\.org(.*)", "$1schema.org$2");
		// add http (and remove all other possibilities)
		input = input.replaceFirst("^.*schema", "http://schema");
		// add missing slash after schema.org
		input = input.replaceFirst("http://schema\\.org",
				"http://schema\\.org/");
		input = input.replaceFirst("http://schema\\.org//",
				"http://schema\\.org/");

		return input;
	}

	@Override
	protected void processEntity(Entity e, BufferedWriter bw) {

		int count = 0;
		String subject = e.getSubject().value();
		String graph = e.getGraph();
		String type = null;
		HashMap<String, List<NodeTrait>> properties = null;

		long fixedTripleCount = 0;
		long addedTripleCount = 0;

		// fixing Any23 property creation
		// properties -- this is not really a fix of triples, as it is created
		// by the used library
		type = e.getType() == null ? null : e.getType().value();
		properties = new HashMap<String, List<NodeTrait>>();
		if (type != null) {
			for (String prop : e.getProperties().keySet()) {
				properties.put(correctSchemaOrgProperty(prop, type), e
						.getProperties().get(prop));
			}
		}
		// update entity
		e.setProperties(properties);

		// fixing namespaces
		// types
		type = e.getType() == null ? null : e.getType().value();
		if (type != null && !type.startsWith("http://schema.org/")) {
			String typeFix = fixNamespace(type);

			// update entity
			e.setType(new Node(typeFix, e.getType().datatype(), e.getType()
					.nodeType(), graph));
			if (!typeFix.equals(type)) {
				fixedTripleCount++;
			}
		}
		// properties
		properties = new HashMap<String, List<NodeTrait>>();
		for (String prop : e.getProperties().keySet()) {
			if (!prop.startsWith("http://schema.org/")) {
				String propFix = fixNamespace(prop);
				properties.put(propFix, e.getProperties().get(prop));
				if (!propFix.equals(prop)) {
					fixedTripleCount++;
				}
			} else {
				properties.put(prop, e.getProperties().get(prop));
			}
		}
		// update entity
		e.setProperties(properties);

		// fixing capitalization
		// types
		type = e.getType() == null ? null : e.getType().value();
		if (type != null && !classPropMap.containsKey(type)) {
			type = type.toLowerCase();
			if (lowerCaseType.containsKey(type.toLowerCase())) {
				type = lowerCaseType.get(type.toLowerCase());
				// update entity
				e.setType(new Node(type, e.getType().datatype(), e.getType()
						.nodeType(), graph));
				fixedTripleCount++;
			}
		}
		// properties
		properties = new HashMap<String, List<NodeTrait>>();
		for (String prop : e.getProperties().keySet()) {
			if (!propValue.containsKey(prop)) {
				if (lowerCaseProperty.containsKey(prop.toLowerCase())) {
					properties.put(lowerCaseProperty.get(prop.toLowerCase()), e
							.getProperties().get(prop));
					fixedTripleCount++;
				} else {
					properties.put(prop, e.getProperties().get(prop));
				}
			} else {
				properties.put(prop, e.getProperties().get(prop));
			}
		}
		// update entity
		e.setProperties(properties);

		// Domain Violation of Properties
		Map<String, Entity> newlyCreatedEntitiesForDomainViolations = new HashMap<String, Entity>();
		if (type != null) {
			// get all properties which are defined for this type
			Set<String> schemaProps = classPropMap.get(type);
			// get fixes
			Map<String, DomainViolationFix> propertyFixes = domainViolationsFixes
					.get(type);
			// if there are any schemas and the fixes are also not empty
			if (schemaProps != null && propertyFixes != null) {
				// for all properties
				Map<String, List<NodeTrait>> propsFromEntity = new HashMap<String, List<NodeTrait>>(
						e.getProperties());
				for (String prop : propsFromEntity.keySet()) {
					// test if the property is defined for the give type
					if (!schemaProps.contains(prop)) {
						// if not, we check if its defined at all
						if (propValue.containsKey(prop)) {
							// now we check what to do with this property
							if (propertyFixes.containsKey(prop)) {
								// here is a fix
								DomainViolationFix fix = propertyFixes
										.get(prop);
								// so we remove the old property
								List<NodeTrait> nodes = e.getProperties().get(
										prop);
								e.getProperties().remove(prop);

								// create new entity
								Entity newEntity = newlyCreatedEntitiesForDomainViolations
										.get(fix.type);
								if (newEntity == null) {
									// create new entity with type
									newEntity = new Entity(
											Node.createBlankNode(subject + ""
													+ graph.hashCode() + ""
													+ count++, graph));
									// set graph
									newEntity.setGraph(graph);

									// set type
									newEntity.setType(Node.createUriNode(type,
											graph));
									addedTripleCount++;

									// now we need to link the nodes somehow

									if (fix.turn) {
										try {
											newEntity.addProperty(prop,
													e.getSubject());
										} catch (Exception e1) {
											System.out.println("Subject: "
													+ subject);
											e1.printStackTrace();
										}
									} else {
										try {
											e.addProperty(prop,
													newEntity.getSubject());
										} catch (Exception e1) {
											System.out.println("Subject: "
													+ subject);
											e1.printStackTrace();
										}
									}
									// add it
									newlyCreatedEntitiesForDomainViolations
											.put(type, newEntity);
								}
								// add the property with value
								for (NodeTrait n : nodes) {
									try {
										newEntity.addProperty(fix.property, n);
										fixedTripleCount++;
									} catch (Exception e1) {
										System.out.println("Subject: "
												+ subject);
										e1.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
		}

		// Literals for ObjectProperties
		// Range Violation of ObjectProperties
		List<Entity> newlyCreatedEntitiesForRangeViolations = new ArrayList<Entity>();
		// for each property and each value
		Map<String, List<NodeTrait>> propsFromEntity = new HashMap<String, List<NodeTrait>>(
				e.getProperties());
		for (String prop : propsFromEntity.keySet()) {
			HashSet<ColumnDataType> propTypes = propValue.get(prop);
			// for only object properties we need to check
			if (propTypes != null && propTypes.contains(ColumnDataType.object)
					&& propTypes.size() == 1) {
				// so we remove
				List<NodeTrait> nodes = propsFromEntity.get(prop);
				e.getProperties().remove(prop);
				for (NodeTrait n : nodes) {
					// this is not a blankNode, also it should be
					if (!n.isBlankNode()) {
						// get the type which is at least abstract for the prop
						String newType = leastAbstractCommonRange.get(prop);
						Entity newEntity = new Entity(Node.createBlankNode(
								subject + "" + graph.hashCode() + "" + count++,
								graph));
						// set type
						newEntity.setType(Node.createUriNode(newType, graph));
						// set graph
						newEntity.setGraph(graph);
						addedTripleCount++;

						// set property
						String value = n.value();
						if (ColumnTypeGuesser.isValueOfType(
								ColumnDataType.link, value)) {
							try {
								newEntity.addProperty("http://schema.org/url",
										n);
								addedTripleCount++;
							} catch (Exception e1) {
								System.out.println("Subject: " + subject);
								e1.printStackTrace();
							}
						} else {
							try {
								newEntity.addProperty("http://schema.org/name",
										n);
								addedTripleCount++;
							} catch (Exception e1) {
								System.out.println("Subject: " + subject);
								e1.printStackTrace();
							}
						}
						// link new entity
						try {
							e.addProperty(prop, newEntity.getSubject());
							fixedTripleCount++;
						} catch (Exception e1) {
							System.out.println("Subject: " + subject);
							e1.printStackTrace();
						}
						// add it
						newlyCreatedEntitiesForRangeViolations.add(newEntity);
					} else {
						try {
							e.addProperty(prop, n);
						} catch (Exception e1) {
							System.out.println("Subject: " + subject);
							e1.printStackTrace();
						}
					}
				}
			}
		}

		// now we write all the stuff
		try {
			// original, fixed entity
			bw.write(e.toLines());
			// new stuff from the domain violation
			for (String byType : newlyCreatedEntitiesForDomainViolations
					.keySet()) {
				bw.write(newlyCreatedEntitiesForDomainViolations.get(byType)
						.toLines());
			}
			// new stuff from the range violation
			for (Entity en : newlyCreatedEntitiesForRangeViolations) {
				bw.write(en.toLines());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// and add the statistics
		if (fixedTripleCount > 0 || addedTripleCount > 0) {
			fixedTriples.addAndGet(fixedTripleCount);
			addedTriples.addAndGet(addedTripleCount);

			// we sync this over all threads
			// synchronized (fixedHosts) {
			// try {
			// URI uri = new URI(graph);
			// String host = uri.getHost();
			// fixedHosts.add(host);
			// try {
			// String pld = DomainUtils.getPayLevelDomain(host);
			// fixedPlds.add(pld);
			// } catch (IllegalArgumentException iex) {
			// fixedPlds.add(host);
			// }
			// } catch (URISyntaxException e1) {
			// e1.printStackTrace();
			// }
			// }

		}
	}

	public static String correctSchemaOrgProperty(String prop, String type) {
		if (type != null && type.contains("http://schema.org")) {
			return prop.replace(type, "http://schema.org");
		} else {
			return prop;
		}
	}

	@Override
	protected void afterProcess() {
		super.afterProcess();
		// write hosts
		BufferedWriter bw;
		try {
			bw = OutputUtil.getGZIPBufferedWriter(new File(outputFolder,
					"fixed.host.list.gz"));

			for (String s : fixedHosts) {
				bw.write(s + "\n");
			}
			bw.close();

			// write plds
			bw = OutputUtil.getGZIPBufferedWriter(new File(outputFolder,
					"fixed.pld.list.gz"));
			for (String s : fixedPlds) {
				bw.write(s + "\n");
			}
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Heuristics could ");
		System.out.println("fix " + fixedTriples + " wrong Triples");
		System.out.println("add " + addedTriples + " missing Triples");
		System.out.println("Fixed and added triples occured in "
				+ fixedHosts.size() + " different hosts.");
		System.out.println("Fixed and added triples occured in "
				+ fixedPlds.size() + " different plds.");
	}

	private class DomainViolationFix {
		String property;
		String type;
		boolean turn = false;

		public DomainViolationFix(String fixString) {
			if (fixString.startsWith("-")) {
				this.turn = true;
				fixString = fixString.substring(1);
			}
			this.type = fixString.split(";")[1];
			this.property = fixString.split(";")[0];
		}

	}

}
