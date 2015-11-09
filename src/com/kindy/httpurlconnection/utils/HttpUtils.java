package com.kindy.httpurlconnection.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

public class HttpUtils {
	protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
	protected static final String CHARSET = "UTF-8";
	/** 建立连接的超时时间 */
	protected static final int connectTimeout = 5 * 1000;
	/** 建立到资源的连接后从 input 流读入时的超时时间 */
	protected static final int readTimeout = 10 * 1000;

	private static HttpUtils instance;

	private TrustManager[] trustAllCerts = { new X509TrustManager() {

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType) {

		}

		public void checkServerTrusted(X509Certificate[] certs, String authType) {

		}
	} };

	public static HttpUtils getInstance() {
		if (instance == null) {
			synchronized (HttpUtils.class) {
				if (instance == null) {
					instance = new HttpUtils();
				}
			}
		}
		return instance;
	}

	private HttpUtils() {
		try {
			HttpsURLConnection
					.setDefaultHostnameVerifier(new NullHostNameVerifier());
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public HttpURLConnection createConnection(String url) throws IOException {
		String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
		HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl)
				.openConnection();
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);
		return conn;
	}

	public InputStream getInputStream(String url) {
		InputStream is = null;

		try {
			HttpURLConnection conn = createConnection(url);
			conn.setRequestMethod("GET");
			is = conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return is;
	}

	public String getString(String url) {
		String result = null;
		InputStream is = null;
		BufferedReader br = null;
		try {
			is = getInputStream(url);
			br = new BufferedReader(new InputStreamReader(is, CHARSET));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			result = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
			}
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
		}

		return result;
	}

	public String postString(String url, String params) {
		String result = null;
		OutputStream os = null;
		InputStream is = null;
		BufferedReader br = null;
		try {
			HttpURLConnection conn = createConnection(url);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);// POST方式不能缓存数据
			// conn.setRequestProperty(field, newValue);//header

			conn.setRequestProperty("Content-Type", "application/json; charset=" + CHARSET);

			// // 设置请求的头
			// conn.setRequestProperty("Connection", "keep-alive");
			// // 设置请求的头
			// conn.setRequestProperty("User-Agent",
			// "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");

			if (params != null) {
				os = conn.getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);
				dos.write(params.getBytes(CHARSET));
				dos.flush();
				dos.close();
			}

			is = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(is, CHARSET));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			result = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
			}
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
			}
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
		}

		return result;
	}

	protected boolean shouldBeProcessed(HttpURLConnection conn)
			throws IOException {
		return conn.getResponseCode() == 200;
	}

	protected void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	private class NullHostNameVerifier implements HostnameVerifier {
		public NullHostNameVerifier() {

		}

		public boolean verify(String hostname, SSLSession session) {
			Log.i("RestUtilImpl", "Approving certificate for " + hostname);
			return true;
		}
	}
}
