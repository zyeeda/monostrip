package com.zyeeda.framework.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.framework.openid.consumer.OpenIdConsumer;
import com.zyeeda.framework.openid.consumer.internal.HttpSessionOpenIdConsumer;

public abstract class OpenIdConsumerServlet extends HttpServlet {

	private static final long serialVersionUID = 6393634729613708155L;
	
	private static final Logger logger = LoggerFactory.getLogger(OpenIdConsumerServlet.class);
	
	private static final String OPENID_CONSUMER_KEY = "openid.consumer";
	private static final String REDIRECT_TO_URL_KEY = "redirect.to.url";
	private static final String RETURN_TO_URL_KEY = "return.to.url";
	private static final String PUBLIC_IDENTIFIER_KEY = "public.identifier";
	protected static final String OPENID_IDENTIFIER_KEY = "openid.identifier";
	
	private String publicIdentifier;
	private String returnToUrl;
	private OpenIdConsumer consumer;
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		this.publicIdentifier = config.getInitParameter(PUBLIC_IDENTIFIER_KEY);
		this.returnToUrl = config.getInitParameter(REDIRECT_TO_URL_KEY);
		
		this.consumer = (OpenIdConsumer) this.getServletContext().getAttribute(OPENID_CONSUMER_KEY);
		if (this.consumer == null) {
			try {
				this.consumer = new HttpSessionOpenIdConsumer();
				this.consumer.setReturnToUrl(config.getInitParameter(RETURN_TO_URL_KEY));
				this.getServletContext().setAttribute(OPENID_CONSUMER_KEY, this.consumer);
			} catch (ConsumerException e) {
				throw new ServletException(e);
			}
		}
		logger.info("Initialized OpenID Consumer Servlet.");
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.debug("servlet path info = {}", request.getPathInfo());
		
		if ("/verify".equals(request.getPathInfo())) {
			logger.info("Verify OpenId auth response.");
			
			try {
				Identifier id = this.consumer.verifyResponse(request);
				if (id == null) {
					logger.info("Access denied");
					this.onAccessDenied(request, response);
					return;
				}
				
				logger.info("openid identifier = {}", id.getIdentifier());
				this.onAccessGranted(request, response);
				return;
			} catch (OpenIDException e) {
				throw new ServletException(e);
			}
		}
		
		try {
			logger.info("Send OpenId auth request.");
			AuthRequest authReq = this.consumer.authRequest(this.publicIdentifier, request, response);
			request.setAttribute("message", authReq);
			request.getRequestDispatcher(this.returnToUrl).forward(request, response);
		} catch (OpenIDException e) {
			throw new ServletException(e);
		}
	}
	
	protected abstract void onAccessGranted(HttpServletRequest request, HttpServletResponse response);
	
	protected abstract void onAccessDenied(HttpServletRequest request, HttpServletResponse response);

}
