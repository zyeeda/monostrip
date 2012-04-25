package com.zyeeda.framework.security.internal;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import com.zyeeda.framework.ldap.LdapService;
import com.zyeeda.framework.security.realms.LdapRealm;

public class OpenIdProviderSecurityServiceProvider extends AbstractSecurityServiceProvider {

	// Injected
	private final LdapService ldapSvc;
	private final SecurityManager securityMgr;
	
	public OpenIdProviderSecurityServiceProvider(LdapService ldapSvc) {
		this.ldapSvc = ldapSvc;
		this.securityMgr = new ShiroSecurityManager();
	}

	@Override
	public SecurityManager getSecurityManager() {
		return this.securityMgr;
	}
	
	private class ShiroSecurityManager extends DefaultWebSecurityManager {
		public ShiroSecurityManager() {
			super(new LdapRealm(ldapSvc));
		}
	}

}
