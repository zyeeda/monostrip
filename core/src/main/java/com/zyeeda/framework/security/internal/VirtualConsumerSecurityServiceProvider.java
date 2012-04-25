package com.zyeeda.framework.security.internal;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;

import com.zyeeda.framework.openid.consumer.shiro.OpenIdConsumerRealm;

public class VirtualConsumerSecurityServiceProvider extends AbstractSecurityServiceProvider {
	
	private SecurityManager securityMgr;

	public VirtualConsumerSecurityServiceProvider() {
		this.securityMgr = new ShiroSecurityManager();
	}

	@Override
	public SecurityManager getSecurityManager() {
		return this.securityMgr;
	}
	
	private class ShiroSecurityManager extends DefaultSecurityManager {
		public ShiroSecurityManager() {
			super(new OpenIdConsumerRealm());
		}
	}

}
