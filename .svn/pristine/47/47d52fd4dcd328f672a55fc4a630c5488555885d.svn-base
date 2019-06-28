package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;

import org.deri.any23.vocab.ICAL;
import org.semanticweb.yars.nx.Node;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;

public class HCalendarFactory {

	public static void commit(Context cont, Model model, String cNode) throws IOException, InterruptedException {

		
		
		ICAL ical = ICAL.getInstance();
		String quStr = ""+

		"SELECT * " +
		"WHERE { ?hcalendar <"+RDF.type+"> <"+ical.Vcalendar+"> . " +
		"		 OPTIONAL{ ?hcalendar <"+ical.component.toString()+"> ?comp . } " +
				"OPTIONAL{ ?comp <"+ical.uid +"> ?uid . } " +
				"OPTIONAL{ ?comp <"+ical.url +"> ?url . }" +
				"OPTIONAL{ ?comp <"+ical.categories+"> ?category . } " +
				"OPTIONAL{ ?comp <"+ical.rrule +"> ?rrule . " +
						"	?rrule <"+ical.freq+"> ?freq . }" +
				"OPTIONAL{ ?comp <"+ical.organizer +"> ?organizer . " +
						"	?organizer <"+ical.calAddress+"> ?organizerAdress . }" +
				"OPTIONAL{ ?comp <"+ical.getProperty("summary") +">  ?summary . }" +
				"OPTIONAL{ ?comp <"+ical.getProperty("class") +">  ?class . }" +
				"OPTIONAL{ ?comp <"+ical.getProperty("transp") +">  ?transp . }" +
				"OPTIONAL{ ?comp <"+ical.getProperty("description") +">  ?description . }" +
				"OPTIONAL{ ?comp <"+ical.getProperty("status") +">  ?status . }" +
				"OPTIONAL{ ?comp <"+ical.getProperty("location") +">  ?location . }" +
				"OPTIONAL{ ?comp <"+ical.getProperty("dtstart") +">  ?dtstart . }" +
				"OPTIONAL{ ?comp <"+ical.getProperty("dtstamp") +">  ?dtstamp . }" +
				"OPTIONAL{ ?comp <"+ical.getProperty("dtend") +">  ?dtend . }" +
		"			 } " ;
		
		List<String> vars = new ArrayList<String>();

		String key = "hcalendar";
		vars.add("comp");
		vars.add("uid");
		vars.add("url");
		vars.add("category");
		vars.add("frequenzy");
		vars.add("organizerAdress");
		vars.add("summary");
        vars.add("class");
        vars.add("transp");
        vars.add("description");
        vars.add("status");
        vars.add("location");
        vars.add("dtstart");
        vars.add("dtstamp");
        vars.add("dtend");
		
		
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
