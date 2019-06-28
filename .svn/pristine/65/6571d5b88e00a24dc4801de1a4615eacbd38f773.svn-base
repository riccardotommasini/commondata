package org.webdatacommons.webgraph.extraction.pig;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pig.EvalFunc;
import org.apache.pig.PigWarning;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.NonSpillableDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;

public class CombineMaps extends EvalFunc<DataBag> {

	private final static TupleFactory tupleFactory = TupleFactory.getInstance();
	
	private static Logger log = Logger.getLogger("CombineMaps.class");

	/**
	 * Functions calculates a distinct list of PLDs from a given list of URLs.
	 * Internally a set is used. Input:
	 * tuple(bag{(chararray,bag{(url:chararray,cnt:number)})}) Output:
	 * bag{(chararray, integer)}
	 */
	@Override
	public DataBag exec(Tuple input) throws IOException {
		if (input == null || input.size() < 1) {
			return new NonSpillableDataBag(0);
		}
		try {
			DataBag inputBag = (DataBag) input.get(0);
			Iterator<Tuple> bagIter = inputBag.iterator();
			Map<String, Integer> plds = new TreeMap<String, Integer>();
			while (bagIter.hasNext()) {
				Tuple bagTuple = bagIter.next();
				if (bagTuple.size() < 2) {
					// if bag is too small skip
					continue;
				}
				DataBag urlBag = (DataBag) bagTuple.get(1);
				if (urlBag == null) {
					continue;
				}
				Iterator<Tuple> urlIter = urlBag.iterator();
				while (urlIter.hasNext()) {
					Tuple pldTuple = urlIter.next();
					if (pldTuple.size() < 2 && pldTuple.get(0) == null) {
						continue;
					}
					String pld = (String) pldTuple
									.get(0);
					if (pld != DomainUtil.INVALID_URL) {
						Integer count = (Integer) pldTuple.get(1);
						if (plds.containsKey(pld)) {
							count += plds.get(pld);
						}
						plds.put(pld, count);
					}
				}
			}
			DataBag ret = new NonSpillableDataBag(plds.size());
			for (String pld : plds.keySet()) {
				Tuple tp = tupleFactory.newTuple(2);
				tp.set(0, pld);
				tp.set(1, plds.get(pld));
				ret.add(tp);
			}
			return ret;
		} catch (ClassCastException ex) {
			warn("class cast exception at " + ex.getStackTrace()[0],
					PigWarning.UDF_WARNING_1);
			log.log(Level.WARNING, "class cast exception", ex.fillInStackTrace());
			return new NonSpillableDataBag(0);
		} catch (Exception e){
			warn("something went wrong " + e.getStackTrace()[0],
					PigWarning.UDF_WARNING_2);
			log.log(Level.WARNING, "something went wrong", e.fillInStackTrace());
			return new NonSpillableDataBag(0);
		} catch (Error er){
			warn("something went wrong with an error " + er.getStackTrace()[0],
					PigWarning.UDF_WARNING_3);
			log.log(Level.WARNING, "something went wrong with an error", er.fillInStackTrace());
			return new NonSpillableDataBag(0);
		}
	}

}
