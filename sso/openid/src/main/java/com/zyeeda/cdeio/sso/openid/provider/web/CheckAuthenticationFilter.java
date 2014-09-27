package com.zyeeda.cdeio.sso.openid.provider.web;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.cdeio.sso.openid.provider.OpenIdProvider;

/**
 * 检查用户是否登录的 filter.
 *
 * @author tangrui
 *
 */
public class CheckAuthenticationFilter extends PathMatchingFilter {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckAuthenticationFilter.class);

    /**
     * OpenID provider.
     */
    private OpenIdProvider openIdProvider;

    public void setOpenIdProvider(final OpenIdProvider openIdProvider) {
        this.openIdProvider = openIdProvider;
    }

    @Override
    protected boolean onPreHandle(final ServletRequest request, final ServletResponse response, final Object mappedValue) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated() && !subject.isRemembered()) {
            LOGGER.debug("Subject is unauthenticated.");

            if (this.pathsMatch(this.openIdProvider.getIndexPath(), request)) {
                WebUtils.issueRedirect(request, response, this.openIdProvider.getSignInPath());
            } else {
                HttpServletResponse httpRes = WebUtils.toHttp(response);
                httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return false;
        }
        return true;
    }
}
