package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.deri.any23.vocab.FOAF;
import org.deri.any23.vocab.ICAL;
import org.deri.any23.vocab.REVIEW;
import org.deri.any23.vocab.VCARD;
import org.deri.any23.vocab.DCTERMS;
import org.deri.any23.vocab.XFN;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;

public class XfnFactory {
	
	public static void commit(Context cont, Model model, String cNode) throws IOException, InterruptedException {
		String quStr = "";
		List<String> vars = new ArrayList<String>();
		String key = "";
		
		FOAF foaf = FOAF.getInstance();
	    XFN  xfn  = XFN.getInstance();

		quStr = ""+

		"SELECT * " +
		"WHERE { ?person <"+RDF.type+"> <"+foaf.Person+"> . " +
		"		 ?person <"+xfn.mePage+"> ?page . " +
				"{ { " +
		"		 ?person <"+xfn.mePage+"> ?contactHyperLink . " +
				"?page <"+xfn.getExtendedProperty("me")+"> ?contactHyperLink . " +
				
				"} UNION { " +
				
				
		"		 OPTIONAL { ?person <"+xfn.acquaintance+"> ?acquaintance . " +
					"?page <"+xfn.acquaintance+"-hyperlink> ?acquaintanceHyperLink . " +
					"?acquaintance <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?acquaintance  <"+xfn.mePage+"> ?acquaintanceHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.child+"> ?child . " +
					"?page <"+xfn.child+"-hyperlink> ?childHyperLink . " +
					"?child <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?child <"+xfn.mePage+"> ?childHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.colleague+"> ?colleague . " +
					"?page <"+xfn.colleague+"-hyperlink> ?colleagueHyperLink . " +
					"?colleague <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?colleague <"+xfn.mePage+"> ?colleagueHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.contact+"> ?contact . " +
					"?page <"+xfn.contact+"-hyperlink> ?contactHyperLink . " +
					"?contact <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?contact <"+xfn.mePage+"> ?contactHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.coResident+"> ?coResident . " +
					"?page <"+xfn.coResident+"-hyperlink> ?coResidentHyperLink . " +
					"?coResident <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?coResident <"+xfn.mePage+"> ?coResidentHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.coWorker+"> ?coWorker . " +
					"?page <"+xfn.coWorker+"-hyperlink> ?coWorkerHyperLink . " +
					"?coWorker <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?coWorker <"+xfn.mePage+"> ?coWorkerHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.crush+"> ?crush . " +
					"?page <"+xfn.crush+"-hyperlink> ?crushHyperLink . " +
					"?crush <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?crush <"+xfn.mePage+"> ?crushHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.date+"> ?date . " +
					"?page <"+xfn.date+"-hyperlink> ?dateHyperLink . " +
					"?date <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?date <"+xfn.mePage+"> ?dateHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.friend+"> ?friend . " +
					"?page <"+xfn.friend+"-hyperlink> ?friendHyperLink . " +
					"?friend <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?friend <"+xfn.mePage+"> ?friendHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.kin+"> ?kin . " +
					"?page <"+xfn.kin+"-hyperlink> ?kinHyperLink . " +
					"?kin <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?kin <"+xfn.mePage+"> ?kinHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.met+"> ?met . " +
					"?page <"+xfn.met+"-hyperlink> ?metHyperLink . " +
					"?met <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?met <"+xfn.mePage+"> ?metHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.muse+"> ?muse . " +
					"?page <"+xfn.muse+"-hyperlink> ?museHyperLink . " +
					"?muse <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?muse <"+xfn.mePage+"> ?museHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.neighbor+"> ?neighbor . " +
					"?page <"+xfn.neighbor+"-hyperlink> ?neighborHyperLink . " +
					"?neighbor <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?neighbor <"+xfn.mePage+"> ?neighborHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.parent+"> ?parent . " +
					"?page <"+xfn.parent+"-hyperlink> ?parentHyperLink . " +
					"?parent <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?parent <"+xfn.mePage+"> ?parentHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.spouse+"> ?spouse . " +
					"?page <"+xfn.spouse+"-hyperlink> ?spouseHyperLink . " +
					"?spouse <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?spouse <"+xfn.mePage+"> ?spouseHyperLink . "+
				"} " +
		"		 OPTIONAL{ ?person <"+xfn.sweetheart+"> ?sweetheart . " +
					"?page <"+xfn.sweetheart+"-hyperlink> ?sweetheartHyperLink . " +
					"?sweetheart <"+RDF.type+"> <"+foaf.Person+"> . " +
					"?sweetheart <"+xfn.mePage+"> ?sweetheartHyperLink . "+
				"} } }" +
		"			 } " ;
		
		key = "person";
		vars.add("page");
		vars.add("contactHyperLink");
//		vars.add("acquaintance");
		vars.add("acquaintanceHyperLink");
//		vars.add("child");
		vars.add("childHyperLink");
//		vars.add("colleague");
		vars.add("colleagueHyperLink");
//		vars.add("coResident");
		vars.add("coResidentHyperLink");
//		vars.add("coWorker");
		vars.add("coWorkerHyperLink");
//		vars.add("crush");
		vars.add("crushHyperLink");
//		vars.add("date");
		vars.add("dateHyperLink");
//		vars.add("friend");
		vars.add("friendHyperLink");
//		vars.add("kin");
		vars.add("kinHyperLink");
//		vars.add("met");
		vars.add("metHyperLink");
//		vars.add("muse");
		vars.add("museHyperLink");
//		vars.add("neighbor");
		vars.add("neighborHyperLink");
//		vars.add("parent");
		vars.add("parentHyperLink");
//		vars.add("spouse");
		vars.add("spouseHyperLink");
//		vars.add("sweetheart");
		vars.add("sweetheartHyperLink");
		
		
		Query query = QueryFactory.create(quStr);
		QueryExecution qE = QueryExecutionFactory.create(query, model);
		ResultSet rS = qE.execSelect();
		
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
