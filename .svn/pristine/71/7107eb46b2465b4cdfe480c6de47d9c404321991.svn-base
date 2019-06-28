package org.webdatacommons.webgraph.extraction.pig;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;

/**
 * Copy from loddesc lib
 * 
 */
public class TopLevelDomain extends EvalFunc<String> {
	
	@Override
	public String exec(Tuple arg0) throws IOException {
		return DomainUtil.getTopLevelDomainFromWholeURL((String) arg0.get(0));
	}

}
