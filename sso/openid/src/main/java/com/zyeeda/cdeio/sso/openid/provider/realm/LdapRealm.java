/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.realm;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextCallback;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.CollectingAuthenticationErrorCallback;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;

/**
 * 基于 LDAP 的 realm 实现.
 *
 * @author $Author$
 *
 */
public class LdapRealm extends AbstractRealm {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(LdapRealm.class);

    /**
     * LDAP 模板.
     */
    private final LdapTemplate ldapTemplate;

    /**
     * 构造方法.
     *
     * @param ldapTemplate LDAP 模板
     */
    public LdapRealm(final LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            final AuthenticationToken token) {

        this.ensureAuthenticationToken(token);

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        char[] password = upToken.getPassword();
        LOGGER.debug("user name = {}", username);

        Filter f = new EqualsFilter("uid", username);

        AuthenticatedLdapEntryContextCallback contextCallback = new AuthenticatedLdapEntryContextCallback() {
            public void executeWithContext(final DirContext ctx,
                    final LdapEntryIdentification ldapEntryIdentification) {
                try {
                    ctx.lookup(ldapEntryIdentification.getRelativeDn());
                } catch (NamingException e) {
                    throw new AuthenticationException("Failed to lookup "
                            + ldapEntryIdentification.getRelativeDn(), e);
                }
            }
        };

        CollectingAuthenticationErrorCallback errorCallback = new CollectingAuthenticationErrorCallback();

        boolean success = this.ldapTemplate.authenticate(
                DistinguishedName.EMPTY_PATH, f.toString(),
                new String(upToken.getPassword()), contextCallback,
                errorCallback);

        LOGGER.debug("Ldap authenticate result = {}", success);
        if (!success) {
            Exception error = errorCallback.getError();
            throw new AuthenticationException(error);
        }

        LOGGER.info("Create simple authentication info using provided user name and password.");
        return new SimpleAuthenticationInfo(username, password, this.getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            final PrincipalCollection principals) {
        return null;
    }

}
