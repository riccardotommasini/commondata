package org.webdatacommons.structureddata.util;

public class ParsingUtil {

	public static String escapeLine(String line) {
		return line.replace("\r", " ").replace("\n", " ").replace("\t", " ");
	}

	public static String correctLanguageTag(String line) {
		return line.replaceFirst("(@[A-Za-z][A-Za-z])_([A-Za-z][A-Za-z])",
				"$1-$2");
	}

}
