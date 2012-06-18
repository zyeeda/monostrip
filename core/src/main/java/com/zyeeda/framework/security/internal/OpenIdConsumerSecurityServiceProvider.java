package com.zyeeda.framework.security.internal;

import org.apache.shiro.mgt.SecurityManager;

public class OpenIdConsumerSecurityServiceProvider extends AbstractSecurityServiceProvider {
	
	private SecurityManager securityManager;

    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public SecurityManager getSecurityManager() {
        return this.securityManager;
    }
}