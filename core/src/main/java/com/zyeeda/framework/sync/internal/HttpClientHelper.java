package com.zyeeda.framework.sync.internal;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientHelper {
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(HttpClientHelper.class);
	
	public static void sendPostRequest(HttpPost post) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(post);
		Integer statusCode = response.getStatusLine().getStatusCode();
		if (HttpStatus.SC_OK == statusCode) {
			return;
		} else {
			printEntityInfo(response);
		}
	}
	
	public static void sendGetRequest(HttpGet get) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(get);
		Integer statusCode = response.getStatusLine().getStatusCode();
		if (HttpStatus.SC_OK == statusCode) {
			return;
		} else {
			printEntityInfo(response);
		}
	}
	
	public static void sendPutRequest(HttpPut put) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(put);
		Integer statusCode = response.getStatusLine().getStatusCode();
		if (HttpStatus.SC_OK == statusCode) {
			return;
		} else {
			printEntityInfo(response);
		}
	}
	
	public static void sendDeleteRequest(HttpDelete delete) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(delete);
		Integer statusCode = response.getStatusLine().getStatusCode();
		if (HttpStatus.SC_OK == statusCode) {
			return;
		} else {
			printEntityInfo(response);
		}
	}
	
	private static void printEntityInfo(HttpResponse response) {
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			Header[] headers = response.getAllHeaders();
		    for (int i = 0; i < headers.length; i++) {
			   logger.info("response head info:{}", headers[i]);
			}
		} else {
			logger.error("no response info");
		}
	}
}
