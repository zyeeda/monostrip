package com.zyeeda.framework.sync.internal;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;


public class HttpPutTask implements Runnable {

	private HttpPut put = null;
	
	public HttpPutTask() {}
	
	public HttpPutTask(HttpPut put) {
		this.put = put;
	}
	
	@Override
	public void run() {
		try {
			HttpClientHelper.sendPutRequest(this.put);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
