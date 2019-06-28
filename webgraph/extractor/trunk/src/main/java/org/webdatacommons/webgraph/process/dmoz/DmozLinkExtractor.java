package org.webdatacommons.webgraph.process.dmoz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;

public class DmozLinkExtractor {

	public static void main(String[] args)
	{
		if(args.length!=5)
		{
			System.out.println("Illegal number of arguments (" + args.length + ")");
			
			System.out.println("Arguments:");
			System.out.println("1. input file");
			System.out.println("2. output file");
			System.out.println("3. repair input file [true|false]");
			System.out.println("4. index file");
			System.out.println("5. Url-mode [pld|sd|page]");
			return;
		}
		
		if(!new File(args[0]).exists())
		{
			System.out.println("Input file does not exist!");
			return;
		}
		
		boolean bRepair = Boolean.parseBoolean(args[2]);
		
		Extract(args[0], args[1], bRepair, args[3], args[4]);
		
	}
	
	public static void Extract(String inputFile, String outputFile, boolean repair, String indexFile, String urlMode)
	{
		DmozLinkExtractor d = new DmozLinkExtractor();
				
		try {
			if(repair)
			{
				System.out.println("Repairing input file ...");
				String repairedFile = inputFile + "repaired.rdf";
				d.RepairFile(inputFile, repairedFile);
				
				inputFile = repairedFile;
			}
			
			// FindMatches won't work with big files ...
			//System.out.println("Extracting URLs from file...");
			//ArrayList<String> values = d.FindMatches(inputFile, topic);
			
			System.out.println("Reading URLs ...");
			ArrayList<String> values = d.ReadList(inputFile);
			
			if(urlMode.equals("pld"))
			{
				System.out.println("Extracting PLDs from URLs ...");
				values = d.GetPLD(values);
			}
			else if(urlMode.equals("sd"))
			{
				System.out.println("Extracting SDs from URLs ...");
				values = d.GetSD(values);
			}
			else if(urlMode.equals("page"))
			{
				
			}
			
			
			System.out.println("Making list distinct ...");
			values = d.Distinct(values);
			
			System.out.println("Sorting ...");
			values = d.Sort(values);
			
			System.out.println("Writing URL list ...");
			File out = new File(outputFile);
			BufferedWriter writer = new BufferedWriter(new FileWriter(out));
			
			for(String s : values)
			{
				writer.write(s + "\n");
			}
			writer.close();
			
			System.out.println("Loading IDs from index ...");
			values = d.GetIds(values, indexFile, urlMode.equals("page"));
			
			System.out.println("Writing ID list ...");
			writer = new BufferedWriter(new FileWriter(new File(outputFile + "_ids")));
			
			for(String s : values)
			{
				writer.write(s + "\n");
			}
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		System.out.println("done.");
	}
	
	ArrayList<String> GetIds(ArrayList<String> urls, String indexFile, boolean uncompress) throws IOException
	{
		File[] files;
		
		if(new File(indexFile).isDirectory())
		{
			files = new File(indexFile).listFiles(new FilenameFilter() {
				
				public boolean accept(File dir, String name) {
					return name.endsWith(".gz");
				}
			});
		}
		else
			files = new File[] { new File(indexFile) };
		
		ArrayList<String> result = new ArrayList<String>();
		
		for(File f : files)
		{
			try
			{
				System.out.println("Index file: " + f.getName());
				// Index format: url\tid
				BufferedReader reader;
				if(f.getName().endsWith(".gz"))
					reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
				else
					reader = new BufferedReader(new FileReader(f));
				
				String line;
				
				long cnt = 0;
				int urlIndex = 0;
				while((line = reader.readLine()) != null && urls.size() > urlIndex)
				{
					String[] values = line.split("\t");
					String url = values[0];
					if(uncompress)
						url = DomainUtil.uncompress(url);
					// URL list and index are both sorted
					int comp = urls.get(urlIndex).compareTo(url);
					// adjust index for url-list:
					// if a URL is not found, this url must be skipped in all following iterations
					while(comp < 0)
					{
						if((urlIndex+1) >= urls.size())
							break;
						comp = urls.get(++urlIndex).compareTo(url);
					}
		
					if(comp==0)
					{
						result.add(values[1]);
						urls.remove(urlIndex);
					}
					
					cnt++;
					
					if(cnt % 1000000 == 0)
						System.out.println(cnt + " lines processed.");
				}
				
				reader.close();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return result;
	}
	
	ArrayList<String> GetPLD(ArrayList<String> urls)
	{
		ArrayList<String> result = new ArrayList<String>();
		
		for(String s : urls)
		{
			String pld = DomainUtil.getPayLevelDomainFromWholeURL(s);
			
			if(pld!=null)
				result.add(pld);
		}
		
		return result;
	}
	
	ArrayList<String> GetSD(ArrayList<String> urls)
	{
		ArrayList<String> result = new ArrayList<String>();
		
		for(String s : urls)
		{
			String pld = DomainUtil.getSubDomainFromWholeUrl(s);
			
			if(pld!=null)
				result.add(pld);
		}
		
		return result;
	}
	
	ArrayList<String> Distinct(ArrayList<String> urls)
	{
		HashSet<String> set = new HashSet<String>();
		
		for(String s : urls)
		{
			if(!set.contains(s))
				set.add(s);
		}
		
		ArrayList<String> result = new ArrayList<String>();
		for(String s : set)
		{
			result.add(s);
		}
		
		return result;
	}
	
	ArrayList<String> Sort(ArrayList<String> values)
	{
		Collections.sort(values);
		
		return values;
	}
	
	ArrayList<String> ReadList(java.lang.String inputFile) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(new File(inputFile)));
		
		ArrayList<String> lst = new ArrayList<String>();
		java.lang.String line;
		
		while((line = reader.readLine()) != null)
		{
			if(!line.isEmpty())
				lst.add(line);
		}
	
		reader.close();
		
		return lst;
	}
	
	void RepairFile(String dmozFile, String output) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(new File(dmozFile)));
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(output)), "UTF8"); 
		//BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		
		String line;
		
		
		java.util.regex.Pattern regex = java.util.regex.Pattern.compile("&#[^;]+;");
		 
		while((line = reader.readLine()) != null)
		{			
			java.util.regex.Matcher matcher = regex.matcher(line);
			
			while(matcher.find())
			{
				System.out.println("Replaced: " + matcher.group());
				System.out.println(" at: " + line);
			}
			line = matcher.replaceAll("");
			
			//line = line.replaceAll("&.+;", "");
			
			writer.write(line + "\n");
		}
		
		reader.close();
		writer.close();
	}
	
	ArrayList<String> FindMatches(String dmozFile, String topic)
		      throws ParserConfigurationException, SAXException, IOException,
		      XPathExpressionException {
		InputStream in = new FileInputStream(new File(dmozFile));
	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    docFactory.setNamespaceAware(false);
	    docFactory.setValidating(false);
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();	    
	    
	    docBuilder.setEntityResolver(new EntityResolver() {
			
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				// TODO Auto-generated method stub
				return null;
			}
		});
	    Document doc = docBuilder.parse(in);
	    
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    String xpathExpr = "//Topic[starts-with(@id,\"" + topic + "\")]/link/@resource";;
	    //String xpathExpr = "/dmoz:RDF/dmoz:Topic[starts-with(@r:id,\"" + topic + "\")]/dmoz:link/@r:resource";
	    
	    NamespaceContext context = new NamespaceContextMap(
	            "r", "http://www.w3.org/TR/RDF/", 
	            "d", "http://purl.org/dc/elements/1.0/",
	            "dmoz", "http://dmoz.org/rdf/");
	    
	    xpath.setNamespaceContext(context);
	    
	    XPathExpression expr = xpath.compile(xpathExpr);

	    NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

	    ArrayList<String> result = new ArrayList<String>();
	    
	    for (int i = 0; i < nodeList.getLength(); ++i) {
	        Node node = nodeList.item(i);
	        result.add(node.getNodeValue());
	      }

	    return result;
	}

}
