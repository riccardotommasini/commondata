package org.webdatacommons.webgraph.process.shrink;

import java.io.IOException;

public class Starter {

	public static void main(String[] args) throws IOException {
		// TODO add logging for domainutils
		//TODO make this nice
		// http://www.javapractices.com/topic/TopicAction.do?Id=143
		startMapping(args[0], args[1], args[2], Long.parseLong(args[3]),
				Integer.parseInt(args[4]));
//		startShrinking();
//		startTransformation(args[0], args[1], args[2], 0, args[3]);
	}


	private static void startTransformation(String shrinkedIndex, String index,
			String output, int core, String type) throws IOException {
		ShrinkedIndexTransformationProcessor p = new ShrinkedIndexTransformationProcessor(
				shrinkedIndex, index, output, core, type);
		p.process();
	}

	private static void startMapping(String transformationIndexFileOrDir,
			String networkFileOrDir, String output, long maxIndex, int threads)
			throws IOException {
		ShrinkIndexMapper map = new ShrinkIndexMapper(
				transformationIndexFileOrDir, networkFileOrDir, output,
				maxIndex, threads, 0);
		map.process();
	}
}
