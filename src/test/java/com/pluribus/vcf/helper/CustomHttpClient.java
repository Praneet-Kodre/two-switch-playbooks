package com.pluribus.vcf.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

@SuppressWarnings("deprecation")
public class CustomHttpClient {
	 boolean enableHttpsForAppTier;
	 int appTierPort;
	 private static CustomHttpClient obj;

	private CustomHttpClient() throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
		java.util.Properties prop = new Properties();
	    prop.loadFromXML(new FileInputStream("Metrics.xml"));
	    this.appTierPort = Integer.parseInt(prop.getProperty("appTierPort"));
		this.enableHttpsForAppTier = true;
	
	}
	
	public  HttpClient getNewHttpClient(HttpParams params) {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			if (params == null) {
				params = new BasicHttpParams();
			}
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			SchemeRegistry registry = new SchemeRegistry();
			if (enableHttpsForAppTier) {
				registry.register(new Scheme("https", sf, this.appTierPort));
			} else {
				registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), this.appTierPort));
			}
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	public static CustomHttpClient getInstance() {
		try {
			if (obj == null) {
				synchronized (CustomHttpClient.class) {
					if (obj == null) {
						obj = new CustomHttpClient();
					}
				}
			}
			return obj;
		} catch (Exception e) {
			com.jcabi.log.Logger.error("Error while creating object for CustomizeHttpClient : ",""+e);
			return null;
		}
	}
}
