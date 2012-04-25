package com.zyeeda.framework.web.demo;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.framework.web.OpenIdConsumerServlet;

public class DemoOpenIdConsumerServlet extends OpenIdConsumerServlet {

	private static final long serialVersionUID = -6625819751410795172L;
	
	private static final Logger logger = LoggerFactory.getLogger(DemoOpenIdConsumerServlet.class);

	@Override
	protected void onAccessGranted(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.getWriter().println("OK");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	protected void onAccessDenied(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.getWriter().println("FAILED");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
