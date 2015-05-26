package dl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import com.alibaba.fastjson.JSONObject;

//TODO wget html ---> java parse to download url ---> do download 

public class DlUtils {
	public static void main(String[] args) throws IOException {

		filterOriFile();

	}

	public static final String MORE_TAG = "data-uix-load-more-href";

	public static final String baseUrl = "https://u.f-q.me";

	public static final String baseVedioPre = "https://u.f-q.me/watch?v=";

	public static final String title = "<title>";

	public static final String count = "videos</li>";

	public static String getFieldValue(String fieldName, String l) {
		int start = l.indexOf(fieldName + "=");
		if (start < 0) {
			return null;
		}
		l = l.substring(start);
		int vs = l.indexOf("\"");
		int ve = l.indexOf("\"", vs + 1);
		String ret = l.substring(vs + 1, ve);
		return ret;
	}

	public static void writeFile(Path path, Map<String, String> urls) throws IOException {
		// path = Paths.get(".", "urls.log");
		Files.deleteIfExists(path);
		Writer w = Files.newBufferedWriter(path);
		BufferedWriter bw = new BufferedWriter(w);
		for (String key : urls.keySet()) {
			bw.write(key + " " + urls.get(key));
			bw.newLine();
		}
		bw.close();
	}

	public static String toFileName(String fn, int size) {
		Matcher matcher = Pattern.compile("(\\W+)").matcher(fn);
		String ret = matcher.replaceAll("_");
		ret = ret.replace("YouTube", "");
		if (ret.endsWith("html")) {
			ret = ret.replace("html", "");
		}
		if (ret.endsWith("htm")) {
			ret = ret.replace("htm", "");
		}
		ret = ret + "_" + size;
		matcher = Pattern.compile("([_]+)").matcher(ret);
		ret = matcher.replaceAll("_");
		return ret;
	}

