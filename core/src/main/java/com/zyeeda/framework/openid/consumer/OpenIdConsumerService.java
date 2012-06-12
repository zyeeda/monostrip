package com.zyeeda.framework.openid.consumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;

public interface OpenIdConsumerService {

	public AuthRequest authRequest(HttpServletRequest request, HttpServletResponse response) throws OpenIDException;
	
	public Identifier verifyResponse(HttpServletRequest request) throws OpenIDException;
	
}
