package org.webdatacommons.structureddata.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ldif.entity.NodeTrait;
import ldif.runtime.Quad;

public class Entity {

	// the graph of the entity
	private String graph;
	// the subject of the entity
	private NodeTrait subject;

	// the type of the entity
	private NodeTrait type;

	// the other properties of the class including values, a list is needed
	// here, as there could be two or more values for one property
	private Map<String, List<NodeTrait>> properties = new HashMap<String, List<NodeTrait>>();

	public Entity(NodeTrait subject, NodeTrait type, String graph) {
		this.graph = graph;
		this.subject = subject;
		this.type = type;
	}

	public Entity(NodeTrait subject) {
		this.subject = subject;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public void setType(NodeTrait type) {
		this.type = type;
	}

	public void setProperties(HashMap<String, List<NodeTrait>> properties) {
		this.properties = new HashMap<String, List<NodeTrait>>(properties);
	}


	public void setProperty(String property, List<NodeTrait> nodes)
			throws Exception {
		if (property == null || property.equals(EntityFileLoader.TYPEPROP)) {
			throw new Exception("Illegal property.");
		}
		if (nodes == null) {
			throw new Exception("Node is null.");
		}
		properties.put(property, nodes);
	}

	public void addProperty(String property, NodeTrait node) throws Exception {
		if (property == null || property.equals(EntityFileLoader.TYPEPROP)) {
			throw new Exception("Illegal property.");
		}
		if (node == null) {
			throw new Exception("Node is null.");
		}
		List<NodeTrait> nodes = properties.get(property);
		if (nodes == null) {
			nodes = new ArrayList<NodeTrait>();
		}
		nodes.add(node);
		properties.put(property, nodes);
	}

	public String getGraph() {
		return graph;
	}

	public NodeTrait getSubject() {
		return subject;
	}

	public NodeTrait getType() {
		return type;
	}

	/**
	 * Converts an entity back into quads.
	 * 
	 * @return {@link Quad}
	 */
	public List<Quad> toQuads() {
		List<Quad> quads = new ArrayList<Quad>();
		// type quad
		if (type != null) {
			quads.add(new Quad(subject, EntityFileLoader.TYPEPROP, type, graph));
		}
		// property quads
		for (String prop : properties.keySet()) {
			for (NodeTrait n : properties.get(prop)) {
				quads.add(new Quad(subject, prop, n, graph));
			}
		}
		return quads;
	}

	/**
	 * Writes an entity back into lines representing quads. Internally makes use
	 * of {@link Entity#toQuads()}.
	 * 
	 * @return String with all quads representing the Entity separated by \n.
	 */
	public String toLines() {
		String output = "";
		for (Quad q : toQuads()) {
			output += q.toLine();
		}
		return output;
	}

	public String toTripleLines() {
		String output = "";
		for (Quad q : toQuads()) {
			output += q.toNTripleFormat() + " .\n";
		}
		return output;
	}

	@SuppressWarnings("unused")
	public int getPropertyCount() {
		int c = 0;
		for (String s : properties.keySet()) {
			for (NodeTrait t : properties.get(s)) {
				c++;
			}
		}
		return c;
	}

	public Map<String, List<NodeTrait>> getProperties() {
		return properties;
	}

}
