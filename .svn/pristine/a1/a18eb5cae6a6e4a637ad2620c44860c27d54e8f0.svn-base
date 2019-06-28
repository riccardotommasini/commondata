package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.deri.any23.vocab.DOAC;
import org.deri.any23.vocab.FOAF;
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

public class HResumeFactory {
	
	public static void commit(Context cont, Model model, String cNode) throws IOException, InterruptedException {
		String quStr = "";
		List<String> vars = new ArrayList<String>();
		String key = "";
		
		FOAF foaf = FOAF.getInstance();
	    DOAC doac = DOAC.getInstance();

		quStr = ""+

		"SELECT * " +
		"WHERE { ?person <"+RDF.type+"> <"+foaf.Person+"> . " +
		"		 OPTIONAL{ ?person <"+doac.summary+"> ?summary . } " +
		"		 OPTIONAL{ ?person <"+foaf.isPrimaryTopicOf+"> ?contact . } " +
		"		 OPTIONAL{ ?person <"+doac.experience+"> ?exp .  " +
				 "OPTIONAL{ ?exp <"+doac.title+"> ?expTitle . } " +
				 "OPTIONAL{ ?exp <"+doac.start_date+"> ?expStartDate . } " +
				 "OPTIONAL{ ?exp <"+doac.end_date+"> ?expEndDate . } " +
				 "OPTIONAL{ ?exp <"+doac.organization+"> ?expOranization . } " +
				 "} "+
		"		 OPTIONAL{ ?person <"+doac.education+"> ?edu .  " +
				 "OPTIONAL{ ?edu <"+doac.title+"> ?eduTitle . } " +
				 "OPTIONAL{ ?edu <"+doac.start_date+"> ?eduStartDate . } " +
				 "OPTIONAL{ ?edu <"+doac.end_date+"> ?eduEndDate . } " +
				 "OPTIONAL{ ?edu <"+doac.organization+"> ?eduOranization . } " +
				 "} "+
		"		 OPTIONAL{ ?person <"+doac.affiliation+"> ?affiliation . } " +
		"		 OPTIONAL{ ?person <"+doac.skill+"> ?skill . } " +
		"			 } " ;
		
		key = "person";
		vars.add("summary");
		vars.add("contact");
		vars.add("exp");
		vars.add("expTitle");
		vars.add("expStartDate");
		vars.add("expEndDate");
		vars.add("expOranization");
		vars.add("edu");
		vars.add("eduTitle");
		vars.add("eduStartDate");
		vars.add("eduEndDate");
		vars.add("eduOranization");
		vars.add("affiliation");
		vars.add("skill");
		
		
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
