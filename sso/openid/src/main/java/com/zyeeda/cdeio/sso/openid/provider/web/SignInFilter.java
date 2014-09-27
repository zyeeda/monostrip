/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.SecurityUtils;
import org.openid4java.message.ParameterList;
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
        LOGGER.debug("Show sign-in page.");

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("request", request);
        ParameterList params = (ParameterList) SecurityUtils.getSubject().getSession().getAttribute("params");
        if (params != null) {
            args.put("applicationName", params.getParameterValue("applicationName"));
        }

        Template template = this.getConfiguration().getTemplate("signin.ftl");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        template.process(args, response.getWriter());
        return false;
    }
}
