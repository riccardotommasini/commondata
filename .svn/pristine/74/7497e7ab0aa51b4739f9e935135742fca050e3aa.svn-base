package org.webdatacommons.webgraph.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.EnumMap;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;


public class DataInput2Text {

	final static int IO_BUFFER_SIZE = 64 * 1024;

	private DataInput2Text() {}

	private static enum Kind { BYTE, SHORT, INT, LONG, FLOAT, DOUBLE };
	@SuppressWarnings("boxing")
	private static final EnumMap<Kind,Integer> kind2size = new EnumMap<Kind,Integer>( new Object2ObjectOpenHashMap<Kind,Integer>(
			new Kind[] { Kind.BYTE, Kind.SHORT, Kind.INT, Kind.LONG, Kind.FLOAT, Kind.DOUBLE }, new Integer[] {  1, 2, 4, 8, 4, 8 }
	) );
	
	private static void skipItems( DataInput in, int numberOfItems, int sizeOfItem ) throws IOException {
		if ( numberOfItems == 0 ) return;
		int toBeSkipped = sizeOfItem * numberOfItems;
		int skipped = ((RandomAccessFile)in).skipBytes( toBeSkipped );
		if ( skipped < toBeSkipped ) throw new EOFException();
	}

	public static void main( String[] arg ) throws IOException, JSAPException {
		SimpleJSAP jsap = new SimpleJSAP( DataInput2Text.class.getName(), "Converts a binary (DataOutput) file containing numbers to text format.",
				new Parameter[] {
					new UnflaggedOption( "binaryFile", JSAP.STRING_PARSER, "-", JSAP.REQUIRED, JSAP.NOT_GREEDY, "The input binary file." ),
					new UnflaggedOption( "textFile", JSAP.STRING_PARSER, "-", JSAP.REQUIRED, JSAP.NOT_GREEDY, "The output text file." ),
					new FlaggedOption( "kind", JSAP.STRING_PARSER, "int", JSAP.NOT_REQUIRED, 'k', "kind", "The data kind: byte, int, short, long, double, float." ),
					new FlaggedOption( "from", JSAP.INTEGER_PARSER, "0", JSAP.NOT_REQUIRED, 'f', "from", "Start from the given item (inclusive), 0-based." ),
					new FlaggedOption( "number", JSAP.LONG_PARSER, "0", JSAP.NOT_REQUIRED, 'n', "number", "Output at most n items, 0 meaning MaxInteger." ),
					new FlaggedOption( "step", JSAP.INTEGER_PARSER, "1", JSAP.NOT_REQUIRED, 's', "step", "Only output one out of step elements." ),
			});
		JSAPResult jsapResult = jsap.parse( arg );
		if ( jsap.messagePrinted() ) System.exit( 1 );
		
		Kind kind = Kind.INT;
		int fromItem = 0;
		long howManyItems = Integer.MAX_VALUE;
		int step = 1;
		boolean fromHasBeenSpecified = false, skipHasBeenSpecified = false;
		
		String kindString = jsapResult.getString( "kind" );
		if ( kindString.equalsIgnoreCase( "byte" ) ) kind = Kind.BYTE; 
		else if ( kindString.equalsIgnoreCase( "short" ) ) kind = Kind.SHORT; 
		else if ( kindString.equalsIgnoreCase( "int" ) ) kind = Kind.INT; 
		else if ( kindString.equalsIgnoreCase( "long" ) ) kind = Kind.LONG; 
		else if ( kindString.equalsIgnoreCase( "float" ) ) kind = Kind.FLOAT; 
		else if ( kindString.equalsIgnoreCase( "double" ) ) kind = Kind.DOUBLE; 
		else {
			System.err.println( "Kind is " + kindString + ", instead it must be one of: byte, int, short, long, float, double.");
			System.exit( 1 );
		}
		int sizeOfItem = kind2size.get( kind ).intValue();

		if ( jsapResult.userSpecified( "from" ) ) {
			fromItem = jsapResult.getInt( "from" );
			howManyItems = 1;
			fromHasBeenSpecified = true;
		}
		howManyItems = jsapResult.getLong( "number" );
		if ( howManyItems == 0 ) howManyItems = Integer.MAX_VALUE;
		if ( jsapResult.userSpecified( "step") ) {
			step = jsapResult.getInt( "step" );
			skipHasBeenSpecified = true;
		}

		String inFileName = jsapResult.getString( "binaryFile" ), outFileName = jsapResult.getString( "textFile" );
		
		DataInput in;
		if ( fromHasBeenSpecified || skipHasBeenSpecified ) {
				if ( inFileName.equals( "-" ) ) throw new IllegalArgumentException( "Cannot position on standard input" );
				in = new RandomAccessFile( inFileName, "r" );
				((RandomAccessFile)in).seek( sizeOfItem * fromItem );		
		} else 
			in = new DataInputStream( new BufferedInputStream( inFileName.equals( "-" ) ? System.in : new FileInputStream( inFileName ), IO_BUFFER_SIZE ) );
						
		PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter( outFileName.equals( "-" ) ? (OutputStream)System.out : new FileOutputStream( outFileName ), "UTF-8" ), IO_BUFFER_SIZE ) );	
				
		switch ( kind ) {
			case BYTE:
				try {
					while ( howManyItems-- > 0 ) { 
						out.println( in.readByte() );
						if ( step > 0 ) skipItems( in, step - 1, sizeOfItem );
					} 
				} catch ( EOFException e ) {}
				break;
			case SHORT:
				try {
					while ( howManyItems-- > 0 ) {
						out.println( in.readShort() );
						if ( step > 0 ) skipItems( in, step - 1, sizeOfItem );
					} 
				} catch ( EOFException e ) {}
				break;
			case INT:
				try {
					while ( howManyItems-- > 0 ) {
						out.println( in.readInt() ); 
						if ( step > 0 ) skipItems( in, step - 1, sizeOfItem );
					} 
				} catch ( EOFException e ) {}
				break;
			case LONG:
				try {
					while ( howManyItems-- > 0 ) {
						out.println( in.readLong() ); 
						if ( step > 0 ) skipItems( in, step - 1, sizeOfItem );
					} 
				} catch ( EOFException e ) {}
				break;
			case FLOAT:
				try {
					while ( howManyItems-- > 0 ) {
						out.println( in.readFloat() ); 
						if ( step > 0 ) skipItems( in, step - 1, sizeOfItem );
					} 
				} catch ( EOFException e ) {}
				break;
			case DOUBLE:
				try {
					while ( howManyItems-- > 0 ) {
						out.println( in.readDouble() );
						if ( step > 0 ) skipItems( in, step - 1, sizeOfItem );
					} 
				} catch ( EOFException e ) {}
				break;
		}
		
		out.close();
		
	}
}
