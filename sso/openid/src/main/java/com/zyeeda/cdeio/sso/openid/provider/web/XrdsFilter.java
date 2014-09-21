/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.context.ServletContextAware;

import freemarker.template.Template;

/**
 * XRDS filter.
 *
 * @author $Author$
 *
 */
public class XrdsFilter extends OpenIdProviderAwareBaseFilter implements ServletContextAware {

    /**
     * 获取模板路劲.
     *
     * @return 模板路径
     */
    protected String getTemplatePath() {
        return "xrds.ftl";
    }

    @Override
    protected boolean preHandle(final ServletRequest request, final ServletResponse response) throws Exception {
        Map<String, Object> args = new HashMap<String, Object>(2);
        args.put("request", request);
        args.put("endpointUrl", this.getOpenIdProvider().getEndpointUrl());
        Template template = this.getConfiguration().getTemplate(this.getTemplatePath());
        response.setContentType("application/xrds+xml");
        response.setCharacterEncoding("UTF-8");
        template.process(args, response.getWriter());
        return false;
    }
}
