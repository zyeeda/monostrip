/* $Id$ */

package com.zyeeda.cdeio.sso.openid.consumer.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Template;

/**
 * Sign-out filter.
 *
 * @author $Author$
 *
 */
public class SignOutFilter extends BaseFilter {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SignOutFilter.class);

    @Override
    protected boolean preHandle(final ServletRequest request, final ServletResponse response) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.logout();
        } catch (SessionException e) {
            LOGGER.debug("Encountered session exception during logout. This can generally safely be ignored.", e);
        }

        String dataType = request.getParameter("dataType");
        Map<String, String> params = new HashMap<String, String>();
        params.put("dataType", dataType);
        params.put("applicationName", this.getOpenIdConsumer().getName());
        params.put("signInUrl", this.getOpenIdConsumer().getSignInPath());
        params.put("opSignOutUrl", this.getOpenIdConsumer().getProviderSignOutUrl());
        Template template = this.getConfiguration().getTemplate("signout.ftl");
        if ("jsonp".equals(dataType)) {
            response.setContentType("application/json");
        } else {
            response.setContentType("text/html");
        }
        response.setCharacterEncoding("UTF-8");
        template.process(params, response.getWriter());
        return false;
    }
}
