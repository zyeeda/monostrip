package com.zyeeda.framework.sync.internal;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;


public class HttpPostTask implements Runnable {
	
	private HttpPost post = null;
	
	public HttpPostTask() {}
	
	public HttpPostTask(HttpPost post) {
		this.post = post;
	}
	
	@Override
	public void run() {
		try {
			HttpClientHelper.sendPostRequest(this.post);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
