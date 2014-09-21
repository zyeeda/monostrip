/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.web;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Cache;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.util.CookieGenerator;

import com.zyeeda.cdeio.sso.openid.provider.OpenIdProvider;
import com.zyeeda.cdeio.sso.openid.provider.web.security.SignInToken;

/**
 * OpenId provider 认证过滤器.
 *
 * @author $Author$
 *
 */
public class OpenIdProviderAuthenticationFilter extends FormAuthenticationFilter implements ServletContextAware {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdProviderAuthenticationFilter.class);

    /**
     * 默认最大登录尝试次数.
     */
    private static final int DEFAULT_MAX_SIGN_IN_ATTEMPTS = 3;

    /**
     * 最大登录尝试次数.
     */
    private int maxSignInAttempts = DEFAULT_MAX_SIGN_IN_ATTEMPTS;

    /**
     * 缓存对象.
     */
    private Cache cache;

    /**
     * OpenID provider.
     */
    private OpenIdProvider openIdProvider;

    public void setOpenIdProvider(final OpenIdProvider openIdProvider) {
        this.openIdProvider = openIdProvider;
    }

    public void setMaxSignInAttempts(final int maxSignInAttempts) {
        this.maxSignInAttempts = maxSignInAttempts;
    }

    public void setCache(final Cache cache) {
        this.cache = cache;
    }

    @Override
    public boolean onPreHandle(final ServletRequest request, final ServletResponse response, final Object mappedValue) throws Exception {
        if (this.isAccessAllowed(request, response, mappedValue)) {
            this.issueSuccessRedirect(request, response);
            return false;
        }
        
        String signInFailure = (String) request.getAttribute(this.getFailureKeyAttribute());
        LOGGER.debug("sign in failure = {}", signInFailure);
        if (signInFailure != null) {
            return true;
        }
        return super.onPreHandle(request, response, mappedValue);
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        // 访问登录页面
        if (this.isLoginRequest(httpReq, httpRes)) {
            // 提交数据到登录页面
            if (this.isLoginSubmission(httpReq, httpRes)) {
                LOGGER.debug("Submit to sign-in page.");
                return this.executeLogin(httpReq, httpRes);
            }

            // 直接访问登录页面
            LOGGER.debug("Show sign-in page.");
            return true;
        }

        // 访问其它页面，转发到登录页面
        LOGGER.debug("Access denied. Redirect to {}.", this.getLoginUrl());
        httpRes.sendRedirect(this.getLoginUrl());
        return false;
    }

    @Override
    protected boolean onLoginSuccess(final AuthenticationToken token, final Subject subject,
            final ServletRequest request, final ServletResponse response) throws Exception {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("subject authenticated = {}", subject.isAuthenticated());
            LOGGER.debug("subject remembered = {}", subject.isRemembered());
            LOGGER.debug("is OpenId request = {}", this.isOpenIdRequest(httpReq));
        }

        this.removeSignInToken(httpReq, httpRes);

        if (this.isOpenIdRequest(httpReq)) {
            String fullEndpointCompleteUrl = this.getServletContext().getContextPath() + this.openIdProvider.getEndpointCompletePath();
            LOGGER.debug("Redirect to {}.", fullEndpointCompleteUrl);
            httpRes.sendRedirect(fullEndpointCompleteUrl);
            return false;
        }

        LOGGER.debug("Redirect to success page.");
        return super.onLoginSuccess(token, subject, request, response);
    }

    @Override
    protected boolean onLoginFailure(final AuthenticationToken token, final AuthenticationException e, final ServletRequest request,
            final ServletResponse response) {
        LOGGER.error("Sign in failed.", e);

        if (e instanceof IncorrectCredentialsException) {
            SignInToken signInToken = (SignInToken) request.getAttribute(Constants.SIGN_IN_TOKEN_ATTRIBUTE_NAME);
            signInToken.updateTimestamp();
            signInToken.increaseAttempts();
            LOGGER.debug("sign in attemtps = {}/{}", signInToken.getAttempts(), this.maxSignInAttempts);
            if (signInToken.getAttempts() >= this.maxSignInAttempts) {
                signInToken.setCaptchaRequired(true);
            }
        }

        return super.onLoginFailure(token, e, request, response);
    }

    @Override
    protected boolean isAccessAllowed(final ServletRequest request, final ServletResponse response, final Object mappedValue) {
        boolean result = super.isAccessAllowed(request, response, mappedValue);
        LOGGER.debug("is access allowed = {}", result);
        return result;
    }

    /**
     * 是否 OpenID 请求.
     *
     * @param httpReq HTTP 请求
     *
     * @return 是否 OpenID 请求
     */
    private boolean isOpenIdRequest(final HttpServletRequest httpReq) {
        return SecurityUtils.getSubject().getSession().getAttribute("params") != null;
    }

    /**
     * 移除 SignInToken cookie 和缓存对象.
     * @param httpReq HttpServletRequest 对象
     * @param httpRes HttpServletResponse 对象
     */
    private void removeSignInToken(final HttpServletRequest httpReq, final HttpServletResponse httpRes) {
        LOGGER.debug("remove sign-in token.");
        SignInToken token = (SignInToken) httpReq.getAttribute(Constants.SIGN_IN_TOKEN_ATTRIBUTE_NAME);
        httpReq.removeAttribute(Constants.SIGN_IN_TOKEN_ATTRIBUTE_NAME);
        this.cache.remove(token.getNonce());

        CookieGenerator cg = new CookieGenerator();
        cg.setCookieName(Constants.COOKIE_NAME);
        cg.removeCookie(httpRes);
    }

}
