package com.zyeeda.cdeio.sso.openid;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletResponse;

import org.apache.shiro.web.servlet.AdviceFilter;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

/**
 * Base filter.
 *
 * @author $Author$
 */
public class BaseFilter extends AdviceFilter {

    /**
     * FreeMarker configuration object.
     */
    private Configuration configuration;

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    protected Configuration getConfiguration() {
        return this.configuration;
    }

    protected void paintTemplate(ServletResponse response, String template,
                                 Map<String, Object> args) throws IOException, TemplateException {
        TemplateUtil.paintTemplate(response,
                this.getConfiguration().getTemplate(template), args);
    }

    protected void paintTemplate(ServletResponse response, String template) throws IOException, TemplateException {
        TemplateUtil.paintTemplate(response,
                this.getConfiguration().getTemplate(template));
    }

}
