package com.zyeeda.framework.security.internal;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import com.zyeeda.framework.openid.consumer.shiro.OpenIdConsumerRealm;

public class OpenIdConsumerSecurityServiceProvider extends AbstractSecurityServiceProvider {
	
	private SecurityManager securityMgr;

	public OpenIdConsumerSecurityServiceProvider() {
		this.securityMgr = new ShiroSecurityManager();
	}

	@Override
	public SecurityManager getSecurityManager() {
		return this.securityMgr;
	}
	
	private class ShiroSecurityManager extends DefaultWebSecurityManager {
		public ShiroSecurityManager() {
			super(new OpenIdConsumerRealm());
		}
	}

}
