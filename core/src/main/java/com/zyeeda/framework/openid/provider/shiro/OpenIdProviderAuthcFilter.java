package com.zyeeda.framework.openid.provider.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zyeeda.framework.openid.provider.OpenIdProviderService;

public class OpenIdProviderAuthcFilter extends FormAuthenticationFilter {
	
	private final static Logger logger = LoggerFactory.getLogger(OpenIdProviderAuthcFilter.class);

	private OpenIdProviderService openIdProviderService = null;
    
    @Autowired
    public void setOpenIdProviderService(OpenIdProviderService openIdProviderService) {
        this.openIdProviderService = openIdProviderService;
    }
    
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		logger.debug("on access denied");
		
		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpRes = (HttpServletResponse) response;
		
		String sessionId = (String) SecurityUtils.getSubject().getSession().getId();
		logger.debug("OpenIdProviderAuthcFilter SecurityUtils.getSubject().getSession().getId() = {}", sessionId);
		
		// 访问登录页面
		if (this.isLoginRequest(httpReq, httpRes)) {
			logger.debug("Sign in request detected!");
			// 提交数据到登录页面
			Session oldsession = SecurityUtils.getSubject().getSession(false);
			if (oldsession == null) {
				logger.info("OpenIdProviderAuthcFiler session is null!");
			}
			if (this.isLoginSubmission(httpReq, httpRes)) {
				logger.debug("Sign in submission request detected!");
				return this.executeLogin(httpReq, httpRes);
			}
			
			// 直接访问登录页面
			logger.debug("Sign in view request detected!");
			return true;
		}
		
		// 访问其它页面，转发到登录页面
		logger.debug("Redirect to sign in page {}!", this.getLoginUrl());
		httpRes.sendRedirect(this.getLoginUrl());
		return false;
	}
	
	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject,
            ServletRequest request, ServletResponse response) throws Exception {
		logger.debug("subject.isAuthenticated = {}, subject.isRemembered = {}",
				subject.isAuthenticated(), subject.isRemembered());
		
		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpRes = (HttpServletResponse) response;
		
		logger.debug("this.isOpenIdRequest(httpReq) = {}", this
				.isOpenIdRequest(httpReq));
		
		logger.debug("this.getServletContext().getContextPath() + opSvc.getEndpointCompleteUrl() = {}",
				this.getServletContext().getContextPath() + openIdProviderService.getEndpointCompleteUrl());
		
		if (this.isOpenIdRequest(httpReq)) {
			logger.debug("sendRedirect to {}", this.getServletContext().getContextPath() + openIdProviderService.getEndpointCompleteUrl());
			httpRes.sendRedirect(this.getServletContext().getContextPath() + openIdProviderService.getEndpointCompleteUrl());
			return false;
		}
		return true;
	}
	
	@Override
	protected boolean onLoginFailure(AuthenticationToken token,	AuthenticationException e, ServletRequest request,
			ServletResponse response) {
		logger.error("login failure", e);
		super.onLoginFailure(token, e, request, response);
		return true;
	}
	
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        boolean result = super.isAccessAllowed(request, response, mappedValue);
        logger.debug("is access allowed = {}", result);
        return result;
    }
	
	private boolean isOpenIdRequest(HttpServletRequest httpReq) {
		return SecurityUtils.getSubject().getSession().getAttribute("params") != null;
	}
}
