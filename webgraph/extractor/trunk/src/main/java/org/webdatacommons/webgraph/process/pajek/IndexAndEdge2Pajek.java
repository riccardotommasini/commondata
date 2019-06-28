package org.webdatacommons.webgraph.process.pajek;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;


public class IndexAndEdge2Pajek {

	public static void main(String[] args) {
		if(args.length!=4)
		{
			System.out.println("parameters:");
			System.out.println("[1]: index-file");
			System.out.println("[2]: edge-file");
			System.out.println("[3]: output-file");
			System.out.println("[4]: (true/false) include vertex labels");
			return;
		}
		
		if(!new File(args[0]).exists())
		{
			System.out.println("index-file does not exist!");
			return;
		}
		
		if(!new File(args[1]).exists())
		{
			System.out.println("edge-file does not exist!");
			return;
		}
		
		if(new File(args[2]).exists())
		{
			System.out.println("output-file already exists!");
			return;
		}
		
		boolean b = Boolean.parseBoolean(args[3]);
		
		transform(args[0], args[1], args[2], b);
		
		System.out.println("finished.");
	}
	
	public static void transform(String indexFile, String edgeFile, String outputFile, boolean includeLabels)
	{
		try {
			// read index file & count vertices
			BufferedReader indexReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(indexFile)))));
			ArrayList<Vertex> vertices = readVertices(indexReader, includeLabels);
			indexReader.close();

			// write vertices
			BufferedWriter pajekWriter = new BufferedWriter(new FileWriter(new File(outputFile)));
			writeVertices(vertices, pajekWriter, includeLabels);
			vertices.clear();
			
			// read & write arcs
			BufferedReader edgeReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(edgeFile)))));
			copyArcs(edgeReader, pajekWriter);
			edgeReader.close();
			pajekWriter.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Vertex> readVertices(BufferedReader reader, boolean includeLabels) throws IOException
	{
		// input:
		// [name]\t[id]
		
		System.out.println("Reading vertices ...");
		long lineNo=0;
		
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		
		String line;
		while((line = reader.readLine()) != null)
		{			
			if(!line.trim().equals(""))
			{
				String[] values = line.split("\t");
			
				Vertex v = new Vertex();
				if(includeLabels)
					v.name = values[0];
				v.id = Long.parseLong(values[1])+1;
				
				vertices.add(v);
			}
			
			lineNo++;
			if(lineNo % 100000 == 0)
				System.out.println("\tprocessed " + lineNo + " lines.");
		}
		
		System.out.println("Done reading vertices.");
		
		return vertices;
	}
	
	public static void writeVertices(ArrayList<Vertex> vertices, BufferedWriter writer, boolean includeLabels) throws IOException
	{
		// output:
		// *Vertices [cnt]
		// [id] "[name]"
		
		System.out.println("Writing vertices ...");
		
		int lineNo=0;
		writer.write("*Vertices " + vertices.size() + "\n");
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i<vertices.size(); i++)
		{
			Vertex v = vertices.get(i);
			
			if(includeLabels)
			{
				sb.append(v.id);
				sb.append(" \"");
				sb.append(v.name);
				sb.append("\"\n");
				writer.write(sb.toString());
				sb.setLength(0);
			}
			else
				writer.write(v.id + "\n");
			
			lineNo++;
			if(lineNo % 100000 == 0)
				System.out.println("\tprocessed " + lineNo + " vertices.");
		}
		
		System.out.println("Done writing vertices.");
	}

	public static void copyArcs(BufferedReader reader, BufferedWriter writer) throws IOException
	{
		// input:
		// [from]\t[to]
		// output:
		// *Arcs :1 "[name]"
		// [from] [to]	
		
		String line, output;
		
		System.out.println("Copying arcs ...");
		
		long lineNo=0;
		
		writer.write("*Arcs :1 \"Arcs\"\n");
		
		StringBuilder sb = new StringBuilder();
		
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			
			sb.append(Long.parseLong(values[0])+1);
			sb.append(" ");
			sb.append(Long.parseLong(values[1])+1);
			if(values.length==3)
			{
				sb.append(" ");
				sb.append(values[2]);
			}
			
			writer.write(sb.toString());
			writer.newLine();
			
			sb.setLength(0);
			
			lineNo++;
			if(lineNo % 100000 == 0)
				System.out.println("\tProcessed " + lineNo + " arcs.");
		}
		
		System.out.println("Done copying arcs.");
	}
	
}
