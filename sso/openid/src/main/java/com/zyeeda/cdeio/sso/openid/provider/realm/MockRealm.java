/* $Id: MockRealm.java,v 49501679b751 2013/09/02 16:18:32 tangrui $ */

package com.zyeeda.cdeio.sso.openid.provider.realm;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import com.zyeeda.cdeio.commons.crypto.BCrypt;
import com.zyeeda.cdeio.commons.organization.entity.Account;

/**
 * Mock realm.
 * 当用户名和密码相同的时候，该 realm 即认证通过.
 *
 * @author $Author$
 *
 */
public class MockRealm extends AbstractRealm {

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) {
        this.ensureAuthenticationToken(token);

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String userName = upToken.getUsername();
        String password = new String(upToken.getPassword());
        if (StringUtils.equals(userName, password)) {
            Account account = new Account();
            account.setAccountName(userName);
            account.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            return new SimpleAuthenticationInfo(account, account.getPassword(), this.getName());
        }

        return null;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            final PrincipalCollection principals) {
        return null;
    }


}
