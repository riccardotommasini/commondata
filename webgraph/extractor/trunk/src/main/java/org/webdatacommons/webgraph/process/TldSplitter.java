package org.webdatacommons.webgraph.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.util.Progress;


public class TldSplitter {

	public static void main(String args[])
	{
		if(args.length!=3 && args.length!=4)
		{
			System.out.println("Parameters:");
			System.out.println("1. Task: [split|edges]");
			System.out.println("   split: creates tld-specific sub.graphs");
			System.out.println("   edges: removes all intra-tld edges");
			System.out.println("2. Index file");
			System.out.println("3. Network file");
			System.out.println("4. output file (edges only)");
			return;
		}
		
		TldSplitter splitter = new TldSplitter();
		
		try {
			if(args[0].equals("split"))
				splitter.Split(args[1], args[2]);
			else if(args[0].equals("edges"))
				splitter.RemoveIntraTldEdges(args[1], args[2], args[3]);
			else
				System.out.println("unknown parameter: " + args[0]);
			System.out.println("Done.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class Tld
	{
		public HashMap<Integer, Integer> Transformation = new HashMap<Integer, Integer>();
		public String TldName;
		
		private BufferedWriter indexWriter;
		private BufferedWriter transformationWriter;
		private BufferedWriter networkWriter;
		public void open(String baseFilename) throws IOException
		{
			File f = new File(baseFilename);
			
			File path = new File(f.getParent() + "/" + TldName + "/");
			path.mkdir();
			indexWriter = new BufferedWriter(new FileWriter(new File(path.getAbsolutePath() + "/index")));
			transformationWriter = new BufferedWriter(new FileWriter(new File(path.getAbsolutePath() + "/transformation")));
			networkWriter = new BufferedWriter(new FileWriter(new File(path.getAbsolutePath() + "/network")));
		}
		
		public void close() throws IOException
		{ 
			indexWriter.close();
			transformationWriter.close();
			networkWriter.close();
		}
		
		private Integer currentId=0;
		public void Write(Integer id, String pld) throws IOException
		{
			indexWriter.write(pld + "\t" + currentId + "\n");
			transformationWriter.write(id + "\t" + currentId + "\n");
			Transformation.put(id, currentId);
			currentId++;
		}
		
		public boolean ContainsEdge(Integer from, Integer to)
		{
			return Transformation.containsKey(from) && Transformation.containsKey(to);
		}
		
		public void WriteEdge(Integer from, Integer to) throws IOException
		{
			int i1,i2;
			i1 = Transformation.get(from);
			i2 = Transformation.get(to);
			
			networkWriter.write(i1 + "\t" + i2 + "\n");
		}
	}
	
	public void RemoveIntraTldEdges(String index, String network, String output) throws IOException
	{
		String[] tldList = new String[] { "com", "de", "net", "org", "co.uk", "nl", "ru", "info", "it", "com.br" };
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(index)))));
		ArrayList<Integer> tld = new ArrayList<Integer>();
		
		String line;
		Progress p = new Progress(1000000, "lines read (index) ...");
		
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			
			String url = values[0];
			
			// Identify TLD
			String tldName = null;
			int tldIndex=-1;
			for(int i=0;i<tldList.length;i++)
			{
				String s = tldList[i];
				if(url.endsWith("." + s))
				{
					tldName = s;
					tldIndex=i;
					break;
				}
			}
			if(tldName == null)
				tldName = "others";
			
			tld.add(tldIndex);
			
			p.IncrementAndPrint();
		}
		reader.close();
		
		reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(network)))));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(new File(output)))));
		p = new Progress(1000000, "edges processed ...");
		
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			
			int i1 = tld.get(Integer.parseInt(values[0]));
			int i2 = tld.get(Integer.parseInt(values[1]));
			
			if(i1!=i2)
			{
				writer.write(line + "\n");
			}
			
			p.IncrementAndPrint();
		}
		reader.close();
		writer.close();
	}
	
	public void Split(String PldIndex, String PldNetwork) throws IOException
	{
		HashMap<String, Tld> tlds = new HashMap<String, Tld>();
		
		String[] tldList = new String[] { "com", "de", "net", "org", "co.uk", "nl", "ru", "info", "it", "com.br" };
		
		// Split Index
		System.out.println("Splitting index ...");
		BufferedReader reader = new BufferedReader(new FileReader(new File(PldIndex)));
		String line;
		Tld tld;
		long cnt=0;
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			String url = values[0];
			int id = Integer.parseInt(values[1]);
			
			// Identify TLD
			String tldName = null;
			for(String s : tldList)
			{
				if(url.endsWith("." + s))
				{
					tldName = s;
					break;
				}
			}
			if(tldName == null)
				tldName = "others";
			//String tldName = DomainUtils.getTopLevelDomain(url);
			//String tldName = DomainUtils.getTopLevelDomainFromWholeURL(url);
			
			if(tlds.containsKey(tldName))
				tld = tlds.get(tldName);
			else
			{
				tld = new Tld();
				tld.TldName = tldName;
				tld.open(PldIndex);
				tlds.put(tldName, tld);
			}
			
			tld.Write(id, url);
			
			cnt++;
			
			if(cnt%100000==0)
				System.out.println(cnt + " lines processed ...");
		}
		reader.close();
		
		// Split Network
		System.out.println("Splitting network ...");
		reader = new BufferedReader(new FileReader(new File(PldNetwork)));
		cnt = 0;
		while((line = reader.readLine()) != null)
		{
			String[] values = line.split("\t");
			int i1,i2;
			i1 = Integer.parseInt(values[0]);
			i2 = Integer.parseInt(values[1]);
			
			for(String key : tlds.keySet())
			{
				if(tlds.get(key).ContainsEdge(i1, i2))
					tlds.get(key).WriteEdge(i1, i2);
			}
			
			cnt++;
			
			if(cnt%100000==0)
				System.out.println(cnt + " lines processed ...");
		}
		
		reader.close();
		
		for(String key : tlds.keySet())
			tlds.get(key).close();
	}
}
