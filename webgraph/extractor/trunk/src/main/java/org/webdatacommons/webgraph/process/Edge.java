package org.webdatacommons.webgraph.process;

import java.util.ArrayList;

public class Edge implements Comparable
{
	public int from;
	public int to;
	
	public Edge(int from, int to)
	{
		this.from = from;
		this.to = to;
	}
	
	public boolean IsContained(ArrayList<Edge> edges)
	{
		for(int i=0;i<edges.size();i++)
			if(edges.get(i).from == from && edges.get(i).to==to)
				return true;
		return false;
	}

	public int compareTo(Object o) {
		Edge e = (Edge) o;
		
		if(e.from!=from)
			return Integer.compare(from, e.from);
		else
			return Integer.compare(to, e.to);
	}
	
	@Override
	public boolean equals(Object obj) {
		Edge e = (Edge) obj;
		return from==e.from && to==e.to;
	}
}
