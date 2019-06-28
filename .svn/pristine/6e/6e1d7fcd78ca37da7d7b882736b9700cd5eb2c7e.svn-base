package org.webdatacommons.webgraph.statistics;

public class Degree {
		long in = 0;
		long out = 0;
		long num = 0;
		long inunw = 0;
		long outunw = 0;
		
		public static String getCSVHeader(){
			return "IN\tINUNW\tOUT\tOUTUNW\tPLDCNT";
		}
		
		public void addOutUnw(long out){
			this.outunw += out;
		}
		
		public void addInUnw(long in){
			this.inunw += in;
		}
		
		public void addIn(long in){
			this.in += in;
		}
		
		public void addOut(long out){
			this.out += out;
		}
		
		public void addNum(long num){
			this.num += num;
		}
		
		public String toCSVString(){
			return in + "\t" + inunw + "\t" + out + "\t" + outunw + "\t" + num;
		}

		
		
		

}
