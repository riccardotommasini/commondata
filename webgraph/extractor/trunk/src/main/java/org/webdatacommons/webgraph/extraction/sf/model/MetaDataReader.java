package org.webdatacommons.webgraph.extraction.sf.model;

import com.google.gson.Gson;

public class MetaDataReader {

	public static Metadata read(String json) {
		Gson gson = new Gson();
		
		return gson.fromJson(json, Metadata.class);
	}
	
	public static Location readLocation(String json) {
		Gson gson = new Gson();
		
		return gson.fromJson(json, Location.class);
	}
	
	public static MetadataSmall readSmall(String json) {
		Gson gson = new Gson();
		
		return gson.fromJson(json, MetadataSmall.class);
	}
}
