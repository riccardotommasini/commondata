package org.webdatacommons.structureddata.model;

import java.util.ArrayList;
import java.util.List;

import ldif.entity.NodeTrait;
import ldif.local.datasources.dump.QuadFileLoader;
import ldif.runtime.Quad;

public class EntityFileLoader {

	public static String TYPEPROP = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	private static QuadFileLoader qlf = new QuadFileLoader();

	public Entity loadEntityFromQuads(List<Quad> quads) throws Exception {
		if (quads != null && quads.size() > 0) {
			// take the first quad to initialize the entity
			Quad q1 = quads.get(0);
			// this is needed to check if all nodes belong to the same subject.
			NodeTrait mainNode = q1.subject();
			Entity e = new Entity(q1.subject());
			e.setGraph(q1.graph());
			for (Quad q : quads) {
				if (!mainNode.equals(q.subject())) {
					throw new Exception(
							"Cannot create Entity for different subjects.");
				} else {
					if (q.predicate().equals(TYPEPROP)) {
						e.setType(q.value());
					} else {
						e.addProperty(q.predicate(), q.value());
					}
				}
			}
			return e;
		} else {
			throw new Exception("Cannot create empty Entity.");
		}
	}

	public ShallowEntity loadEntityFromQuadsForConversion (List<Quad> quads) throws Exception {
		
		
			if (quads != null && quads.size() > 0) {
				// take the first quad to initialize the entity
				Quad q1 = quads.get(0);
				// this is needed to check if all nodes belong to the same subject.
				NodeTrait mainNode = q1.subject();
				ShallowEntity e = new ShallowEntity(q1.subject().value());
				e.setGraph(q1.graph());
				String entityClass= "";
				for (Quad q : quads) {
					if (!mainNode.equals(q.subject())) {
						throw new Exception(
								"Cannot create Entity for different subjects.");
					} else {
						if (q.predicate().equals(TYPEPROP)) {
							entityClass= q.value().toString().replace(">", "").replace("<", "");
							e.setType(entityClass);
						} else {
							//remove the entity class and keep only the property name
							if (!entityClass.equals(""))
								e.addProperty(q.predicate().replace(entityClass+"/", ""), q.value().toString());
							else
								e.addProperty(q.predicate(), q.value().toString());

						}
					}
				}
				
				return e;
			} else {
				throw new Exception("Cannot create empty Entity.");
			}
	
	}
	
	public Entity loadEntityFromLines(List<String> lines) throws Exception {
		List<Quad> quads = new ArrayList<Quad>();
		for (String line : lines) {
			quads.add(qlf.parseQuadLine(line));
		}
		return loadEntityFromQuads(quads);
	}

	public static void main(String[] args) throws Exception {
		String s1 = "_:g7615caeaafc542cc660fnode1a2e76197f26501bb348f390d8c18840 <http://schema.org/NewsArticle/url> <http://www.newgrounds.com/bbs/topic/1356260> <http://0-alfyan.newgrounds.com/> .";
		String s2 = "_:g7615caeaafc542cc660fnode1a2e76197f26501bb348f390d8c18840 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/NewsArticle> <http://0-alfyan.newgrounds.com/> .";
		String s3 = "_:g7615caeaafc542cc660fnode1a2e76197f26501bb348f390d8c18840 <http://schema.org/NewsArticle/name> \"Game Devs to Follow\" <http://0-alfyan.newgrounds.com/> .";
		List<String> sList = new ArrayList<String>();
		sList.add(s1);
		sList.add(s2);
		sList.add(s3);
		EntityFileLoader efl = new EntityFileLoader();
		Entity e = efl.loadEntityFromLines(sList);
		System.out.println("The Entity written to lines:");
		System.out.println(e.toLines());
		System.out.println("The Type of the Entity:");
		System.out.println(e.getType().value());

	}
}
