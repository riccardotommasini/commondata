package org.webdatacommons.webgraph.extraction.pig;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.pig.EvalFunc;
import org.apache.pig.PigWarning;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.NonSpillableDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;

/**
 * Class implements the functionality to calculate from a bag of tuples
 * including a bag of URLs a unique set of PLDs based on the input URLs. The
 * function expects as input a tuple containing a bag of tuples (index 0) and
 * optional a PLD (index 1) which will be removed from the final distinct list
 * of PLDs. This can be used to remove self-links. Input looks like:
 * tuple(bag{(chararray, bag{(url:chararray)})}, chararray). The output is
 * returned sorted.
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 */
public class ExtractUniqueWebPlds extends EvalFunc<DataBag> {

	private final static TupleFactory tupleFactory = TupleFactory.getInstance();

	/**
	 * Functions calculates a distinct list of PLDs from a given list of URLs.
	 * Internally a set is used. Input:
	 * tuple(bag{(chararray,bag{(url:chararray)})}, chararray) Output:
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
					if (pldTuple.get(0) == null) {
						continue;
					}
					String pld = DomainUtil
							.getPayLevelDomainFromWholeURL((String) pldTuple
									.get(0));
					if (pld != DomainUtil.INVALID_URL) {
						int count = 1;
						if (plds.containsKey(pld)) {
							count += plds.get(pld);
						}
						plds.put(pld, count);
					}
				}
			}
			// if an pld is given we will remove it from the set
			if (input.size() > 1 && input.get(1) != null) {
				String pld = (String) input.get(1);
				if (plds.containsKey(pld)) {
					plds.remove(pld);
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
			warn("class cast exception at " + ex.getStackTrace(),
					PigWarning.UDF_WARNING_1);
		}
		return null;
	}

	@Override
	public Schema outputSchema(Schema arg0) {
		return new Schema(new Schema.FieldSchema(null, DataType.BAG));
	}
}
