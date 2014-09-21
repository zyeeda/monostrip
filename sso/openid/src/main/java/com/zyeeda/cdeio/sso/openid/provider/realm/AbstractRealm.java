/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.cdeio.sso.openid.ShiroRealm;

/**
 * Abstract realm.
 *
 * @author $Author$
 *
 */
public abstract class AbstractRealm extends ShiroRealm {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractRealm.class);

    /**
     * 对 AuthenticationToken 对象进行确认.
     *
     * @param token 待确认的 AuthenticationToken 对象.
     */
    protected void ensureAuthenticationToken(final AuthenticationToken token) {
        LOGGER.debug("authentication token type = {}", token.getClass()
                .getName());

        if (!(token instanceof UsernamePasswordToken)) {
            throw new AuthenticationException(
                    "Invalid type of authentication token. We support only UsernamePasswordToken now.");
        }
    }

}
