package org.websitegenerator.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class SpeedTest {

	/**
	 * @param args
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws MalformedURLException,
			IOException, InterruptedException {

		ArrayList<String> urls = new ArrayList<String>();
		urls.add("http://thobach.de/index.php");
		urls.add("http://d1fkpe63kjy5ka.cloudfront.net/index2.html");
		urls.add("http://s3-eu-west-1.amazonaws.com/websitegenerator/index2.html");
		urls.add("http://thobach.de/index2.html");
		urls.add("https://sslsites.de/thobach.de/index.php");
		urls.add("https://d1fkpe63kjy5ka.cloudfront.net/index2.html");
		urls.add("https://s3-eu-west-1.amazonaws.com/websitegenerator/index2.html");
		urls.add("https://sslsites.de/thobach.de/index2.html");

		HashMap<String, Integer> times = new HashMap<String, Integer>();

		for (int i = 0; i < 101; i++) {
			for (String url : urls) {
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httpget = new HttpGet(url);
				long start = System.currentTimeMillis();
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					int l;
					byte[] tmp = new byte[2048];
					while ((l = instream.read(tmp)) != -1) {
					}
				}
				long end = System.currentTimeMillis();
				if (i != 0) {
					times.put(url, Integer.valueOf(((times.get(url) == null ? 0
							: times.get(url)) + (int) (end - start))));
				}
				System.out.println(url + ": " + (end - start) + " ms");
				Thread.sleep(1000);
			}
		}

		for (String url : urls) {
			System.out.println("AVG for " + url + ": " + (times.get(url) / 100)
					+ " ms");
		}

	}
}
