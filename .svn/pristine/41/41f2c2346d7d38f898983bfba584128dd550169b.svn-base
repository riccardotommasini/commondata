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

public class ReduceUrlListToPldMap extends EvalFunc<DataBag> {

	private final static TupleFactory tupleFactory = TupleFactory.getInstance();

	private static Logger log = Logger.getLogger("ReduceUrlListToPldMap.class");

	@Override
	public DataBag exec(Tuple input) throws IOException {
		log.info("Reducing url list");
		if (input == null || input.size() < 1) {
			return new NonSpillableDataBag(0);
		} else {
			try {
				DataBag dataBag = (DataBag) input.get(0);
				Iterator<Tuple> bagIter = dataBag.iterator();
				Map<String, Integer> pldMap = new TreeMap<String, Integer>();
				while (bagIter.hasNext()){
					Tuple bagTuple = bagIter.next();
					if (bagTuple == null || bagTuple.size()<1){
						// skip if empty
						continue;
					}else{
						String url = (String) bagTuple.get(0);
						String pld = DomainUtil.getPayLevelDomainFromWholeURL(url);
						if (pld != null && !pld.equals(DomainUtil.INVALID_URL)){
							int count = 1;
							if (pldMap.containsKey(pld)){
								count += pldMap.get(pld);
							}
							pldMap.put(pld, count);
						}
					}
				}
				// if an pld is given we will remove it from the set
				if (input.size() > 1 && input.get(1) != null) {
					String pld = (String) input.get(1);
					log.info("PLD which will be removed is: " + pld);
					if (pld != null && pldMap.containsKey(pld)) {
						pldMap.remove(pld);
					}
				}
				DataBag ret = new NonSpillableDataBag(pldMap.size());
				for (String pld : pldMap.keySet()) {
					Tuple tp = tupleFactory.newTuple(2);
					tp.set(0, pld);
					tp.set(1, pldMap.get(pld));
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
}
