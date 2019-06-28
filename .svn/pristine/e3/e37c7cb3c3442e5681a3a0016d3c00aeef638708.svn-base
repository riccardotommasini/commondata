package org.webdatacommons.webgraph.extraction.pig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
 * This class implements the functionalities needed to extract for a given bag
 * of links and a given URL all links which point to site which are not of the
 * same PLD as the given URL. The output are a bag of links (only pointing to
 * external pages) enriched with the PLD of the given input URL. The output is
 * sorted and unique.
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 */
public class ExtractUniqueExternalWebLinks extends EvalFunc<DataBag> {

	/**
	 * Tuple Factory
	 */
	private final static TupleFactory tupleFactory = TupleFactory.getInstance();

	/**
	 * Functions extract all links from a given a bag of links which link out of
	 * a given input PLD. Input: (bag{chararray}, chararray)
	 */
	@Override
	public DataBag exec(Tuple input) throws IOException {
		try {

			// if an empty string is given return just the url
			if (input == null || input.size() < 2 || input.get(0) == null
					|| input.get(1) == null) {
				warn("Input does not contain needed information",
						PigWarning.UDF_WARNING_1);
				return new NonSpillableDataBag(0);
			} else {
				// origin
				String origin = (String) input.get(1);
				String originPLD = DomainUtil
						.getPayLevelDomainFromWholeURL(origin);
				if (originPLD == null || originPLD == DomainUtil.INVALID_URL) {
					warn("PLD could not be calculated from given input: "
							+ origin, PigWarning.UDF_WARNING_1);
					return new NonSpillableDataBag(0);
				}
				// links
				DataBag links = (DataBag) input.get(0);
				Iterator<Tuple> bagIterator = links.iterator();
				// checking all links
				HashSet<String> externalLinks = new HashSet<String>();
				while (bagIterator.hasNext()) {
					Tuple tp = bagIterator.next();
					if (tp == null || tp.get(0) == null) {
						continue;
					} else {
						String link = (String) tp.get(0);
						if (!originPLD.equals(DomainUtil.getPayLevelDomainFromWholeURL(link))) {
							externalLinks.add(link);
						}
					}
				}
				// only links to external pages are included.
				// adding the root note
				externalLinks.add(originPLD);

				DataBag bag = new NonSpillableDataBag(externalLinks.size());
				// sort
				List<String> linksList = new ArrayList<String>();
				linksList.addAll(externalLinks);
				Collections.sort(linksList);
				for (String s : linksList) {
					Tuple tp = tupleFactory.newTuple(1);
					tp.set(0, s);
					bag.add(tp);
				}
				return bag;
			}
		} catch (ClassCastException ex) {
			warn("class cast exception at " + ex.getStackTrace(),
					PigWarning.UDF_WARNING_1);
			return new NonSpillableDataBag(0);
		}
	}

	@Override
	public Schema outputSchema(Schema arg0) {
		return new Schema(new Schema.FieldSchema(null, DataType.BAG));
	}
}
