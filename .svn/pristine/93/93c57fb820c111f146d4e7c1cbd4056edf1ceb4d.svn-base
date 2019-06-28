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

public class HReviewFactory {
	
	public static void commit(Context cont, Model model, String cNode) throws IOException, InterruptedException {
		String quStr = "";
		List<String> vars = new ArrayList<String>();
		String key = "";
		
	    REVIEW  rev  = REVIEW.getInstance();
	    VCARD   vcard   = VCARD.getInstance();
	    DCTERMS dc = DCTERMS.getInstance();
		quStr = ""+

		"SELECT * " +
		"WHERE { ?hreview <"+RDF.type+"> <"+rev.Review+"> . " +
		"		 OPTIONAL{ ?hreview <"+rev.title+"> ?summary . } " +
		"		 OPTIONAL{ ?hreview <"+dc.date+"> ?time . } " +
		"		 OPTIONAL{ ?hreview <"+rev.type+"> ?type . } " +
		"		 OPTIONAL{ ?hreview <"+rev.text+"> ?description . } " +
		"		 OPTIONAL{ ?hreview <"+rev.reviewer+"> ?reviewer . } " +
		"		 OPTIONAL{ ?item <"+rev.hasReview+"> ?hreview . " +
				"OPTIONAL{ ?item <"+vcard.fn+"> ?itemName . } " +
				"OPTIONAL{ ?item <"+vcard.url+"> ?itemUrl . } " +
				"OPTIONAL{ ?item <"+vcard.photo+"> ?itemPhoto . } " +
				"} " +
		"			 } " ;
		
		key = "hreview";
		vars.add("rating");
		vars.add("summary");
		vars.add("time");
		vars.add("type");
		vars.add("description");
		vars.add("reviewer");
		vars.add("item");
		vars.add("itemName");
		vars.add("itemUrl");
		vars.add("itemPhoto");
		
		
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
