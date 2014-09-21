/* $Id$ */
package com.zyeeda.cdeio.sso.openid.consumer.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.servlet.AdviceFilter;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Callback filter.
 *
 * @author $Author$
 *
 */
public class CallbackFilter extends AdviceFilter {

    /**
     * FreeMarker 配置对象.
     */
    private Configuration configuration;

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected boolean preHandle(final ServletRequest request, final ServletResponse response) throws Exception {
        Map<String, String> args = new HashMap<String, String>(2);
        args.put("url", request.getParameter("_url"));
        args.put("method", request.getParameter("_method"));
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        Template template = this.configuration.getTemplate("callback.ftl");
        template.process(args, response.getWriter());
        return false;
    }
}
