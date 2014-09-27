/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.web;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sign-out filter.
 *
 * @author $Author$
 *
 */
public class SignOutFilter extends OpenIdProviderAwareBaseFilter {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SignOutFilter.class);

    @Override
    protected boolean preHandle(final ServletRequest request, final ServletResponse response) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.logout();
        } catch (SessionException e) {
            LOGGER.debug("Encountered session exception during logout. This can generally safely be ignored.", e);
        }
        String returnTo = request.getParameter("returnTo");
        if (StringUtils.isNotBlank(returnTo)) {
            WebUtils.issueRedirect(request, response, returnTo);
        } else {
            WebUtils.issueRedirect(request, response, this.getOpenIdProvider().getSignInPath());
            /*Template template = this.getConfiguration().getTemplate("signout.ftl");
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            template.process(null, response.getWriter());*/
        }
        return false;
    }
}
