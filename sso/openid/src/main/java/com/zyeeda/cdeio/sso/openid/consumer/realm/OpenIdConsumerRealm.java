/* $Id$ */

package com.zyeeda.cdeio.sso.openid.consumer.realm;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.cdeio.commons.organization.entity.Account;
import com.zyeeda.cdeio.sso.openid.ShiroRealm;
import com.zyeeda.cdeio.sso.openid.consumer.support.OpenIdAuthenticationToken;

/**
 * OpenID consumer realm.
 *
 * @author $Author$
 *
 */
public class OpenIdConsumerRealm extends ShiroRealm {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdConsumerRealm.class);

    /**
     * 默认构造方法.
     */
    public OpenIdConsumerRealm() {
        this.setAuthenticationTokenClass(OpenIdAuthenticationToken.class);
        this.setCredentialsMatcher(new AllowAllCredentialsMatcher());
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) {
        LOGGER.debug("authentication token type = {}", token.getClass()
                .getName());

        String principal = (String) token.getPrincipal();
        Account account = this.getAccount("sso.openid.realm.findAccountByName", principal);
        if (account == null) {
            return null;
        }

        LOGGER.info("Create simple authentication info without password.");
        return new SimpleAuthenticationInfo(account, null, this.getName());
    }

}
