package org.webdatacommons.webgraph.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class ClusterUtil {
	
	//TODO move whatever possible and what makes sense to IndexUtils

	public static void main(String[] args) throws IOException {
		List<Integer> clusterIds = getNodeCluster(
				"/home/rmeusel/data/crawl/cc/pldNetwork/webgraph/network.wcc.dist",
				30000000, 0);
		List<Integer> pageIds = getPageIdsFromClusterList(
				"/home/rmeusel/data/crawl/cc/pldNetwork/webgraph/network.wcc.clu",
				clusterIds);

		System.out.println("Done.");

	}

	/**
	 * Returns a list of ids of clusters which do not meet a certain size.
	 * Return only the first x found. Output is also written to file.
	 * 
	 * @param inputFile
	 *            the input file with the cluster sizes
	 * @param size
	 *            the max size of the returned clusters
	 * @param topX
	 *            the number of clusters which should be returned. If 0 all are
	 *            returned.
	 * @return a list of cluster ids
	 * @throws IOException
	 */
	private static List<Integer> getNodeCluster(String inputFile, int size,
			int topX) throws IOException {
		System.out.println("Detecting cluster ids....");
		BufferedReader br = new BufferedReader(new FileReader(new File(
				inputFile)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				inputFile + "_oneNodeClusterId")));
		int cnt = 0;
		if (topX == 0) {
			topX = Integer.MAX_VALUE;
		}
		List<Integer> oneNodeCluster = new ArrayList<Integer>();
		while (br.ready() && (topX > cnt)) {
			String line = br.readLine();
			if (Integer.parseInt(line) < size) {
				oneNodeCluster.add(cnt);
				bw.write(cnt + "\n");
			}
			cnt++;
		}
		bw.close();
		br.close();
		System.out.println(oneNodeCluster.size() + " clusters found.");
		return oneNodeCluster;
	}

	public static List<Integer> getPageIdsFromClusterList(String inputFile,
			List<Integer> clusterIds) throws IOException {
		System.out.println("Transforming data into array...");
		// sort to get max
		Collections.sort(clusterIds);
		int max = clusterIds.get(clusterIds.size() - 1);
		// everything is 0
		int clusterIdArray[] = new int[max + 1];
		for (Integer i : clusterIds) {
			clusterIdArray[i] = 1;
		}

		System.out.println("Detecting page ids....");
		BufferedReader br = new BufferedReader(new FileReader(new File(
				inputFile)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				inputFile + "_pageIdFromClusterList")));
		List<Integer> pageIds = new ArrayList<Integer>();
		int cnt = 0;
		while (br.ready()) {
			cnt++;
			if (cnt % 1000000 == 0) {
				System.out.println("Read " + cnt + " lines");
				bw.flush();
			}
			String line = br.readLine();
			String[] tok = line.split("\t");
			if (tok.length != 2) {
				System.out.println("Size did not fit ... length was "
						+ tok.length);
				continue;
			}

			try {
				if (clusterIdArray[Integer.parseInt(tok[1])] == 1) {
					bw.write(tok[0] + "\n");
					pageIds.add(Integer.parseInt(tok[0]));
				}

			} catch (ArrayIndexOutOfBoundsException ex) {
				// do nothing
			}
		}
		br.close();
		bw.close();
		return pageIds;
	}

	

	public static void checkIndexForUrls(String urlFile, String indexFile,
			String comp) throws IOException {

		Set<String> urls = new HashSet<String>();
		BufferedReader br = new BufferedReader(
				new FileReader(new File(urlFile)));
		System.out.println("Reading url file ...");
		while (br.ready()) {
			String line = br.readLine();
			// line = line.replace("http://vimeo.com", "#010#");
			if (line.startsWith("s:w.") || line.startsWith("w.")) {
				line = line.replaceFirst("w.", "www.");
			}
			if (comp == null || line.compareTo(comp) < 0) {
				urls.add(line.trim());
			}
		}
		br.close();
		br = new BufferedReader(new FileReader(new File(indexFile)));
		System.out.println("Detecting uris ...");
		int all = urls.size();
		int cnt = 0;
		while (br.ready() && urls.size() > 0) {
			String line = br.readLine();
			try {
				line = line.split("\t")[0];
			} catch (Exception e) {
				continue;
			}
			if (urls.contains(line.trim())) {
				cnt++;
				urls.remove(line.trim());
			}
		}
		System.out.println("Found " + cnt + " of " + all + " urls.");
		System.out.println("Urls not found: ");
		// System.out.println(urls.toString());
		// for (String url : urls) {
		// System.out.println(url);
		// }
	}

	public static void getTargetIds(String inputPath)
			throws FileNotFoundException, IOException {
		File dir = new File(inputPath);
		if (!dir.isDirectory()) {
			System.out.println("Input is not a directory.");
			return;
		}
		for (File f : dir.listFiles()) {

			if (f.isFile() && f.getName().endsWith("gz")) {
				Set<Long> ids = new HashSet<Long>();
				System.out.println("Reading " + f.getName());
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(new FileInputStream(f))));

				while (br.ready()) {
					String line = br.readLine();
					String tok[] = line.split("\t");
					if (tok.length != 2) {
						System.out.println("Incomplete line.");
						continue;
					}
					long index = Long.parseLong(tok[1]);
					ids.add(index);

				}
				List<Long> idsList = new ArrayList<Long>(ids);
				Collections.sort(idsList);
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
						dir, "targetid_" + f.getName().replace(".gz", ""))));
				for (Long id : idsList) {
					bw.write(id + "\n");
				}

				bw.close();
				br.close();
			}

		}
	}

	public static void getClusterIdsOfPageIds(String clusterFile,
			String inputPath) throws NumberFormatException, IOException {
		File dir = new File(inputPath);
		if (dir.isFile()) {
			System.out.println("Input is no directory.");
			return;
		}
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().startsWith("targetid_")) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				Set<Long> ids = new HashSet<Long>();
				System.out.println("Reading " + f.getName());
				while (br.ready()) {
					Long index = Long.parseLong(br.readLine());
					ids.add(index);
				}
				br.close();
				Set<Long> clusterIds = new HashSet<Long>();
				br = new BufferedReader(new FileReader(new File(clusterFile)));

				System.out.println("Reading cluster ids.");
				long lnCnt = 0;
				while (br.ready() && !ids.isEmpty()) {
					String line = br.readLine();
					if (ids.contains(lnCnt)) {
						String tok[] = line.split("\t");
						clusterIds.add(Long.parseLong(tok[1]));
						ids.remove(lnCnt);
					}
					lnCnt++;
				}
				br.close();
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
						dir, "clusterid_" + f.getName())));
				for (Long id : clusterIds) {
					bw.write(id + "\n");
				}

				bw.close();
				br.close();
			}
		}
	}

	public static void getClusterSizeFromClusterId(String clusterSizeFile,
			String clusterIdFile) throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(
				clusterIdFile)));
		HashSet<Long> clusterIds = new HashSet<Long>();
		System.out.println("Reading file.");
		while (br.ready()) {
			clusterIds.add(Long.parseLong(br.readLine()));
		}
		br.close();
		br = new BufferedReader(new FileReader(new File(clusterSizeFile)));
		HashMap<Long, Long> sizes = new HashMap<Long, Long>();
		long lnCnt = 0;
		System.out.println("Parsing cluster sizes");
		while (br.ready() && !clusterIds.isEmpty()) {
			String line = br.readLine();
			if (clusterIds.contains(lnCnt)) {
				Long size = Long.parseLong(line);
				long cnt = 1;
				if (sizes.containsKey(size)) {
					cnt += sizes.get(size);
				}
				sizes.put(size, cnt);
				clusterIds.remove(lnCnt);
			}
			lnCnt++;
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				clusterIdFile + "_size_dist")));
		List<Long> sorted = new ArrayList<Long>(sizes.keySet());
		Collections.sort(sorted);
		for (Long size : sorted) {
			bw.write(size + "\t" + sizes.get(size) + "\n");
		}
		bw.close();
		System.out.println("done.");
	}

}
