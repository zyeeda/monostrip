package com.zyeeda.framework.openid.consumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;

public interface OpenIdConsumer {

	public abstract AuthRequest authRequest(String userSuppliedId,
			HttpServletRequest httpReq, HttpServletResponse httpRes)
			throws OpenIDException;

	public abstract Identifier verifyResponse(HttpServletRequest httpReq)
			throws OpenIDException;

	public abstract void setReturnToUrl(String returnToUrl);

	public abstract String getReturnToUrl();

	public abstract String getRealm();

	public abstract void setRealm(String realm);

	public abstract void storeDiscoveryInfo(HttpServletRequest httpReq,
			DiscoveryInformation discovered);

	public abstract DiscoveryInformation retrieveDiscoveryInfo(
			HttpServletRequest httpReq);

	//public AxExtensionConsumer getAxExtensionConsumer();

	//public void setAxExtensionConsumer(AxExtensionConsumer axExtConsumer);

}