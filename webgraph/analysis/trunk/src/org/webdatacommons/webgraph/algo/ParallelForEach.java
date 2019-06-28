package org.webdatacommons.webgraph.algo;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ParallelForEach {

	public abstract void loop(Object var);
	
	class ParallelForEachThread extends Thread
	{
		public ParallelForEach parent;
		public int startIndex;
		public int endIndex;
		public List list;
		public AtomicInteger next;
		
		@Override
		public void run() {			
			int n = next.getAndIncrement();
			
			while(n<endIndex)
			{
				parent.loop(list.get(n));
				n = next.getAndIncrement();
			}
			/*for(int i=startIndex; i<endIndex;i++)
				parent.loop(list.get(i));*/
		}
	}
	
	public void Start(List list, int numThreads) throws InterruptedException
	{
		int cnt = list.size();
		
		if(numThreads==0)
			numThreads = Runtime.getRuntime().availableProcessors();
		
		ParallelForEachThread[] t = new ParallelForEachThread[numThreads];
		
		AtomicInteger next = new AtomicInteger();
		next.set(0);
		
		for(int i=0;i<numThreads;i++)
		{
			t[i] = new ParallelForEachThread();
			t[i].parent = this;
			//t[i].startIndex = cnt / numThreads * i;
			//t[i].endIndex = cnt / numThreads * (i+1);
			t[i].endIndex = cnt;
			t[i].list = list;
			t[i].next = next;
		}
		
		System.out.println("Parallel For-Each: Starting " + numThreads + " threads.");
		for(int i=0;i<numThreads;i++)
			t[i].run();
		
		for(int i=0;i<numThreads;i++)
			t[i].join();
	}
	
	class ParallelForEachCollectionThread extends Thread
	{
		public ParallelForEach parent;
		public int startIndex;
		public int endIndex;
		//public Collection collection;
		public Object[] data;
		public AtomicInteger next;
		
		@Override
		public void run() {			
			int n = next.getAndIncrement();
			
			/*Iterator it = collection.iterator();
			int i=0;*/
			
			while(n<endIndex)
			{
				/*while(i++<n)
					it.next();*/
				
				//parent.loop(it.next());
				parent.loop(data[n]);
				n = next.getAndIncrement();
			}
			
			
			
			/*int i=0;
			Iterator it = collection.iterator();
			while(i++<startIndex)
				it.next();
			
			while(i++<endIndex)
				parent.loop(it.next());*/
			
			System.out.println("Parallel For-Each: Thread finished.");
		}
	}
	
	public void Start(Collection collection, int numThreads) throws InterruptedException
	{
		int cnt = collection.size();
		Object[] data = collection.toArray();
		
		if(numThreads==0)
			numThreads = Runtime.getRuntime().availableProcessors();
		
		ParallelForEachCollectionThread[] t = new ParallelForEachCollectionThread[numThreads];
		
		AtomicInteger next = new AtomicInteger();
		next.set(0);
		
		for(int i=0;i<numThreads;i++)
		{
			t[i] = new ParallelForEachCollectionThread();
			t[i].parent = this;
			//t[i].startIndex = cnt / numThreads * i;
			//t[i].endIndex = cnt / numThreads * (i+1);
			t[i].endIndex = cnt;
			//t[i].collection = collection;
			t[i].data = data;
			t[i].next = next;
		}
		
		System.out.println("Parallel For-Each: Starting " + numThreads + " threads.");
		for(int i=0;i<numThreads;i++)
			t[i].run();
		
		for(int i=0;i<numThreads;i++)
			t[i].join();
	}
	
}
