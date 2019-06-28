package org.webdatacommons.webgraph.process;

import java.util.Date;

public class Starter {

	
	public static void main(String[] args) {
		System.out.println(new Date() + " Starting ...");
		NetworkCompressor nc = new NetworkCompressor(args[0], args[1]);
		if (args.length > 2){
			nc.setCores(Integer.parseInt(args[2]));
		}
		nc.start();
		System.out.println(new Date() + " Done.");
	}
}
