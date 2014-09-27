package com.zyeeda.cdeio.sso.openid.provider.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Template;

/**
 * Main filter.
 *
 * @author $Author: tangrui $
 *
 */
public class IndexFilter extends BaseFilter {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexFilter.class);

    @Override
    protected boolean preHandle(final ServletRequest request, final ServletResponse response) throws Exception {
        LOGGER.debug("Show index page.");

        Map<String, Object> args = new HashMap<String, Object>();
        Subject subject = SecurityUtils.getSubject();
        args.put("account", subject.getPrincipal());

        Template template = this.getConfiguration().getTemplate("index.ftl");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        template.process(args, response.getWriter());
        return false;
    }

}
