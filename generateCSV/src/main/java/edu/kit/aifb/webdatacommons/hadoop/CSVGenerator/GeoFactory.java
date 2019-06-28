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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;

public class GeoFactory {
	
	public static void commit(Context cont, Model model, String cNode) throws IOException, InterruptedException {
		String quStr = "";
		List<String> vars = new ArrayList<String>();
		String key = "";
		
	    VCARD   vcard   = VCARD.getInstance();

		quStr = ""+

		"SELECT * " +
		"WHERE { ?location <"+RDF.type+"> <"+vcard.Location+"> . " +
		"		 OPTIONAL{ ?location <"+vcard.longitude+"> ?long . } " +
		"		 OPTIONAL{ ?location <"+vcard.latitude+"> ?lat . } " +
		"			 } " ;
		
		key = "location";
		vars.add("long");
		vars.add("lat");
		
		
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
