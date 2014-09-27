/* $Id$ */

package com.zyeeda.cdeio.sso.openid.consumer.web;

import org.apache.shiro.web.servlet.AdviceFilter;

import com.zyeeda.cdeio.sso.openid.consumer.OpenIdConsumer;

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

    /**
     * OpenID consumer.
     */
    private OpenIdConsumer openIdConsumer;

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    protected Configuration getConfiguration() {
        return this.configuration;
    }

    public void setOpenIdConsumer(final OpenIdConsumer openIdConsumer) {
        this.openIdConsumer = openIdConsumer;
    }

    protected OpenIdConsumer getOpenIdConsumer() {
        return this.openIdConsumer;
    }

}
