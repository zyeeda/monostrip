package com.zyeeda.framework.openid.provider.shiro;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zyeeda.framework.openid.provider.OpenIdProviderService;

public class OpenIdProviderEndpointFilter extends PathMatchingFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(OpenIdProviderEndpointFilter.class);
	
	private final static String OPENID_NAMESPACE = "http://specs.openid.net/auth/2.0";
	
	private OpenIdProviderService openIdProviderService = null;
    
    @Autowired
    public void setOpenIdProviderService(OpenIdProviderService openIdProviderService) {
        this.openIdProviderService = openIdProviderService;
    }
    
	@Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpRes = (HttpServletResponse) response;
		
		ParameterList params = null;
		if (this.pathsMatch(openIdProviderService.getEndpointCompleteUrl(), httpReq)) {
			logger.debug("OpenID provider endpoint complete request detected!");
			params = (ParameterList) SecurityUtils.getSubject().getSession().getAttribute("params");
			if (params == null) {
				this.outputInvalidAuthRequestMessage(httpRes);
				return false;
			}
		} else {
			logger.debug("OpenID provider endpoint direct request detected!");
			params = new ParameterList(httpReq.getParameterMap());
		}
		
		if (!params.hasParameter("openid.ns") || !params.hasParameter("openid.mode")) {
			this.outputInvalidAuthRequestMessage(httpRes);
			return false;
		}
		
		String ns = params.getParameterValue("openid.ns");
		if (!OPENID_NAMESPACE.equals(ns)) {
			this.outputInvalidAuthRequestMessage(httpRes);
			return false;
		}
		
        String mode = params.getParameterValue("openid.mode");
        logger.debug("OpenID mode = {}", mode);
        
        if ("associate".equals(mode)) {
        	logger.debug("OpenID request mode [associate] detected!");
        	Message message = openIdProviderService.associateRequest(params);
        	this.outputMessage(message, httpRes);
        	return false;
        }
        
        if ("check_authentication".equals(mode)) {
        	logger.debug("OpenID request mode [check_authentication] detected!");
        	Message message = openIdProviderService.verifyRequest(params);
        	this.outputMessage(message, httpRes);
        	return false;
        }
        
        if ("checkid_setup".equals(mode) || "checkid_immediate".equals(mode)) {
        	logger.debug("OpenID request mode [checkid_immediate] or [checkid_setup] detected!");
        	Subject subject = SecurityUtils.getSubject();
        	if (subject.isAuthenticated()) {
        		logger.debug("User is authenticated.");
        		SecurityUtils.getSubject().getSession().removeAttribute("params");
        		
        		String urlPrefix = httpReq.getScheme() + "://" + httpReq.getServerName() + ":" + httpReq.getServerPort() + httpReq.getContextPath();
        		
        		String userSelectedId = (subject.getPrincipals().iterator().next()).toString();
                userSelectedId = urlPrefix + "/provider/user.jsp?id=" + userSelectedId;
                String userSelectedClaimedId = userSelectedId;
                String fullEndpointUrl = urlPrefix + openIdProviderService.getEndpointUrl();
                if (logger.isDebugEnabled()) {
                	logger.debug("user selected id = {}", userSelectedId);
                	logger.debug("user selected claimed id = {}", userSelectedClaimedId);
                	logger.debug("full endpoint url = {}", fullEndpointUrl);
                }
                
                Message message = openIdProviderService.authResponse(params, userSelectedId, userSelectedClaimedId, subject.isAuthenticated(), fullEndpointUrl);
                if (message instanceof AuthSuccess) {
                    httpRes.sendRedirect(message.getDestinationUrl(true));
                    return false;
                }
                
                // TODO
                httpRes.sendRedirect(message.getDestinationUrl(true));
                return false;
        	}
        	
        	SecurityUtils.getSubject().getSession().setAttribute("params", params);
        	String mainPage = params.getParameterValue("openid.realm");
        	SecurityUtils.getSubject().getSession().setAttribute("mainPage", mainPage);
        	
        	String sessionId = (String) SecurityUtils.getSubject().getSession().getId();
    		logger.debug("OpenIdProviderEndpointFilter SecurityUtils.getSubject().getSession().getId() = {}", sessionId);
        	return true;
        }
        
        logger.debug("Unknown OpenID request mode [{}]", mode);
        this.outputInvalidAuthRequestMessage(httpRes);
        return false;
	}
	
	private void outputMessage(Message message, HttpServletResponse httpRes) throws IOException {
		String messageText = message.keyValueFormEncoding();
    	httpRes.getWriter().print(messageText);
	}
	
	private void output400Message(Message message, HttpServletResponse httpRes) throws IOException {
		httpRes.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		this.outputMessage(message, httpRes);
	}
	
	private void outputInvalidAuthRequestMessage(HttpServletResponse httpRes) throws IOException {
		Message message = DirectError.createDirectError("Invalid OpenID auth request!");
		this.output400Message(message, httpRes);
	}

}
