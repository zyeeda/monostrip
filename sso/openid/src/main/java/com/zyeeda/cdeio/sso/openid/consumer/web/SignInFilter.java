/* $Id$ */

package com.zyeeda.cdeio.sso.openid.consumer.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.util.WebUtils;
import org.openid4java.message.AuthRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Template;

/**
 * Sign-in filter.
 *
 * @author $Author$
 *
 */
public class SignInFilter extends BaseFilter {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SignInFilter.class);

    @Override
    protected boolean preHandle(final ServletRequest request, final ServletResponse response) throws Exception {
        AuthRequest authReq = (AuthRequest) request.getAttribute("message");
        Map<String, Object> args = new HashMap<String, Object>();
        if (authReq == null) { // 用户已登录
            LOGGER.debug("Subject has already signed in.");
            WebUtils.issueRedirect(request, response, this.getOpenIdConsumer().getProviderSignInUrl());
            return false;
        }

        // 用户请求登录
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sign in using OpenID.");
            LOGGER.debug("params = {}", authReq.getParameterMap());
            LOGGER.debug("op endpoint = {}", authReq.getOPEndpoint());
        }
        args.put("authReq", authReq);
        args.put("applicationName", this.getOpenIdConsumer().getName());
        args.put("signOutUrl", this.getOpenIdConsumer().getSignOutUrl());
        args.put("indexUrl", this.getOpenIdConsumer().getIndexUrl());

        Template template = this.getConfiguration().getTemplate("signin.ftl");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        template.process(args, response.getWriter());
        return false;
    }
}
