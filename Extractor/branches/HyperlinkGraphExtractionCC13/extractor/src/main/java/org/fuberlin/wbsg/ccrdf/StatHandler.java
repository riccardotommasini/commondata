package org.fuberlin.wbsg.ccrdf;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 
 * @author Hannes Mühleisen
 *
 */
public interface StatHandler {
	public void addStats(String key, Map<String, String> data);

	public void flush();
}

class LoggingStatHandler implements StatHandler {
	private static Logger log = Logger.getLogger(LoggingStatHandler.class);

	@Override
	public void addStats(String key, Map<String, String> data) {
		log.debug(key + ": " + data);
	}

	@Override
	public void flush() {
	}

}


