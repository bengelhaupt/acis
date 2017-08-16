/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Represents an extension library for easily sending HTTP GET requests and
 * getting the response.
 *
 */
public final class SimpleHTTPGetRequestSender {

	/**
	 * Send a GET request to an address and returns a String containing the
	 * content returned from the host.<br>
	 * SSL is disabled<br>
	 * Should be executed in a separate Thread.
	 * 
	 * @param link
	 *            The request URL.
	 * @return The response text of the request.
	 * @throws Exception
	 *             When an error occurred. See the Exception's message for more
	 *             information.
	 */
	public static String downloadData(String link) throws Exception {
		// disable SSL verification
		HostnameVerifier allHostsValid = new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		URL url = new URL(link);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		conn.connect();
		InputStream is = conn.getInputStream();
		BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder total = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			total.append(line).append('\n');
		}
		is.close();
		return total.toString();
	}
}