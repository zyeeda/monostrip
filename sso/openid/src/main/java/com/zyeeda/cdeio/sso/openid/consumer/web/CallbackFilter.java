/* $Id$ */
package com.zyeeda.cdeio.sso.openid.consumer.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.zyeeda.cdeio.sso.openid.BaseFilter;

/**
 * Callback filter.
 *
 * @author $Author$
 *
 */
public class CallbackFilter extends BaseFilter {

    @Override
    protected boolean preHandle(final ServletRequest request, final ServletResponse response) throws Exception {
        Map<String, Object> args = new HashMap<String, Object>(2);
        args.put("url", request.getParameter("_url"));
        args.put("method", request.getParameter("_method"));
        this.paintTemplate(response, "callback.ftl", args);
        return false;
    }
}
