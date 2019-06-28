package edu.kit.aifb.webdatacommons.hadoop.CSVGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.parser.NxParser;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class CSVGeneratorMapper extends Mapper<Text, Text, Text, Text>{
	
	
	@Override
	 public void map(Text cNode, Text tx, Context context) {
	
			NxParser nxp = new NxParser( new ByteArrayInputStream(tx.getBytes()) );
			
			String cl = context.getConfiguration().get("edu.kit.aifb.lookingfor");
			
			
			Model model = ModelFactory.createDefaultModel();
			String modelStr = "";
			
			//terminate context node
			while(nxp.hasNext()){
				Node[] n = nxp.next();
				Node [] n3 = new Node[3];
				n3[0]=n[0];
				n3[1]=n[1];
				n3[2]=n[2];
				modelStr+= Nodes.toN3(n3)+"\n" ; 
			}
			
		try{
			model.read(new StringReader(modelStr), null, "TURTLE"); //read in model
			
	
			if("hcalendar".equals(cl)){
				HCalendarFactory.commit(context, model, cNode.toString());
			} else if("geo".equals(cl)){
				GeoFactory.commit(context, model, cNode.toString() );
			}else if("hcard".equals(cl)){
				HCardFactory.commit(context, model, cNode.toString() );
			}else if("hlisting".equals(cl)){
				HListingFactory.commit(context, model, cNode.toString() );
			}else if("hrecipe".equals(cl)){
				HRecipeFactory.commit(context, model, cNode.toString() );
			}else if("hresume".equals(cl)){
				HResumeFactory.commit(context, model, cNode.toString() );
			}else if("hreview".equals(cl)){
				HReviewFactory.commit(context, model, cNode.toString() );
			}else if("species".equals(cl)){
				SpeciesFactory.commit(context, model, cNode.toString() );
			}else if("xfn".equals(cl)){
				XfnFactory.commit(context, model, cNode.toString() );
			}
			
	
	}catch(Exception e){
				
	}
}
	
}
