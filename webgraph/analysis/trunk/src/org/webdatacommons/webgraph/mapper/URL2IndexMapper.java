package org.webdatacommons.webgraph.mapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import de.uni_mannheim.informatik.dws.dwslib.framework.Processor;
import de.uni_mannheim.informatik.dws.dwslib.util.DomainUtil;
import de.uni_mannheim.informatik.dws.dwslib.util.InputUtil;
import de.uni_mannheim.informatik.dws.dwslib.util.Tuple;

/**
 * Maps back URLs to an index. Input: Index File and URL List Output: IDs with
 * URL for the List in a specific format
 * 
 * @author Robert Meusel
 * 
 */
public class URL2IndexMapper extends Processor<String> {

	public URL2IndexMapper(int threads, String inputDirOrFile,
			String urlFileName) {
		super(threads);
		this.inputDirOrFile = inputDirOrFile;
		try {
			fillUrlMap(urlFileName);
		} catch (IOException e) {
			System.out.println("Could not load url list. "
					+ e.getLocalizedMessage());
			System.exit(0);
		}
	}

	public void printKeys() {
		for (String url : urls) {
			System.out.println(url + "\n");
		}
	}

	/**
	 * The input includes each line one url
	 * 
	 * @param urlFileOrDir
	 * @throws IOException
	 */
	private void fillUrlMap(String urlFileOrDir) throws IOException {
		urlIndexes.clear();
		List<File> fileList = InputUtil.getFileList(urlFileOrDir);
		System.out.println(fileList.size()
				+ " files as input for URL Map detected.");

		System.out.println(new Date() + " Start reading url file.");
		String line = null;
		long ercnt = 0;
		long linecnt = 0;
		for (File f : fileList) {
			BufferedReader br = InputUtil.getBufferedReader(f);
			while (br.ready()) {
				linecnt++;
				try {
					line = br.readLine();
					String tok[] = line.split("\t");
					//TODO remove or whatever
					if (Integer.parseInt(tok[1]) > 4) {
						urls.add(tok[0]);
					}
				} catch (Exception e) {
					System.out.println("Could not parse line: " + line
							+ ". Skipping.");
					ercnt++;
				}
			}
		}

		System.out.println(new Date() + " Done reading url file. Read "
				+ urls.size() + " urls to cache from " + linecnt
				+ " lines. Could not read " + ercnt + " lines from file.");
	}

	// private String urlBuilder(String domain, String path, String query,
	// String fragment) {
	// StringBuilder sb = new StringBuilder();
	// sb.append(domain);
	// sb.append(path != null ? path : "");
	// sb.append("?");
	// sb.append(query != null ? query : "");
	// sb.append(fragment != null ? fragment : "");
	// return sb.toString();
	// }

	private String inputDirOrFile;
	private Set<String> urls = new HashSet<String>();
	public List<Tuple<Long, String>> urlIndexes = new ArrayList<Tuple<Long, String>>();

	/**
	 * Writes a sorted output.
	 * 
	 * @param out
	 *            an outputstream
	 * @throws IOException
	 */
	public void writeOutput(OutputStream out) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));

//		try {
//			Collections.sort(urlIndexes, new Comparator<Tuple<Long, String>>() {
//
//				@Override
//				public int compare(Tuple<Long, String> o1,
//						Tuple<Long, String> o2) {
//						return o1.getFirstElement().compareTo(
//								o2.getFirstElement());
//				}
//			});
//
//		} catch (NullPointerException npe) {
//			System.out.println("Could not sort the data: " + npe.getMessage());
//			npe.printStackTrace();
//		}
		for (Tuple<Long, String> t : urlIndexes) {
			if (t != null)
				bw.write(t.getFirstElement() + "\t" + t.getSecondElement()
						+ "\n");
		}
		bw.flush();
	}

	@Override
	protected List<String> fillListToProcess() {
		try {
			return InputUtil.getFileReferenceList(inputDirOrFile);
		} catch (IOException e) {
			System.out.println("Could not read input. "
					+ e.getLocalizedMessage());
			return new ArrayList<String>();
		}
	}

	@Override
	protected void process(String file) {
		long erCnt = 0;
		try {
			BufferedReader br = InputUtil.getBufferedReader(new File(file));
			// int cnt = 0;
			while (br.ready()) {
				String tok[] = br.readLine().split("\t");
				String url = DomainUtil.uncompress(tok[0]);
				// if (cnt++ < 10) {
				// System.out.println("Example URL of " + file + ": " + url);
				// }
				if (urls.contains(url)) {
					Tuple<Long, String> t = new Tuple<Long, String>(
							Long.parseLong(tok[1]), url);
					if (t != null) {
						urlIndexes.add(t);
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Could not process file: " + file
					+ ". Skippting.");
			e.printStackTrace();
		} catch (NumberFormatException ne) {
			System.out.println("Could not process file: " + file
					+ ". Skippting.");
			ne.printStackTrace();
		}
		System.out.println("Parsed file " + file + ". Skipped " + erCnt
				+ " lines.");
		System.out.println("Outputmap consists out of " + urlIndexes.size()
				+ " pairs.");

	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		try {
			URL2IndexMapper mapper = new URL2IndexMapper(
					Integer.parseInt(args[3]), args[0], args[1]);
			File outputFile = new File(args[2]);
			mapper.process();
			GZIPOutputStream gzipStream = new GZIPOutputStream(
					new FileOutputStream(outputFile));
			mapper.writeOutput(gzipStream);
			gzipStream.close();
		} catch (IndexOutOfBoundsException e) {
			System.out
					.println("USAGE: org.webgraphcommons.mapper.URL2IndexMapper <INDEXFILEORDIR> <URLFILE> <OUTPUTFILE> <NUMTHREADS>");
		}

	}

}
