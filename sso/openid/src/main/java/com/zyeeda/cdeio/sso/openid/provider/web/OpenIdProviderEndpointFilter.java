/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.web;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import com.zyeeda.cdeio.commons.organization.entity.Account;
import com.zyeeda.cdeio.sso.openid.provider.ActiveSite;
import com.zyeeda.cdeio.sso.openid.provider.OpenIdProvider;
import com.zyeeda.cdeio.sso.openid.provider.util.SessionUtil;

/**
 * OpendID endpoint 过滤器.
 *
 * @author $Author$
 *
 */
public class OpenIdProviderEndpointFilter extends PathMatchingFilter implements ServletContextAware {

    /**
     * 日志组件.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdProviderEndpointFilter.class);

    /**
     * OpenID 命名空间.
     */
    private static final String OPENID_NAMESPACE = "http://specs.openid.net/auth/2.0";

    /**
     * OpenID provider 对象.
     */
    private OpenIdProvider openIdProvider;

    public void setOpenIdProvider(final OpenIdProvider openIdProvider) {
        this.openIdProvider = openIdProvider;
    }

    @Override
    protected boolean onPreHandle(final ServletRequest request, final ServletResponse response, final Object mappedValue) throws Exception {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        ParameterList params = null;
        if (this.pathsMatch(this.openIdProvider.getEndpointCompletePath(), httpReq)) {
            LOGGER.debug("OpenID provider endpoint complete request detected.");
            params = (ParameterList) SecurityUtils.getSubject().getSession().getAttribute("params");
            if (params == null) {
                this.outputInvalidAuthRequestMessage(httpRes);
                return false;
            }
        } else {
            LOGGER.debug("OpenID provider endpoint direct request detected.");
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
        LOGGER.debug("OpenID mode = {}", mode);

        if ("associate".equals(mode)) {
            LOGGER.debug("OpenID request mode [associate] detected!");
            Message message = this.openIdProvider.associateRequest(params);
            this.outputMessage(message, httpRes);
            return false;
        }

        if ("check_authentication".equals(mode)) {
            LOGGER.debug("OpenID request mode [check_authentication] detected!");
            Message message = this.openIdProvider.verifyRequest(params);
            this.outputMessage(message, httpRes);
            return false;
        }

        if ("checkid_setup".equals(mode) || "checkid_immediate".equals(mode)) {
            LOGGER.debug("OpenID request mode [checkid_immediate] or [checkid_setup] detected!");
            Subject subject = SecurityUtils.getSubject();
            if (subject.isAuthenticated()) {
                LOGGER.debug("User is authenticated.");

                Session session = SecurityUtils.getSubject().getSession();
                session.removeAttribute("params");
                this.registerSite(session, params);

                Account account = (Account) subject.getPrincipal();
                String userSelectedId = account.getAccountName();
                userSelectedId = this.openIdProvider.getUserUrl(userSelectedId);
                String userSelectedClaimedId = userSelectedId;
                String fullEndpointUrl = this.openIdProvider.getEndpointUrl();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("user selected id = {}", userSelectedId);
                    LOGGER.debug("user selected claimed id = {}", userSelectedClaimedId);
                    LOGGER.debug("full endpoint url = {}", fullEndpointUrl);
                }

                Message message = this.openIdProvider.authResponse(params, userSelectedId, userSelectedClaimedId, subject.isAuthenticated(), fullEndpointUrl);
                if (message instanceof AuthSuccess) {
                    httpRes.sendRedirect(message.getDestinationUrl(true));
                    return false;
                }

                httpRes.sendRedirect(message.getDestinationUrl(true));
                return false;
            }

            SecurityUtils.getSubject().getSession().setAttribute("params", params);

            LOGGER.debug("session id = {}", SecurityUtils.getSubject().getSession().getId());
            return true;
        }

        LOGGER.debug("Unknown OpenID request mode [{}]", mode);
        this.outputInvalidAuthRequestMessage(httpRes);
        return false;
    }

    /**
     * 向 session 中注册站点.
     *
     * @param session session 对象
     * @param params 请求参数
     */
    private void registerSite(final Session session, final ParameterList params) {
        String applicationName = params.getParameterValue("applicationName");
        Date signInDate = new Date();
        LOGGER.debug("Register active site {} at {}", applicationName, signInDate);

        ActiveSite site = new ActiveSite();
        site.setName(applicationName);
        site.setSignInTime(signInDate);
        site.setIndexUrl(params.getParameterValue("indexUrl"));
        site.setSignOutUrl(params.getParameterValue("signOutUrl"));

        SessionUtil.storeSite(session, site);
    }

    /**
     * 输出信息.
     *
     * @param message 信息内容
     * @param httpRes HTTP 响应
     *
     * @throws IOException IO 异常
     */
    private void outputMessage(final Message message, final HttpServletResponse httpRes) throws IOException {
        String messageText = message.keyValueFormEncoding();
        httpRes.getWriter().print(messageText);
    }

    /**
     * 输出错误请求消息.
     *
     * @param message 消息内容
     * @param httpRes HTTP 响应
     *
     * @throws IOException IO 异常
     */
    private void outputBadRequestMessage(final Message message, final HttpServletResponse httpRes) throws IOException {
        httpRes.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        this.outputMessage(message, httpRes);
    }

    /**
     * 输出无效认证请求消息.
     *
     * @param httpRes HTTP 响应
     *
     * @throws IOException IO 异常
     */
    private void outputInvalidAuthRequestMessage(final HttpServletResponse httpRes) throws IOException {
        Message message = DirectError.createDirectError("Invalid OpenID auth request!");
        this.outputBadRequestMessage(message, httpRes);
    }

}
