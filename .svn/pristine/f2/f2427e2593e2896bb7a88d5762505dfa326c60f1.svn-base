package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.io.Text;
import org.deri.any23.extractor.html.HCardName;
import org.deri.any23.vocab.DCTERMS;
import org.deri.any23.vocab.DOAC;
import org.deri.any23.vocab.FOAF;
import org.deri.any23.vocab.HLISTING;
import org.deri.any23.vocab.HRECIPE;
import org.deri.any23.vocab.ICAL;
import org.deri.any23.vocab.REVIEW;
import org.deri.any23.vocab.VCARD;
import org.deri.any23.vocab.WO;
import org.deri.any23.vocab.XFN;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;



public class Tester {
	static public void main(String args[]) throws FileNotFoundException, ParseException, IOException{
		
		NxParser nxp = new NxParser(new GZIPInputStream (new FileInputStream("/Users/fubar/downloads/_user_sstadtmueller_wdc_species_ccrdf.html-mf-species.0.nq.gz")));
		Model model = ModelFactory.createDefaultModel();
		String modelStr = "";
		String cont = "";
		int i = 0;
		while(nxp.hasNext()){	
			Node[] n = nxp.next();
			
			if(cont.equals("")){
				cont = n[3].toString();
			}
			
			Node [] n3 = new Node[3];
			n3[0]=n[0];
			n3[1]=n[1];
			n3[2]=n[2];
			
			if(cont.equals(n[3].toString())){
				modelStr+= Nodes.toN3(n3)+"\n" ;
			}else{
				i++;
				//if(i>20) break;
				//model.write(System.out, "TURTLE");
				cont = n[3].toString();
				model.read(new StringReader(modelStr), null, "TURTLE");
				buildCSV_spe(model);
				model = ModelFactory.createDefaultModel();
				modelStr = Nodes.toN3(n3)+ "\n";
			}
			
		}
		
		
	}
	
	public static void buildCSV_hCard(Model model){
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
			System.out.println(keyT.toString() + "    "+ line.replace(System.getProperty("line.separator"), ""));
		}
	}
	
	public static void buildCSV_xfn(Model model){
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
			System.out.println(keyT.toString() + "    "+ line.replace(System.getProperty("line.separator"), ""));
		}
	}
	
	public static void buildCSV_spe(Model model){
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
			System.out.println(keyT.toString() + "    "+ line.replace(System.getProperty("line.separator"), ""));
		}
	}
	
	public static void buildCSV_hRes(Model model){
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
			System.out.println(keyT.toString() + "    "+ line.replace(System.getProperty("line.separator"), ""));
		}
	}
	
	
	
	public static void buildCSV_hrec(Model model){
		String quStr = "";
		List<String> vars = new ArrayList<String>();
		String key = "";
		
		HRECIPE rec = HRECIPE.getInstance();

		quStr = ""+

		"SELECT * " +
		"WHERE { ?hrecipe <"+RDF.type+"> <"+rec.Recipe+"> . " +
		"		 OPTIONAL{ ?hrecipe <"+rec.fn+"> ?name . } " +
		"		 OPTIONAL{ ?hrecipe <"+rec.ingredient+"> ?ingredient . " +
				"OPTIONAL{ ?ingredient <"+rec.ingredientName+"> ?ingredientName . } " +
				"OPTIONAL{ ?ingredient <"+rec.ingredientQuantity+"> ?ingredientQuantity . } " +
				"} " +
		"		 OPTIONAL{ ?hrecipe <"+rec.yield+"> ?yield . } " +
		"		 OPTIONAL{ ?hrecipe <"+rec.instructions+"> ?instructions . } " +
		"		 OPTIONAL{ ?hrecipe <"+rec.duration+"> ?duration .  " +
				"OPTIONAL{ ?duration <"+rec.durationTime+"> ?durationTime . } " +
				"OPTIONAL{ ?duration <"+rec.durationTitle+"> ?durationTitle . } " +
				"} " +
		" 		 OPTIONAL{ ?hrecipe <"+rec.photo+"> ?photo . } " +
		"		 OPTIONAL{ ?hrecipe <"+rec.summary+"> ?summary . } " +
		" 		 OPTIONAL{ ?hrecipe <"+rec.author+"> ?author . } " +
		" 		 OPTIONAL{ ?hrecipe <"+rec.published+"> ?published . } " +
		" 		 OPTIONAL{ ?hrecipe <"+rec.nutrition+"> ?nutrition .  " +
				"OPTIONAL{ ?nutrition <"+rec.nutritionValue+"> ?nutritionValue . } " +
				"OPTIONAL{ ?nutrition <"+rec.nutritionValueType+"> ?nutritionValueType . } " +
				"} " +
		" 		 OPTIONAL{ ?hrecipe <"+rec.tag+"> ?tag . } " +
		"			 } " ;
		
		key = "hrecipe";
		vars.add("name");
		vars.add("ingredient");
		vars.add("ingredientName");
		vars.add("ingredientQuantity");
		vars.add("ingredientQuantityType");
		vars.add("yield");
		vars.add("instructions");
		vars.add("duration");
		vars.add("durationTime");
		vars.add("durationTitle");
		vars.add("photo");
		vars.add("summary");
		vars.add("author");
		vars.add("published");
		vars.add("nutrition");
		vars.add("nutritionValue");
		vars.add("nutritionValueType");
		vars.add("tag");
		
		
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
			System.out.println(keyT.toString() + "    "+ line.replace(System.getProperty("line.separator"), ""));
		}
	}
	
	public static void buildCSV_geo(Model model){
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
			System.out.println(keyT.toString() + "    "+ line.replace(System.getProperty("line.separator"), ""));
		}
	}
	
	public static void buildCSV_hrev(Model model){
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
			System.out.println(keyT.toString() + "    "+ line.replace(System.getProperty("line.separator"), ""));
		}
	}
	
	public static void buildCSV_hList(Model model){
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
			System.out.println(keyT.toString() + "    "+ line.replace(System.getProperty("line.separator"), ""));
		}
	}

	
	public static void buildCSV_hCal(Model model){
		String quStr = "";
		List<String> vars = new ArrayList<String>();
		String key = "";
		
		ICAL ical = ICAL.getInstance();
		quStr = ""+

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
		
		key = "hcalendar";
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
			System.out.println(keyT.toString() + "    "+ line.replace(System.getProperty("line.separator"), ""));
		}
	}
}
