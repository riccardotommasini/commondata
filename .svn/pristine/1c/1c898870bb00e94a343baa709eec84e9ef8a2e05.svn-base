package org.webdatacommons.structureddata.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONObject;
import org.webdatacommons.structureddata.Master;
import org.webdatacommons.structureddata.model.Entity;
import org.webdatacommons.structureddata.model.EntityFileLoader;
import org.webdatacommons.structureddata.model.ShallowEntity;
import org.webdatacommons.structureddata.util.DomainUtil;
import org.webdatacommons.structureddata.util.QuadSorter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.opencsv.CSVWriter;

import de.dwslab.dwslib.collections.MapUtils;
import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.models.SortingOrderTypes;
import de.dwslab.dwslib.util.io.InputUtil;
import de.dwslab.dwslib.util.io.OutputUtil;
import ldif.entity.NodeTrait;
import ldif.local.datasources.dump.QuadFileLoader;
import ldif.runtime.Quad;

/**
 
 * @author Anna Primpeli
 *
 */
@Parameters(commandDescription = "Converts the input nq files to other formats (JSON, CSV)")
public class WDCQuadConverter extends Processor<File> {

	@Parameter(names = { "-out",
			"-outputDir" }, required = true, description = "Folder where the outputfile(s) are written to.", converter = FileConverter.class)
	private File outputDirectory;

	@Parameter(names = { "-in",
			"-inputDir" }, required = true, description = "Folder where the input is read from.", converter = FileConverter.class)
	private File inputDirectory;

	@Parameter(names = "-threads", required = true, description = "Number of threads.")
	private Integer threads;

	@Parameter(names = { "-p",
			"-prefix" }, description = "Prefix of files in the input folder which will be processed.")
	private String filePrefix = "";

	@Parameter(names = { "-tp",
			"-typeProperties" }, required = true, description = "Properties which are used to identify a type (comma separated).", splitter = CommaParameterSplitter.class)
	private List<String> typeProperties = new ArrayList<String>();

	@Parameter(names = { "-e",
			"-typeAsRegex" }, required = false, description = "Indicates if the type properties should be handled as regex.")
	private boolean useRegex = false;
	
	@Parameter(names = { "-convert",
	"-convertOutputType" }, required = true, description = "Indicates which format the converted output will have. Supported formats: JSON, CSV")
	private String convertType = "";
	
	@Parameter(names = { "-density",
	"-propertyDensity" }, required = true, description = "Indicates the desires property density which the properties should have in order to be included in the property file")
	private Double propertyDensity = 0.0;
	
	@Parameter(names = { "-multiplePropValues",
	"-multiplePropertyValues" }, required = false, description = "Indicates if the converted result will contain all property values (true) for a certain subject or if one value per property is enough (false). ", arity = 1)
	private boolean multiplePropValues = true;
	


	
	@Override
	protected List<File> fillListToProcess() {
		List<File> files = new ArrayList<File>();
		System.out.println("List files to convert from:"+inputDirectory.getPath());
		for (File f : inputDirectory.listFiles()) {
			if (!f.isDirectory()) {
				if (filePrefix.length() > 0) {
					if (!f.getName().startsWith(filePrefix)) {
						continue;
					}
				}
				files.add(f);
			}
		}
		return files;
	}


	@Override
	protected int getNumberOfThreads() {
		return this.threads;
	}


	@Override
	protected void process(File object) throws Exception {
		
		HashMap<String, Integer> propertiesCount = new HashMap<String, Integer>();
		String fileName;
		
		fileName = object.getName();
		//first sort the file
		System.out.println("Sort File: "+fileName);
		object = sortFile(object);
		System.out.println("Sorted File Created: SORTED_"+fileName);

		System.out.println("Converting File");

		QuadFileLoader qfl = new QuadFileLoader();
		EntityFileLoader etl = new EntityFileLoader();
		BufferedReader br = InputUtil.getBufferedReader(object);
		
		String currentURL = "";
		NodeTrait currentSubject = null;
		List<ShallowEntity> entities = new ArrayList<ShallowEntity>();
		List<Quad> quads = new ArrayList<Quad>();
		
		while (br.ready()) {
			try{
				Quad q = qfl.parseQuadLine(br.readLine());
				if (q.graph().equals(currentURL)) {
					if (q.subject().equals(currentSubject)) {
						quads.add(q);
					} else {
						// create entity
						if (quads.size() > 0) {
							ShallowEntity e = etl.loadEntityFromQuadsForConversion(quads);	
							entities.add(e);							
							propertiesCount = updateProperties(e , propertiesCount);
						}
						// clear list
						quads.clear();
						quads.add(q);
						currentSubject = q.subject();
					}
				} else {
					// create entity
					if (quads.size() > 0) {
						ShallowEntity e = etl.loadEntityFromQuadsForConversion(quads);
						entities.add(e);
						propertiesCount = updateProperties(e , propertiesCount);

					}
					quads.clear();
					quads.add(q);
					currentSubject = q.subject();

					currentURL = q.graph();
				}
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
				
			}
		}
		// one final time:
		// create entity
		if (quads.size() > 0) {
			ShallowEntity e = etl.loadEntityFromQuadsForConversion(quads);
			entities.add(e);
			propertiesCount = updateProperties(e , propertiesCount);

		}
		processEntities(entities, propertiesCount, fileName);
		quads = null;
		entities = null;
		br.close();

	}
	

	
	private HashMap<String, Integer> updateProperties(ShallowEntity e, HashMap<String, Integer> propCount) {
		
		for (String p:e.getProperties().keySet()){
			Integer freq = propCount.get(p);
			if (null== freq){
				propCount.put(p, 1);
			}
			else
				propCount.put(p, freq+1);
		}
				
		return propCount;
	}


