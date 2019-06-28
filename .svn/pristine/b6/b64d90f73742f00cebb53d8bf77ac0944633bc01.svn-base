package org.webdatacommons.structureddata;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.webdatacommons.structureddata.cleanser.DuplicateRemovalPerSiteUsingHashOfCanonicalRepresentation;
import org.webdatacommons.structureddata.cleanser.DuplicateRemovalUsingHashOfCanonicalRepresentation;
import org.webdatacommons.structureddata.cleanser.SchemaViolationCorrection;
import org.webdatacommons.structureddata.cleanser.SortEntityByWebPage;

public class Master {

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {

		/* set up command line parameters */
		Options options = new Options();
		// correct with heuristics
		options.addOption(OptionBuilder.hasArgs(8).create("correct"));
		// dudup in pld
		options.addOption(OptionBuilder.hasArgs(4).create("plddedup"));
		// dedup global
		options.addOption(OptionBuilder.hasArgs(3).create("dedup"));
		// sort
		options.addOption(OptionBuilder.hasArgs(4).create("sort"));

		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		String[] io;
		boolean hit = false;
		try {
			cmd = parser.parse(options, args);

			io = cmd.getOptionValues("correct");
			if (io != null) {
				if (io.length != 8) {
					System.err
							.println("Usage: Master -correct <FileFolder> <OutputFolder> <ClassPropertyFile> <DataTypePropertyFile> <ObjectPropertyFile> <DomainViolationFixFile> <RangeVioloationFixFile> <NumberOfThreads>");
					System.exit(1);
				}
				SchemaViolationCorrection ec = new SchemaViolationCorrection(
						io[0], io[1], io[2], io[3], io[4], io[5], io[6],
						Integer.parseInt(io[7]));
				ec.process();
				hit = true;
			}

			io = cmd.getOptionValues("sort");
			if (io != null) {
				if (io.length != 4) {
					System.err
							.println("Usage: Master -sort <FileFolder> <OutputFolder> <FILTERSTRING> <NumberOfThreads>");
					System.exit(1);
				}
				SortEntityByWebPage sort = new SortEntityByWebPage(io[0],
						io[1], io[2], Integer.parseInt(io[3]));
				sort.process();
				hit = true;
			}

			io = cmd.getOptionValues("dedup");
			if (io != null) {
				if (io.length != 3) {
					System.err
							.println("Usage: Master -dedup <FileFolder> <OutputFolder> <NumberOfThreads>");
					System.exit(1);
				}
				DuplicateRemovalUsingHashOfCanonicalRepresentation dedup = new DuplicateRemovalUsingHashOfCanonicalRepresentation(
						io[0], io[1], Integer.parseInt(io[2]));
				dedup.process();
				hit = true;
			}
			
			io = cmd.getOptionValues("plddedup");
			if (io != null) {
				if (io.length != 4) {
					System.err
							.println("Usage: Master -plddedup <FileFolder> <OutputFolder> <FIXRDF> <NumberOfThreads>");
					System.exit(1);
				}
				DuplicateRemovalPerSiteUsingHashOfCanonicalRepresentation dedup = new DuplicateRemovalPerSiteUsingHashOfCanonicalRepresentation(
						io[0], io[1], Boolean.parseBoolean(io[2]), Integer.parseInt(io[3]));
				dedup.process();
				hit = true;
			}

			if (!hit) {
				printHelp();
			}
		} catch (ParseException e) {
			printHelp();
		}
	}

	private static void printHelp() {
		System.err.println("Welcome to the MD cleaning library.");
		System.err.println("Usage: Master -[sort,plddedup,correct,dedup]");
		System.exit(1);
	}

}
