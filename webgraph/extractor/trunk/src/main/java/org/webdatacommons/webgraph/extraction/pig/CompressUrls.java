package org.webdatacommons.webgraph.extraction.pig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pig.EvalFunc;
import org.apache.pig.PigWarning;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.NonSpillableDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;

public class CompressUrls extends EvalFunc<DataBag> {

	private final static TupleFactory tupleFactory = TupleFactory.getInstance();
	private static Logger log = Logger.getLogger("CompressUrls.class");

	@Override
	public DataBag exec(Tuple input) throws IOException {
		try {
			if (input == null || input.size() < 1 || input.get(0) == null) {
				return new NonSpillableDataBag(0);
			} else {
				DataBag inputBag = (DataBag) input.get(0);
				Iterator<Tuple> bagIter = inputBag.iterator();
				List<String> urlList = new ArrayList<String>();
				while (bagIter.hasNext()) {
					Tuple pldTuple = bagIter.next();
					if (pldTuple == null || pldTuple.size() < 1 || pldTuple.get(0) == null) {
						continue;
					} else {
						String url = (String) pldTuple.get(0);
						urlList.add(DomainUtil.compress(url));
					}
				}
				DataBag ret = new NonSpillableDataBag(urlList.size());
				for (String newUrl : urlList) {
					Tuple tp = tupleFactory.newTuple(1);
					tp.set(0, newUrl);
					ret.add(tp);
				}
				return ret;
			}
		} catch (Error er) {
			warn("something went wrong with an error " + er.getStackTrace()[0],
			PigWarning.UDF_WARNING_3);
			log.log(Level.WARNING, "something went wrong with an error",
					er.fillInStackTrace());
			return new NonSpillableDataBag(0);
		}
	}

}
