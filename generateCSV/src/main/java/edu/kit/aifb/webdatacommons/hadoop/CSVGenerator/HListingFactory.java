package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.deri.any23.vocab.FOAF;
import org.deri.any23.vocab.HLISTING;
import org.deri.any23.vocab.ICAL;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;

public class HListingFactory {
	
	public static void commit(Context cont, Model model, String cNode) throws IOException, InterruptedException {

		String quStr = "";
		List<String> vars = new ArrayList<String>();
		String key = "";
		
		HLISTING list = HLISTING.getInstance();
		FOAF foaf = FOAF.getInstance();
		
		quStr = ""+

		"SELECT * " +
		"WHERE { ?hlisting <"+RDF.type+"> <"+list.Listing+"> . " +
		"		 OPTIONAL{ ?hlisting <"+list.dtlisted+"> ?dtlisted . } " +
		"		 OPTIONAL{ ?hlisting <"+list.price+"> ?price . } " +
		"		 OPTIONAL{ ?hlisting <"+list.permalink+"> ?permalink . } " +
		"		 OPTIONAL{ ?hlisting <"+list.description+"> ?description . } " +
		"		 OPTIONAL{ ?hlisting <"+list.summary+"> ?summary . } " +
		"		 OPTIONAL{ ?hlisting <"+list.action+"> ?action . } " +
		"		 OPTIONAL{ ?hlisting <"+list.lister+"> ?lister . " +
				"OPTIONAL{ ?lister <"+list.listerName+"> ?listerName . } " +
				"OPTIONAL{ ?lister <"+list.listerOrg+"> ?listerOrg . } " +
				"OPTIONAL{ ?lister <"+foaf.mbox+"> ?listerMail . } " +
				"OPTIONAL{ ?lister <"+list.listerUrl+"> ?listerUrl . } " +
				"OPTIONAL{ ?lister <"+list.tel+"> ?listerTel . } " +
				"OPTIONAL{ ?lister <"+list.listerLogo+"> ?listerLogo . } " +
				"			} " +
		"		 OPTIONAL{ ?hlisting <"+list.item+"> ?item . " +
				"OPTIONAL{ ?item <"+list.itemName+"> ?itemName . } " +
				"OPTIONAL{ ?item <"+list.itemUrl+"> ?itemUrl . } " +
				"OPTIONAL{ ?item <"+list.itemPhoto+"> ?itemPhoto . } " +
	            "OPTIONAL{ ?item <"+list.postOfficeBox+"> ?postOfficeBox . } " +
	            "OPTIONAL{ ?item <"+list.extendedAddress+"> ?extendedAddress . } " +
	            "OPTIONAL{ ?item <"+list.streetAddress+"> ?streetAddress . } " +
	            "OPTIONAL{ ?item <"+list.locality+"> ?locality . } " +
	            "OPTIONAL{ ?item <"+list.region+"> ?region . } " +
	            "OPTIONAL{ ?item <"+list.postalCode+"> ?postalCode . } " +
	            "OPTIONAL{ ?item <"+list.countryName+"> ?countryName . } " +
				"			} " +
		"			 } " ;
		
		key = "hlisting";
		vars.add("dtlisted");
		vars.add("price");
		vars.add("permalink");
		vars.add("description");
		vars.add("summary");
		vars.add("action"); 
//		vars.add("lister"); 
		vars.add("listerName");
		vars.add("listerOrg");
		vars.add("listerMail");
		vars.add("listerUrl");
		vars.add("listerTel");
		vars.add("listerLogo");
//		vars.add("item");
		vars.add("itemName");
		vars.add("itemUrl");
		vars.add("itemPhoto");
        vars.add("post-office-box");
        vars.add("extended-address");
        vars.add("street-address");
        vars.add("locality");
        vars.add("region");
        vars.add("postal-code");
        vars.add("country-name");
		
		
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
