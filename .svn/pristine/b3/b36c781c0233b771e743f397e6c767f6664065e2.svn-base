package org.webdatacommons.webgraph.extraction.pig;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.PigWarning;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.NonSpillableDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;

public class StringToBag extends EvalFunc<DataBag> {
	
	/**
	 * Tuple Factory
	 */
	private final static TupleFactory tupleFactory = TupleFactory.getInstance();

	@Override
	public DataBag exec(Tuple input) throws IOException {
		if (input == null || input.size() < 1) {
			return new NonSpillableDataBag(0);
		} else {
			try {
				String inputString = (String) input.get(0);
				String[] targets;
				if (inputString == null || inputString.length() < 1){
					return new NonSpillableDataBag(0);
				}else{
					// as we know we already have a bag including tuples in the string we remove leading and final curly brackets
					inputString = inputString.substring(1, inputString.length()-1);
					targets = inputString.split("\\),\\(");
					DataBag bag = new NonSpillableDataBag(targets.length);
					for (String target : targets){
						//there might be a leading or ending bracket
						if (target.startsWith("(")){
							target = target.substring(1);
						}
						if (target.endsWith(")")){
							target = target.substring(0, target.length() - 1);
						}
						Tuple tp = tupleFactory.newTuple(1);
						tp.set(0, target);
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
	}
	
	@Override
	public Schema outputSchema(Schema arg0) {
		return new Schema(new Schema.FieldSchema(null, DataType.BAG));
	}
}
