/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.realm;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.cdeio.commons.organization.entity.Account;

/**
 * 基于 JPA 的 realm 实现.
 *
 * @author $Author$
 *
 */
public class JpaRealm extends AbstractRealm {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(JpaRealm.class);

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) {
        this.ensureAuthenticationToken(token);

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        LOGGER.debug("account name = {}", username);

        Account account = this.getAccount("sso.openid.realm.findAccountByName", username);
        if (account == null) {
            return null;
        }
        if (account.getDisabled() != null && account.getDisabled()) {
            LOGGER.info("Account [{}] is disabled.", username);
            throw new DisabledAccountException();
        }

        LOGGER.info("Create simple authentication info using provided user name and password.");
        return new SimpleAuthenticationInfo(account, account.getPassword(), this.getName());
    }

}
