package com.zyeeda.framework.openid.consumer.shiro;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.framework.openid.consumer.OpenIdConsumerService;

public class OpenIdConsumerAuthcFilter extends AuthenticatingFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(OpenIdConsumerAuthcFilter.class);
	
	private String callbackUrl;

    private String returnToUrl;
	
	private OpenIdConsumerService openIdConsumerService;
	
    public OpenIdConsumerService getOpenIdConsumerService() {
        return openIdConsumerService;
    }

    public void setOpenIdConsumerService(OpenIdConsumerService openIdConsumerService) {
        this.openIdConsumerService = openIdConsumerService;
    }

    @Override
    public void setLoginUrl(String loginUrl) {
        String previous = getLoginUrl();
        if (previous != null) {
            this.appliedPaths.remove(previous);
        }
        super.setLoginUrl(loginUrl);
        logger.trace("Adding login url to applied paths.");
        this.appliedPaths.put(getLoginUrl(), null);
    }


	@Override
	protected AuthenticationToken createToken(ServletRequest request,
			ServletResponse response) throws Exception {
		
		HttpServletRequest httpReq = (HttpServletRequest) request;
		
		Identifier id = openIdConsumerService.verifyResponse(httpReq);
		
		logger.info("Create OpenID authentication info.");
		AuthenticationToken token = new OpenIdAuthenticationToken(id);
		return token;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		
		logger.debug("Access denied.");
		
		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpRes = (HttpServletResponse) response;
		
		if (logger.isDebugEnabled()) {
			Cookie[] cookies = httpReq.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					logger.debug(cookie.getName() + " : " + cookie.getValue());
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("http request uri = {}", httpReq.getRequestURI());
			logger.debug("login url = {}", this.getLoginUrl());
		}
		if (this.isLoginRequest(httpReq, httpRes)) {
			// 如果请求的是登录地址，继续进入 OpenID 的登录界面
			logger.debug("OpenID login request detected, redirect to OP endpoint.");
			AuthRequest authReq = openIdConsumerService.authRequest(httpReq, httpRes);
			httpReq.setAttribute("message", authReq);
			return true;
		}
		
		logger.debug("success url = {}", this.getSuccessUrl());
		if (this.pathsMatch(this.getSuccessUrl(), request)) {
			// 如果请求的是认证成功地址 （一般是首页），重定向到登录界面
			logger.debug("Trying to visit login success URL, redirect to sign in page.");
			WebUtils.getAndClearSavedRequest(request);
			this.redirectToLogin(request, response);
			return false;
		}
		
		logger.debug("return to url = {}", this.returnToUrl);
		if (this.pathsMatch(this.returnToUrl, request)) {
			// 如果请求的是验证地址，处理登录
			logger.debug("OpenID verify request detected, attempt to perform signin.");
			boolean success = this.executeLogin(httpReq, httpRes);
			logger.debug("OpenID login result = {}", success);
			if (success) {
				this.issueSuccessRedirect(httpReq, httpRes);
			} else {
				httpRes.setStatus(HttpServletResponse.SC_FORBIDDEN);
			}
			
			return false;
		}
		
		// 请求其它地址，返回 401
		logger.debug("Permission denied on visiting resource [{}].", httpReq.getPathInfo());
		this.saveRequest(request);
		httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
	}
	
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        boolean result = super.isAccessAllowed(request, response, mappedValue);
        logger.debug("is access allowed = {}", result);
        return result;
    }
	
	@Override
	protected boolean onLoginFailure(AuthenticationToken token,	AuthenticationException e, ServletRequest request,
			ServletResponse response) {
		logger.error(e.getMessage(), e);
		return false;
	}
	
	protected boolean issueSuccessRedirect(HttpServletRequest httpReq, HttpServletResponse httpRes) throws IOException {
		SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(httpReq);
		if (savedRequest == null) {
			logger.debug("saved request is null");
			WebUtils.issueRedirect(httpReq, httpRes, this.getSuccessUrl());
			return false;
		}
		
		Map<String, String> params = new HashMap<String, String>(2);
		params.put("_url", savedRequest.getRequestUrl());
		params.put("_method", savedRequest.getMethod());
		logger.debug("saved request is {}", params);
		WebUtils.issueRedirect(httpReq, httpRes, this.callbackUrl, params);
		return false;
	}
	
    public String getReturnToUrl() {
        return returnToUrl;
    }
    
    public void setReturnToUrl(String returnToUrl) {
        /*String previous = this.returnToUrl;
        if (previous != null) {
            this.appliedPaths.remove(previous);
        }*/
        this.returnToUrl = returnToUrl;
        /*logger.trace("Adding OpenId returnTo url to applied paths.");
        this.appliedPaths.put(this.returnToUrl, null);*/
    }
    
     public String getCallbackUrl() {
         return callbackUrl;
     }
    	
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

}
