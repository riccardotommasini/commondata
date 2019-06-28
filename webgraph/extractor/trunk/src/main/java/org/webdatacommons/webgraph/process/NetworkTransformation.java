package org.webdatacommons.webgraph.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class NetworkTransformation {

	public String file;
	private BufferedReader reader;
	public boolean IsEOF;
	public int OriginalId;
	public int TransformedId;
	
	public NetworkTransformation(String transformationFile) throws FileNotFoundException
	{
		file = transformationFile;
		reader = new BufferedReader(new FileReader(new File(file)));
		IsEOF = false;
	}
	
	public boolean Read() throws IOException
	{
		if(!IsEOF)
		{
			String line = reader.readLine();
			
			if(line == null)
			{
				IsEOF = true;
				reader.close();
				return false;
			}
			else
			{
				String[] values = line.split("\t");
				OriginalId = Integer.parseInt(values[0]);
				TransformedId = Integer.parseInt(values[1]);
				return true;
			}
		}
		else
			return false;
	}
}
