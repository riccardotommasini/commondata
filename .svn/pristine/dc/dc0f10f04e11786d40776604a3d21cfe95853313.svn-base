package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.deri.any23.vocab.ICAL;
import org.deri.any23.vocab.REVIEW;
import org.deri.any23.vocab.VCARD;
import org.deri.any23.vocab.DCTERMS;
import org.deri.any23.vocab.WO;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;

public class SpeciesFactory {
	
	public static void commit(Context cont, Model model, String cNode) throws IOException, InterruptedException {
		String quStr = "";
		List<String> vars = new ArrayList<String>();
		String key = "";
		
		WO wo = WO.getInstance();

		quStr = ""+

		"SELECT * " +
		"WHERE { ?speciesK <"+RDF.type+"> <"+wo.species+"> . " +
		"		 OPTIONAL{ ?speciesK <"+wo.scientificName+"> ?sciName . } " +
		"		 OPTIONAL{ ?speciesK <"+wo.speciesName+"> ?name . } " +
		"		 OPTIONAL{ ?speciesK <"+wo.kingdom+"> ?kingdom . " +
				"?kingdom <"+wo.kingdomName+"> ?kingdomName . " +
				"} " +
		"		 OPTIONAL{ ?speciesK <"+wo.division+"> ?division . " +
				"?division <"+wo.divisionName+"> ?divisionName . " +
				"} " +
		"		 OPTIONAL{ ?speciesK <"+wo.phylum+"> ?phylum . " +
				"?phylum <"+wo.phylumName+"> ?phylumName . " +
				"} " +
		"		 OPTIONAL{ ?speciesK <"+wo.order+"> ?order . " +
				"?order <"+wo.orderName+"> ?orderName . " +
				"} " +
		"		 OPTIONAL{ ?speciesK <"+wo.family+"> ?family . " +
				"?family <"+wo.familyName+"> ?familyName . " +
				"} " +
		"		 OPTIONAL{ ?speciesK <"+wo.kingdom+"> ?genus . " +
				"?genus <"+wo.kingdomName+"> ?genusName . " +
				"} " +
		"		 OPTIONAL{ ?speciesK <"+wo.species+"> ?species . " +
				"?species <"+wo.speciesName+"> ?speciesName . " +
				"} " +
		"		 OPTIONAL{ ?speciesK <"+wo.clazz+"> ?class . " +
				"?class <"+wo.clazzName+"> ?className . " +
				"} " +
		"			 } " ;
		
		key = "speciesK";
		vars.add("sciName");
		vars.add("name");
//		vars.add("kingdom");
		vars.add("kingdomName");
//		vars.add("division");
		vars.add("divisionName");
//		vars.add("phylum");
		vars.add("phylumname");
//		vars.add("order");
		vars.add("orderName");
//		vars.add("family");
		vars.add("familyName");
//		vars.add("genus");
		vars.add("genusName");
//		vars.add("species");
		vars.add("speciesName");
//		vars.add("class");
		vars.add("className");
		
		
		Query query = QueryFactory.create(quStr);
		QueryExecution qE = QueryExecutionFactory.create(query, model);
		ResultSet rS = qE.execSelect();
		
		while(rS.hasNext()){
			QuerySolution qS = rS.next();
			
			RDFNode sol = qS.get(key);
			Text keyT = new Text(sol.toString());
			String line = sol.toString()+",";
			for(String var : vars){
				sol = qS.get(var);
				if(sol != null){
					line += "\""+sol.toString()+"\",";
				}else{
					line += ",";
				}
			}
			line += "\""+cNode+"\",";
			cont.write(keyT, new Text(line.replace(System.getProperty("line.separator"), "")) );
		}
	}
}
