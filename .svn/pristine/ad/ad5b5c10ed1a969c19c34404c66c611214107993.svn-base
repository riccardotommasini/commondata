package org.webdatacommons.webgraph.algo;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.util.ArrayList;
import java.util.HashSet;

public class DegreeCache {

	public ArrayList<Integer> in_degree;
	public ArrayList<Integer> out_degree;
	ImmutableGraph graph, transpose;
	HashSet<Integer> eliminated;
	ProgressLogger pl;
	
	public DegreeCache(final ImmutableGraph graph, final ImmutableGraph transpose, HashSet<Integer> eliminated, final ProgressLogger pl)
	{
		this.graph = graph;
		this.transpose = transpose;
		this.eliminated = eliminated;
		this.pl = pl;
	}
	
	public void ReadDegrees()
	{
		in_degree = new ArrayList<Integer>(graph.numNodes());
		out_degree = new ArrayList<Integer>(graph.numNodes());
		
		for(int i=0;i<graph.numNodes();i++)
		{
			in_degree.add(0);
			out_degree.add(0);
		}
		
		pl.expectedUpdates = graph.numNodes();
		pl.start("Reading degrees ...");
		pl.info = "Read degree";
		
		try {
			new ParallelFor() {
				
				@Override
				public void loop(int iterator) {
					in_degree.set(iterator, graph.outdegree(iterator));
					out_degree.set(iterator, transpose.outdegree(iterator));
					pl.update();
				}
			}.Start(graph.numNodes(), 0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pl.done();
	}
	
	public void RemoveNode(int id)
	{
		LazyIntIterator it = graph.successors(id);
		
		int degree = graph.outdegree(id);
		int link = it.nextInt();
		int cnt = 0;
		
		while(link!=-1 && cnt<degree)
		{
			if(!eliminated.contains(link))
			{
				in_degree.set(link, in_degree.get(link)-1);
			}
			link = it.nextInt();
			cnt++;
		}
		
		it = transpose.successors(id);
		degree = graph.outdegree(id);
		cnt = 0;
		link = it.nextInt();
		
		while(link!=-1 && cnt<degree)
		{
			if(!eliminated.contains(link))
			{
				out_degree.set(link, out_degree.get(link)-1);
			}
			link = it.nextInt();
			cnt++;
		}
		
		eliminated.add(id);
	}
	
}
