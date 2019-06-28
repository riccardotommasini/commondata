package org.webdatacommons.webgraph.extraction.pig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pig.EvalFunc;
import org.apache.pig.PigWarning;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.NonSpillableDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;

/**
 * Class implements the functionality to extract all links included in a
 * metadata JSON file from CC and return them sorted and distinct within a bag.
 * As input the function expects the part of the JSON metadata string which
 * includes the list of links, normally starting with "links":[{....}]. Only
 * links of type 'link' and 'a' are returned. Important: The different links
 * (including type etc.) still needs to be separated by curly brackets as this
 * function does not use a JSON ready but uses regular expressions.
 * 
 * @author Robert Meusel (robert@informatik.uni-mannheim.de)
 * 
 */
public class ExtractUniqueWebLinks extends EvalFunc<DataBag> {

	/**
	 * Tuple Factory
	 */
	private final static TupleFactory tupleFactory = TupleFactory.getInstance();
	
	private static Logger log = Logger.getLogger("ExtractUniqueWebLinks.class");

	/**
	 * Regular expression pattern to extract type and href.
	 */
	private final static Pattern linkTypePattern = Pattern
			.compile("\"type\":\"([^\"]*)\"[^}]*\"href\":\"([^\"]*)\"");

	/**
	 * Functions extract all links from a JSON and returns all 'link' and 'a'
	 * links in a distinct bag.
	 */
	@Override
	public DataBag exec(Tuple input) throws IOException {
		try {
			// if an empty string is given return just the url
			if (input == null || input.size() < 1 || input.get(0) == null) {
				return new NonSpillableDataBag(0);
			} else {
				String links = (String) input.get(0);
				// get all (within the JSON links:[...] from CC)
				Matcher m = linkTypePattern.matcher(links);
				HashSet<String> allMatches = new HashSet<String>();
				while (m.find()) {
					// check if the type is a or link
					String type = m.group(1);
					if (type != null
							&& (type.equals("a") || type.equals("link"))) {
						// only get the links not the surround stuff
						allMatches.add(m.group(2));

					}
				}
				DataBag bag = new NonSpillableDataBag(allMatches.size());
				// sort
				List<String> linksList = new ArrayList<String>();
				linksList.addAll(allMatches);
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
			log.log(Level.WARNING, "Could not cast", ex.fillInStackTrace());
			return new NonSpillableDataBag(0);
		}
		catch (Error er) {
			warn("something went wrong with an error " + er.getStackTrace()[0],
			PigWarning.UDF_WARNING_3);
			log.log(Level.WARNING, "Something went wrong and cause en error", er.fillInStackTrace());
			return new NonSpillableDataBag(0);
		}
	}

	@Override
	public Schema outputSchema(Schema arg0) {
		return new Schema(new Schema.FieldSchema("links", DataType.BAG));
	}
}
