/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.web;

import org.apache.shiro.web.servlet.AdviceFilter;

import freemarker.template.Configuration;

/**
 * Base filter.
 *
 * @author $Author$
 *
 */
public class BaseFilter extends AdviceFilter {

    /**
     * FreeMarker 配置对象.
     */
    private Configuration configuration;

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    protected Configuration getConfiguration() {
        return this.configuration;
    }

}
