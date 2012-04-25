package com.zyeeda.framework.openid.consumer;

import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

public interface AxExtensionConsumer {

	public FetchRequest prepareFetchRequest() throws MessageException;
	
	public void processFetchResponse(FetchResponse fetchResp);
	
}
