package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.deri.any23.vocab.HRECIPE;
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

public class HRecipeFactory {
	
	public static void commit(Context cont, Model model, String cNode) throws IOException, InterruptedException {
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
			line += "\""+cNode+"\",";
			cont.write(keyT, new Text(line.replace(System.getProperty("line.separator"), "")) );
		}
	}
}