	protected void processEntities(List<ShallowEntity> entities, HashMap<String, Integer> propertiesCount, String fileName) {
	
		//maybe emit this part
//		double maxDensity = (double)getMaxDensity(predicates) / (double)entities.size();
//		System.out.println("The maximum density of properties found in the current file is:"+maxDensity);
//		System.out.println("Do you want to change your initial density value of: "+propertyDensity+" (y/n)");
//		Scanner reader = new Scanner(System.in);  // Reading from System.in
//		String a = reader.nextLine(); 
//		if (a.equals("y")){
//			System.out.println("Enter the desired density");
//			propertyDensity = reader.nextDouble(); 
//			System.out.println("Desired Density:"+propertyDensity);
//
//		}
//		reader.close();
	    ArrayList<String> toBeAppendedJSON = new ArrayList<String>();
		List<String[]> toBeAppendedCSV = new ArrayList<>();
		
		if(convertType.equalsIgnoreCase("JSON")){
			for (ShallowEntity e:entities){
				try {		
					e = shrinkEntity(e, entities.size(),propertiesCount);
					String entityAsJSON = convertToJSON(e);
					toBeAppendedJSON.add(entityAsJSON);
					
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			createFile(toBeAppendedJSON,toBeAppendedCSV, fileName);
		}
		else if (convertType.equalsIgnoreCase("CSV")){
			//get the header line
			HashSet<String> densePredicates = getDensePredicates(entities.size(),propertiesCount);
			
			String [] header = new String[densePredicates.size()+3];
			header [0] = "graph";
			header [1]="subject";
			header [2]="type";
			int headerPosition=3;
			for(String p:densePredicates){
				header[headerPosition]=p;
				headerPosition++;				
			}
			toBeAppendedCSV.add(header);

			for (ShallowEntity e:entities){
				
				e = shrinkEntity(e,  entities.size(), propertiesCount);

				String [] line = new String[densePredicates.size()+3];
				try {
					line [0]=e.getGraph();
					line [1]=e.getSubject().toString();
					line [2] = null == e.getType() ? "" : e.getType().toString();
					for (int i=3;i<line.length;i++){
						if (e.getProperties().containsKey(header[i])){
							if(multiplePropValues){
								for (String value: e.getProperties().get(header[i]))
									line[i]= null == line[i] ? value+"||||" : line[i] + value+"||||";
								line[i]= line[i].substring(0, line[i].length()-4);
							}
							else
								line[i]= null == line[i] ? e.getProperties().get(header[i]).get(0) : line[i] + e.getProperties().get(header[i]).get(0);
						}
						else line[i]="";
					}
					toBeAppendedCSV.add(line);

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			createFile(toBeAppendedJSON,toBeAppendedCSV, fileName);

		}
		else {
			System.out.println("Not supported conversion type:"+convertType+" You may choose between JSON and CSV");
			System.exit(0);
		}
		
		
	}

	private void createFile(ArrayList<String> toBeAppendedJSON, List<String[]> toBeAppendedCSV, String fileName) {
		try {
			BufferedWriter convertedWriter;
			
			String outputFile = convertType.equals("JSON") ? "CONVERTED_"+fileName.replace(".gz", ".json.gz") : "CONVERTED_"+fileName.replace(".gz", ".csv.gz");
			
			System.out.println("Intermediate sorted file will be deleted: SORTED_" +fileName);
			System.out.println("Writing conversion results in "+ outputFile);
			File file = new File (outputDirectory,"SORTED_"+fileName);
			file.delete();
			
			convertedWriter = OutputUtil.getGZIPBufferedWriter(new File(outputDirectory,
					(filePrefix.length() > 0 ? (filePrefix + ".") : ("")) + outputFile));
			
			if (convertType.equalsIgnoreCase("JSON")){

				convertedWriter.write("[ \n");
				for(int i=0;i<toBeAppendedJSON.size();i++){
					if(i==toBeAppendedJSON.size()-1) convertedWriter.write(toBeAppendedJSON.get(i) + "\n");
					else convertedWriter.write(toBeAppendedJSON.get(i) + ",\n");
				}
				convertedWriter.write("]");
				
				convertedWriter.close();

			}
			else if (convertType.equalsIgnoreCase("CSV")){
				CSVWriter writer = new CSVWriter(convertedWriter);
				writer.writeAll(toBeAppendedCSV);
				writer.close();
			}
			else {
				System.out.println("Not supported conversion type:"+convertType+" You may choose between JSON and CSV");
				System.exit(0);
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private Integer getMaxDensity(HashMap<String, Integer> predicates) {
		Map.Entry<String, Integer> maxEntry = null;

		for (Map.Entry<String, Integer> entry : predicates.entrySet())
		{
		    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
		    {
		        maxEntry = entry;
		    }
		}
		
		return maxEntry.getValue();
	}


	private HashSet<String> getDensePredicates(int size, HashMap<String, Integer> propertiesCount) {
		
		HashSet<String> densePredicates =new HashSet<String>();
		for(Entry<String, Integer> p: propertiesCount.entrySet()){
			double density= (double)propertiesCount.get(p.getKey())/(double)size;
			if (density>=propertyDensity)
				densePredicates.add(p.getKey());

		}
		return densePredicates;
	}


	/**
	 * @param e
	 * @param numberOfentities
	 * @return
	 * Shrinks the entity properties based on the density and on the propertyValues parameters
	 */
	private ShallowEntity shrinkEntity(ShallowEntity e, int numberOfentities, HashMap<String, Integer> propertiesCount) { 
		
		ShallowEntity shrinkedEntity = new ShallowEntity(e.getSubject(), e.getType(), e.getGraph());
		HashMap<String, List<String>> shrinkedProperties = new HashMap<String, List<String>>();

		for (Entry<String,List<String>> p: e.getProperties().entrySet()){
			double density= (double) propertiesCount.get(p.getKey())/ (double) numberOfentities;
			//check if the density of the current property is larger than what the user wants to have
			if (density>=propertyDensity){
				if(multiplePropValues) shrinkedProperties.put(p.getKey(), p.getValue());
				else shrinkedProperties.put(p.getKey(), new ArrayList<String>() {{add(p.getValue().get(0));}});
				}
		}
	
		shrinkedEntity.setProperties(shrinkedProperties);
		return shrinkedEntity;
	}


	private String convertToJSON(ShallowEntity e)  {
		
		try{
			
			Gson gson = new Gson();
			String json = gson.toJson(e);
			return json;
		}
		catch (Exception ex){
			System.out.println(ex.getMessage());
			return null;

		}		
	}


	private File sortFile(File object) throws FileNotFoundException, IOException{
		
		File output = new File(this.outputDirectory, "SORTED_"+object.getName());
		BufferedWriter bw = OutputUtil.getGZIPBufferedWriter(output);
		QuadFileLoader qfl = new QuadFileLoader();


		BufferedReader br = InputUtil.getBufferedReader(object);
		String currentURL = "";
		Map<String, ArrayList<Quad>> quadList = new HashMap<String, ArrayList<Quad>>();
		String line = "";

		while (br.ready()) {
			Quad q;
			try {
				line = br.readLine();
				q = qfl.parseQuadLine(line);
			} catch (Exception e) {
				//System.out.println(e.toString());
				continue;
			}

			if (q.graph().equals(currentURL)) {
				if (!quadList.containsKey(q.subject().value())) {
					quadList.put(q.subject().value(), new ArrayList<Quad>());
				}
				quadList.get(q.subject().value()).add(q);
			} else {
				if (quadList.size() > 0) {
					for (String s : quadList.keySet()) {
						for (Quad quad : quadList.get(s)) {
							bw.write(quad.toLine());
						}
					}
				}
				// re-init list
				quadList.clear();
				quadList.put(q.subject().value(), new ArrayList<Quad>());
				quadList.get(q.subject().value()).add(q);
				currentURL = q.graph();
			}
		}

		// one final time:
		if (quadList.size() > 0) {
			for (String s : quadList.keySet()) {
				for (Quad quad : quadList.get(s)) {
					bw.write(quad.toLine());
				}
			}
		}

		br.close();
		bw.close();
		return output;
	}
	/**
	 * Checks if the predicate is a type predicate
	 * 
	 * @param predicate
	 *            the predicate
	 * @return true if its a type predicate, false if not.
	 */
	private boolean isType(String predicate) {
		if (useRegex) {
			for (String pattern : typeProperties) {
				if (predicate.matches(pattern)) {
					return true;
				}
			}
		} else {
			if (typeProperties.contains(predicate)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void beforeProcess(){


	}
	

	@Override
	protected void afterProcess() {
		
		System.out.println("Done");

	}

	public static void main(String[] args)  {
		WDCQuadConverter cal = null;
		try {
			cal = new WDCQuadConverter();
			new JCommander(cal, args);
			cal.process();
		} catch (ParameterException pe) {
			pe.printStackTrace();
			new JCommander(cal).usage();
		}
	}

}
