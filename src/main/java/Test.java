import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

public class Test {

	static String url = "https://u.f-q.me/watch?v=dxroZ5gWXH0";

	public static void main(String[] args) {
		// httpsGet(url);
		// System.out.println(Integer.parseInt("2393766767"));
		File file = new File("./dlurl");
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.isFile()) {
				String path = f.getPath();

				System.out.println(f.getAbsolutePath());
				System.out.println(f.getName());
				System.out.println(path);

				File dir = new File("." + File.separator + "done"
						+ File.separator + f.getName());
				dir.mkdirs();
				
				
				// file.mkdir();
			}
		}
	}
	
	public static List<String> httpsGet(String su) {

		String https_url = su;
		URL url;
		try {

			url = new URL(https_url);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			// dumpl all cert info
			print_https_cert(con);

			// dump all the content
			// List<String> lines = getContent(con);
			// return lines;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

				Map<String, List<String>> map = con.getHeaderFields();

				for (String key : map.keySet()) {
					System.out.println(key + " = " + map.get(key));
				}
				System.out.println(map.get("Location").get(0));
			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
