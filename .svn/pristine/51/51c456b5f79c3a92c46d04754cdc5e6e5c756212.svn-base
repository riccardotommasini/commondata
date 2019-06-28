package org.webdatacommons.webgraph.extraction.pig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
 * Class implements the functionality to filter from a bag of tuples including a
 * bag of URLs a unique set of URLs belonging to a given PLD. The function
 * expects as input a tuple containing a bag of tuples (index 0) and a PLD
 * (index 1) which is decisive for the selection of the links. Input looks like:
 * tuple(bag{(pld:chararray, url:chararray)}). The output is returned sorted and
 * unique.
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 */
public class ExtractUniqueInternalWebLinks extends EvalFunc<DataBag> {

	private final static TupleFactory tupleFactory = TupleFactory.getInstance();

	/**
	 * Functions calculates a distinct list of URLs from a given list of URLs
	 * and a PLD which all belong to the given PLD. Internally a set is used.
	 * Input: tuple(bag{(pld:chararray, url:chararray)}) Output:
	 * bag{chararray}
	 */
	@Override
	public DataBag exec(Tuple input) throws IOException {
		if (input == null || input.size() < 1 || input.get(0) == null) {
			return new NonSpillableDataBag(0);
		}
		try {
			DataBag inputBag = (DataBag) input.get(0);
			Iterator<Tuple> bagIter = inputBag.iterator();
			Set<String> links = new HashSet<String>();
			while (bagIter.hasNext()) {
				Tuple bagTuple = bagIter.next();
				if (bagTuple.size() < 2) {
					// if bag is too small skip
					continue;
				}
				String pld = (String) bagTuple.get(0);
				String url = (String) bagTuple.get(1);
				if (pld == null || url == null) {
					continue;
				}
				if (pld.equals(DomainUtil.getPayLevelDomainFromWholeURL(url))) {
					links.add(url);
				}

			}
			DataBag ret = new NonSpillableDataBag(links.size());
			// sort
			List<String> linkList = new ArrayList<String>();
			linkList.addAll(links);
			Collections.sort(linkList);
			for (String link : linkList) {
				Tuple tp = tupleFactory.newTuple(1);
				tp.set(0, link);
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
