/* $Id$ */

package com.zyeeda.cdeio.sso.openid.consumer.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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

import com.zyeeda.cdeio.sso.openid.consumer.OpenIdConsumer;
import com.zyeeda.cdeio.sso.openid.consumer.support.OpenIdAuthenticationToken;

/**
 * OpenId consumer 认证过滤器.
 *
 * @author $Author$
 *
 */
public class OpenIdConsumerAuthenticationFilter extends AuthenticatingFilter {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdConsumerAuthenticationFilter.class);

    /**
     * OpenId consumer 对象.
     */
    private OpenIdConsumer openIdConsumer;

    public void setOpenIdConsumer(final OpenIdConsumer openIdConsumer) {
        this.openIdConsumer = openIdConsumer;
    }

    @Override
    public void setLoginUrl(final String loginUrl) {
        String previous = getLoginUrl();
        if (previous != null) {
            this.appliedPaths.remove(previous);
        }
        super.setLoginUrl(loginUrl);
        LOGGER.trace("Adding login url to applied paths.");
        this.appliedPaths.put(getLoginUrl(), null);
    }

    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) throws Exception {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        Identifier id = this.openIdConsumer.verifyResponse(httpReq);
        LOGGER.info("Create OpenID authentication token based on {}", id);
        AuthenticationToken token = new OpenIdAuthenticationToken(id);
        return token;
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        LOGGER.debug("Access denied.");

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("http request uri = {}", httpReq.getRequestURI());
            LOGGER.debug("sign-in url = {}", this.getLoginUrl());
        }
        if (this.isLoginRequest(httpReq, httpRes)) {
            // 如果请求的是登录地址，继续进入 OpenID 的登录界面
            LOGGER.debug("OpenID sign-in request detected, redirect to OP endpoint.");
            AuthRequest authReq = this.openIdConsumer.authRequest(this.openIdConsumer.getProviderXrdsUrl(), httpReq, httpRes);
            httpReq.setAttribute("message", authReq);
            return true;
        }

        LOGGER.debug("success url = {}", this.getSuccessUrl());
        if (this.pathsMatch(this.getSuccessUrl(), request)) {
            // 如果请求的是认证成功地址 （一般是首页），重定向到登录界面
            LOGGER.debug("Trying to visit sign-in success URL, redirect to sign in page.");
            WebUtils.getAndClearSavedRequest(request);
            this.redirectToLogin(request, response);
            return false;
        }

        if (this.pathsMatch(this.openIdConsumer.getReturnToPath(), request)) {
            // 如果请求的是验证地址，处理登录
            LOGGER.debug("OpenID verify request detected, attempt to perform signin.");
            boolean success = this.executeLogin(httpReq, httpRes);
            LOGGER.debug("OpenID sign-in result = {}", success);
            if (success) {
                this.issueSuccessRedirect(httpReq, httpRes);
            } else {
                httpRes.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }

            return false;
        }

        // 请求其它地址，返回 401
        LOGGER.debug("Permission denied on visiting resource {}.", httpReq.getRequestURI());
        this.saveRequest(request);
        httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    @Override
    protected boolean isAccessAllowed(final ServletRequest request, final ServletResponse response, final Object mappedValue) {
        boolean result = super.isAccessAllowed(request, response, mappedValue);
        LOGGER.debug("is access allowed = {}", result);
        return result;
    }

    @Override
    protected boolean onLoginFailure(final AuthenticationToken token, final AuthenticationException e, final ServletRequest request, final ServletResponse response) {
        LOGGER.error(e.getMessage(), e);
        return false;
    }

    /**
     * 重定向到成功页面.
     *
     * @param httpReq HTTP 请求
     * @param httpRes HTTP 响应
     * @return 请求是否继续
     * @throws IOException IO 异常
     */
    protected boolean issueSuccessRedirect(final HttpServletRequest httpReq, final HttpServletResponse httpRes) throws IOException {
        SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(httpReq);
        if (savedRequest == null) {
            LOGGER.debug("saved request is null");
            WebUtils.issueRedirect(httpReq, httpRes, this.getSuccessUrl());
            return false;
        }

        Map<String, String> params = new HashMap<String, String>(2);
        params.put("_url", savedRequest.getRequestUrl());
        params.put("_method", savedRequest.getMethod());
        LOGGER.debug("saved request is {}", params);
        WebUtils.issueRedirect(httpReq, httpRes, this.openIdConsumer.getCallbackPath(), params);
        return false;
    }

}
