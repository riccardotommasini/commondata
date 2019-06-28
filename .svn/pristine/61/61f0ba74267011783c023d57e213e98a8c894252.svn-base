package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.deri.any23.extractor.html.HCardName;
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

public class HCardFactory {
	
	public static void commit(Context cont, Model model, String cNode) throws IOException, InterruptedException {
		String quStr = "";
		List<String> vars = new ArrayList<String>();
		String key = "";
		
		VCARD hcard = VCARD.getInstance();

		quStr = ""+

		"SELECT * " +
		"WHERE { ?card <"+RDF.type+"> <"+hcard.VCard+"> . " +
		"		 OPTIONAL{ ?card <"+hcard.fn+"> ?fn . } " +
		"		 OPTIONAL{ ?card <"+hcard.n+"> ?n . ";
		
		int q = 0;
		for (String fieldName : HCardName.FIELDS) {
			q++;
			quStr += "" +
			"		 OPTIONAL{ ?n <"+hcard.getProperty(fieldName)+"> ?"+fieldName.replace("-", "")+" . } ";

		}
		
		quStr +=" }"+
		"		 OPTIONAL{ ?card <"+hcard.org+"> ?org . " +
					"OPTIONAL{ ?org <"+hcard.organization_name+"> ?orgName . } " +
					"OPTIONAL{ ?org <"+hcard.organization_unit+"> ?orgUnit . } " +
				"} " +
		"		 OPTIONAL{ ?card <"+hcard.sort_string+"> ?sortString . } " +
		"		 OPTIONAL{ ?card <"+hcard.url+"> ?url . } " +
		"		 OPTIONAL{ ?card <"+hcard.email+"> ?email . } " +
		"		 OPTIONAL{ ?card <"+hcard.photo+"> ?photo . } " +
		"		 OPTIONAL{ ?card <"+hcard.logo+"> ?logo . } " +
		"		 OPTIONAL{ ?card <"+hcard.uid+"> ?uid . } " +
		"		 OPTIONAL{ ?card <"+hcard.class_+"> ?class . } " +
		"		 OPTIONAL{ ?card <"+hcard.bday+"> ?bday . } " +
		"		 OPTIONAL{ ?card <"+hcard.rev+"> ?rev . } " +
		"		 OPTIONAL{ ?card <"+hcard.tz+"> ?tz . } " +
		"		 OPTIONAL{ ?card <"+hcard.category+"> ?category . } " +
		"		 OPTIONAL{ ?card <"+hcard.adr+"> ?adr . } " +
		"		 OPTIONAL{ ?card <"+hcard.homeTel+"> ?homeTel . } " +
		"		 OPTIONAL{ ?card <"+hcard.mobileTel+"> ?mobileTel . } " +
		"		 OPTIONAL{ ?card <"+hcard.tel+"> ?tel . } " +
		"		 OPTIONAL{ ?card <"+hcard.unlabeledTel+"> ?unlabeledTel . } " +
		"		 OPTIONAL{ ?card <"+hcard.workTel+"> ?workTel . } " +
		"		 OPTIONAL{ ?card <"+hcard.title+"> ?title . } " +
		"		 OPTIONAL{ ?card <"+hcard.role+"> ?role . } " +
		"		 OPTIONAL{ ?card <"+hcard.note+"> ?note . } " +
		"		 OPTIONAL{ ?card <"+hcard.geo+"> ?geo . } " +
		"			 } " ;
		
		
		
		key = "card";
		vars.add("fn");
//		vars.add("n");
		for (String fieldName : HCardName.FIELDS) {
			vars.add(fieldName.replace("-", ""));  //given-name, family-name, additional-name, nickname, honorific-prefix, honorific-suffix, 
		}
//		vars.add("org");
		vars.add("orgName");
		vars.add("orgUnit");
		vars.add("sortstring");
		vars.add("url");
		vars.add("email");
		vars.add("photo");
		vars.add("logo");
		vars.add("uid");
		vars.add("class");
		vars.add("bday");
		vars.add("rev");
		vars.add("tz");
		vars.add("category");
		vars.add("adr");
		vars.add("homeTel");
		vars.add("mobileTel");
		vars.add("tel");
		vars.add("unlabeledTel");
		vars.add("workTel");
		vars.add("title");
		vars.add("role");
		vars.add("note");
		vars.add("geo");
		
		
		Query query = QueryFactory.create(quStr);
		QueryExecution qE = QueryExecutionFactory.create(query, model);
		ResultSet rS = qE.execSelect();
		
		cont.progress();
		
		int c=0;
		while(rS.hasNext()){
			c++;
			if(c ==100){
				c=0;
				cont.progress();
			}
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
