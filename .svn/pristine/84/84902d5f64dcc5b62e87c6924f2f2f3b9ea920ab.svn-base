package org.webdatacommons.webgraph.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.uni_mannheim.informatik.dws.dwslib.util.Progress;


public abstract class FileTransformation {

	public void Transform(String input, String output, Progress p) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(new File(input)));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
		
		initialize();
		
		String line;
		if(p==null)
			p = new Progress(100000, "lines read ...");
		
		String header = createHeader();
		if(header!=null)
			writer.write(header + "\n");
		
		while((line = reader.readLine()) != null)
		{
			String value = transformValue(line);
			if(value!=null)
				writer.write(value + "\n");
			p.IncrementAndPrint();
		}
		
		reader.close();
		writer.close();
	}
	
	public void initialize()
	{
	}
	public abstract String createHeader();
	public abstract String transformValue(String input);
}
