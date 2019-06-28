package org.webdatacommons.webgraph.extraction.pig;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pig.EvalFunc;
import org.apache.pig.PigWarning;
import org.apache.pig.data.Tuple;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;

public class CompressUrl extends EvalFunc<String> {

	private static Logger log = Logger.getLogger("CompressUrl.class");

	@Override
	public String exec(Tuple urlTuple) throws IOException {
		try {
			if (urlTuple == null || urlTuple.size() < 1 || urlTuple.get(0) == null) {
				return null;
			} else {
				String url = (String) urlTuple.get(0);
				return DomainUtil.compress(url);
			}
		} catch (ClassCastException e) {
			warn("Could not cast " + e.getStackTrace()[0],
					PigWarning.UDF_WARNING_1);
			log.log(Level.WARNING, "Could not cast", e.fillInStackTrace());
			return null;
		} catch (Error er) {
			warn("something went wrong with an error " + er.getStackTrace()[0],
					PigWarning.UDF_WARNING_3);
			log.log(Level.WARNING, "something went wrong with an error",
					er.fillInStackTrace());
			return null;
		}

	}

}
