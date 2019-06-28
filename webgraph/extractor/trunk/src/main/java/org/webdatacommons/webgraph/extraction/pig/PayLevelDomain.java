package org.webdatacommons.webgraph.extraction.pig;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;

/**
 * Copy from loddesc lib
 * 
 */
public class PayLevelDomain extends EvalFunc<String> {
	private static Logger log = Logger.getLogger("PayLevelDomain.class");

	@Override
	public String exec(Tuple arg0) throws IOException {
		log.info("PayLevelDomain for " + (String) arg0.get(0));
		String pld = DomainUtil.getPayLevelDomainFromWholeURL((String) arg0
				.get(0));
		if (pld == null || pld.equals(DomainUtil.INVALID_URL)) {
			return (String) arg0.get(0);
		} else {
			return pld;
		}
	}

}
