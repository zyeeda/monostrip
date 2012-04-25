package com.zyeeda.framework.openid.consumer.internal;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.discovery.DiscoveryInformation;


public class ShiroSessionOpenIdConsumer extends HttpSessionOpenIdConsumer {

	public ShiroSessionOpenIdConsumer() throws ConsumerException {
		super();
	}
	
	@Override
	public void storeDiscoveryInfo(HttpServletRequest httpReq, DiscoveryInformation discovered) {
		SecurityUtils.getSubject().getSession().setAttribute(OPENID_DISCOVERED_KEY, discovered);
	}
	
	@Override
	public DiscoveryInformation retrieveDiscoveryInfo(HttpServletRequest httpReq) {
		return (DiscoveryInformation) SecurityUtils.getSubject().getSession().getAttribute(OPENID_DISCOVERED_KEY);
	}

}