	public static void filterOriFile() throws IOException {
		Path path = Paths.get("./ori");
		Files.list(path)
				.filter(pa -> !Files.isDirectory(pa))
				.forEach(
					pa -> {
						System.out
								.println(" ======================================================================");
						String fileName = pa.getFileName().toString();
						System.out.println(" file name " + fileName);

						try {
							Map<String, String> allUrls = filterLocal(pa);

							String ret = toFileName(fileName, allUrls.size());
							System.out.println(" final file name  = [" + ret + "]");

							Path toFile = Paths.get("./dlurl", ret);
							writeFile(toFile, allUrls);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					});

	}

	public static String cutDuration(String title) {
		int start = title.indexOf("- Duration");
		if (start < 0) {
			return title;
		}
		return title.substring(0, start);
	}

	public static Map<String, String> filterLines(List<String> allLines, String baseUrl,
													List<String> checkTags, String videoFieldName,
													String titleFieldName) {
		Map<String, String> map = new HashMap<String, String>();

		int count = 0;
		for (String l : allLines) {
			boolean ok = true;
			for (String checkTag : checkTags) {
				if (!l.contains(checkTag)) {
					ok = false;
					break;
				}
			}
			if (!ok) {
				continue;
			}

			count++;
			String videoContent = getFieldValue(videoFieldName, l);
			String titleContent = getFieldValue(titleFieldName, l);

			titleContent = cutDuration(titleContent).trim();

			Matcher matcher = Pattern.compile("(\\W+)").matcher(titleContent);

			String fileName = matcher.replaceAll("_") + ".mp4";
			String url = baseUrl + videoContent;

			if (map.containsValue(fileName) || fileName.equals("_.mp4")) {
				fileName = url.replace(baseVedioPre, "") + fileName;
			}
			System.out.println(url + " " + fileName);
			if (!map.containsKey(url)) {
				map.put(url, fileName);
			}
			else {
				System.out.println(" ===== duplicate : " + url + " " + fileName);
			}
		}
		System.out.println(" count = " + map.size());
		if (count != map.size()) {
			System.out.println(" !!!!!!!!!!!!!! ori count = " + count);
		}
		return map;

	}

	public static Map<String, String> filterLocal(Path path) throws IOException {
		Reader reader = Files.newBufferedReader(path);
		BufferedReader br = new BufferedReader(reader);

		List<String> allLines = br.lines().collect(Collectors.toList());

		return filterLines(allLines, baseVedioPre, Arrays.asList("data-video-id", "data-title"),
			"data-video-id", "data-title");
	}

	public static List<String> httpsGet(String su) {

		String https_url = su;
		URL url;
		try {

			url = new URL(https_url);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			// dumpl all cert info
			//			print_https_cert(con);

			// dump all the content
			List<String> lines = getContent(con);
			return lines;
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void print_https_cert(HttpsURLConnection con) {

		if (con != null) {

			try {

				System.out.println("Response Code : " + con.getResponseCode());
				System.out.println("Cipher Suite : " + con.getCipherSuite());
				System.out.println("\n");

				Certificate[] certs = con.getServerCertificates();
				for (Certificate cert : certs) {
					System.out.println("Cert Type : " + cert.getType());
					System.out.println("Cert Hash Code : " + cert.hashCode());
					System.out.println("Cert Public Key Algorithm : "
							+ cert.getPublicKey().getAlgorithm());
					System.out.println("Cert Public Key Format : "
							+ cert.getPublicKey().getFormat());
					System.out.println("\n");
				}

			}
			catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static List<String> getContent(HttpsURLConnection con) {
		List<String> lines = new ArrayList<String>();

		if (con != null) {

			try {

				System.out.println("****** Content of the URL ********");
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

				String input;
				while ((input = br.readLine()) != null) {
					lines.add(input);
					// System.out.println(input);
				}
				br.close();

			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}
		return lines;
	}

	public static String replaceMoreContent(String line) throws UnsupportedEncodingException {
		String ret = "";
		try {
			JSONObject json = JSONObject.parseObject(line);
			for (String key : json.keySet()) {
				String value = json.getString(key);
				ret += value;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static void parseLines(String url, boolean isMoreUrl, Map<String, String> fillMap,
									List<String> checkTags, String vedioPre, String vedioTag,
									String titleTag) throws UnsupportedEncodingException {
		if (url == null) {
			return;
		}
		System.out.println(url);
		System.out.println(" map count " + fillMap.size());
		System.out.println("========================");

		List<String> lines = DlUtils.httpsGet(url);

		if (isMoreUrl) {
			String ml = lines.get(0);
			ml = DlUtils.replaceMoreContent(ml);
			lines = Arrays.asList(ml.split("\n"));
		}

		String moreUrl = "";
		for (String line : lines) {
			if (line.contains(DlUtils.MORE_TAG)) {
				moreUrl = DlUtils.getFieldValue(DlUtils.MORE_TAG, line);
				if (moreUrl != null && !moreUrl.equals("")) {
					System.out.println("===================== find more url = " + moreUrl);
				}
				else {
					System.out.println(" ===================  can not find more url = " + moreUrl);
				}
			}

			if (line.contains(title)) {
				System.out.println();
				String ti = line.replace(title, "");
				ti = ti.trim();

				fillMap.put(title, ti);
			}

			if (line.contains(count)) {
				System.out.println();
				line = line.replace(" videos</li>", "");
				line = line.replace("<li>", "");
				line = line.trim();
				fillMap.put(count, line);
			}

		}

		Map<String, String> map = DlUtils.filterLines(lines, vedioPre, checkTags, vedioTag,
			titleTag);
		for (String key : map.keySet()) {
			String value = map.get(key);
			if (fillMap.containsKey(key)) {
				System.out.println(" ================================ [" + key
						+ "] \n duplicate key " + value + " \n" + fillMap.get(key));
			}
			else {
				fillMap.put(key, value);
			}
		}

		if (moreUrl != null && !moreUrl.equals("")) {
			String mu = baseUrl + moreUrl;
			parseLines(mu, true, fillMap, checkTags, vedioPre, vedioTag, titleTag);
		}
		else {
			parseLines(null, false, fillMap, checkTags, vedioPre, vedioTag, titleTag);
		}
	}

	public static void mapToFile(Map<String, String> map, String info) throws IOException {

		System.out.println(" ====================== ");
		System.out.println(" final map size " + map.size());

		System.out.println(" Title is [" + map.get(DlUtils.title) + "]");
		System.out.println(" count is [" + map.get(DlUtils.count) + "]");
		String ti = map.remove(DlUtils.title);
		map.remove(DlUtils.count);

		Set<String> keys = new HashSet<>(map.keySet());
		for (String key : keys) {
			String fn = map.get(key);
			if (!validFileName(fn)) {
				System.out.println("===<<<<<<<<<<<<<<<<== exclude invalid file " + key + " = "
						+ map.get(key));
				map.remove(key);
			}
		}

		String fileName = DlUtils.toFileName(ti, map.size());
		fileName = fileName + "_" + info;
		System.out.println(" fileName is [" + fileName + "]");

		Path toFile = Paths.get("./dlurl", fileName);
		DlUtils.writeFile(toFile, map);
	}

	public static boolean validFileName(String fileName) {
		if (fileName.contains("_Deleted_Video_.mp4") || fileName.contains("_Private_Video_.mp4")) {
			return false;
		}
		return true;
	}
}
