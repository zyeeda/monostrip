package com.zyeeda.cdeio.sso.openid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletResponse;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateUtil {

    public static void paintTemplate(ServletResponse response,
                                     Template template, Map<String, Object> args)
            throws TemplateException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        if (args == null) {
            args = new HashMap<String, Object>();
        }
        template.process(args, response.getWriter());
    }

    public static void paintTemplate(ServletResponse response, Template template) throws IOException, TemplateException {
        paintTemplate(response, template, null);
    }

}
