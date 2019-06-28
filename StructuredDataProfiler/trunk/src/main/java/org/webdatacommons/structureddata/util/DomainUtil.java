package org.webdatacommons.structureddata.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.net.InternetDomainName;

public class DomainUtil {

	public static String INVALID_URL = null;

	/**
	 * This class gets the PLD from an URL and makes sure that all blogspot.com urls are
	 * mapped to the blogspot.com pld.
	 * @param url the URL
	 * @return the PLD
	 */
	public static String getPayLevelDomainFromWholeURL(String url) {
		String domain = getDomain(url);
		try {
			InternetDomainName fullDomainName = InternetDomainName.from(domain);
			// This is a necessary fix to guarantee blogspot.com is one PLD and
			// not millions
			String pld = fullDomainName.topPrivateDomain().toString();
			if (pld.endsWith("blogspot.com")) {
				pld = "blogspot.com";
			}
			return pld;
		} catch (Exception e) {

		}
		return INVALID_URL;
	}

	private static final Pattern DOMAIN_PATTERN = Pattern.compile("http(s)?://(([a-zA-Z0-9-_]+(\\.)?)+)");

	private static String getDomain(String uri) {
		try {
			Matcher m = DOMAIN_PATTERN.matcher(uri);
			if (m.find()) {
				return m.group(2);
			}
		} catch (Exception e) {

		}
		return uri;
	}

}
