package org.webdatacommons.structureddata.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ldif.entity.NodeTrait;
import ldif.runtime.Quad;

public class ShallowEntity {

	// the graph of the entity
	private String graph;
	// the subject of the entity
	private String subject;

	// the type of the entity
	private String type;

	// the other properties of the class including values, a list is needed
	// here, as there could be two or more values for one property
	private Map<String, List<String>> properties = new HashMap<String, List<String>>();


	
	public ShallowEntity(String subject, String type, String graph) {
		this.graph = graph;
		this.subject = subject;
		this.type = type;
	}

	public ShallowEntity(String subject) {
		this.subject = subject;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setProperties(HashMap<String, List<String>> properties) {
		this.properties = new HashMap<String, List<String>>(properties);
	}


	public void setProperty(String property, List<String> nodes)
			throws Exception {
		if (property == null || property.equals(EntityFileLoader.TYPEPROP)) {
			throw new Exception("Illegal property.");
		}
		if (nodes == null) {
			throw new Exception("Node is null.");
		}
		properties.put(property, nodes);
	}

	public void addProperty(String property, String node) throws Exception {
		if (property == null || property.equals(EntityFileLoader.TYPEPROP)) {
			throw new Exception("Illegal property.");
		}
		if (node == null) {
			throw new Exception("Node is null.");
		}
		List<String> nodes = properties.get(property);
		if (nodes == null) {
			nodes = new ArrayList<String>();
		}
		nodes.add(node);
		properties.put(property, nodes);
	}

	public String getGraph() {
		return graph;
	}

	public String getSubject() {
		return subject;
	}

	public String getType() {
		return type;
	}

	
	

	public Map<String, List<String>> getProperties() {
		return properties;
	}

}
