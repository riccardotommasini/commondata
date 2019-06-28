package org.webdatacommons.webgraph.algo;

import java.util.List;

import org.webdatacommons.webgraph.algo.ParallelForEach.ParallelForEachThread;

public abstract class ParallelFor {

	public abstract void loop(int iterator);
	
	class ParallelForThread extends Thread
	{
		public ParallelFor parent;
		public int startIndex;
		public int endIndex;
		
		@Override
		public void run() {			
			for(int i=startIndex; i<endIndex;i++)
				parent.loop(i);
		}
	}
	
	public void Start(int numIterations, int numThreads) throws InterruptedException
	{
		int cnt = numIterations;
		
		if(numThreads==0)
			numThreads = Runtime.getRuntime().availableProcessors();
		
		ParallelForThread[] t = new ParallelForThread[numThreads];
		
		for(int i=0;i<numThreads;i++)
		{
			t[i] = new ParallelForThread();
			t[i].parent = this;
			t[i].startIndex = cnt / numThreads * i;
			t[i].endIndex = cnt / numThreads * (i+1);
		}
		
		System.out.println("Parallel For: Starting " + numThreads + " threads.");
		for(int i=0;i<numThreads;i++)
			t[i].run();
		
		for(int i=0;i<numThreads;i++)
			t[i].join();
	}
	
}
