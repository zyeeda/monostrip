package com.zyeeda.framework.security.realms;

import java.io.IOException;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.framework.ldap.LdapService;

public class LdapRealm extends AuthorizingRealm {
	
	private final static Logger logger = LoggerFactory.getLogger(LdapRealm.class);
	
	private final LdapService ldapSvc;
	
	public LdapRealm(LdapService ldapSvc) {
		this.ldapSvc = ldapSvc;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		
		logger.debug("authentication token type = {}", token.getClass().getName());
		
		if (!(token instanceof UsernamePasswordToken)) {
			throw new AuthenticationException("Invalid authentication token.");
		}
		
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		logger.debug("username = {}", upToken.getUsername());
		logger.debug("password = ******");
		
		LdapContext ctx = null;
		try {
			ctx = this.ldapSvc.getLdapContext(upToken.getUsername(), new String(upToken.getPassword()));
		} catch (NamingException e) {
			throw new AuthenticationException(e);
		} catch (IOException e) {
			throw new AuthenticationException(e);
		} finally {
			LdapUtils.closeContext(ctx);
		}
		
		return new SimpleAuthenticationInfo(upToken.getUsername(), upToken.getPassword(), this.getName());
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		// TODO Auto-generated method stub
		return null;
	}

}
