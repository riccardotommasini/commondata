package org.webdatacommons.framework.io;

import java.util.Map;

/**
 * 
 * @author Hannes M�hleisen
 *
 */
public interface StatHandler {
	public void addStats(String key, Map<String, String> data);

	public void flush();
}
