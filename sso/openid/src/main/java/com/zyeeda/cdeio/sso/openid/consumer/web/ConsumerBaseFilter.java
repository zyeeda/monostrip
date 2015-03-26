/* $Id$ */

package com.zyeeda.cdeio.sso.openid.consumer.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletResponse;

import com.zyeeda.cdeio.sso.openid.consumer.OpenIdConsumer;
import com.zyeeda.cdeio.sso.openid.TemplateUtil;
import com.zyeeda.cdeio.sso.openid.BaseFilter;

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
public class ConsumerBaseFilter extends BaseFilter {

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
