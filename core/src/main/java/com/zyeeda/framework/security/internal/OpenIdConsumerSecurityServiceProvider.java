package com.zyeeda.framework.security.internal;

import org.apache.shiro.mgt.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;

public class OpenIdConsumerSecurityServiceProvider extends AbstractSecurityServiceProvider {
	
	private SecurityManager securityManager;

//    public OpenIdConsumerSecurityServiceProvider() {
//        this.securityManager = new ShiroSecurityManager();
//    }

//    private class ShiroSecurityManager extends DefaultWebSecurityManager {
//		public ShiroSecurityManager() {
//			super(new OpenIdConsumerRealm());
//			
//			 ((AbstractSessionManager) this.getSessionManager())
//					 .setGlobalSessionTimeout(3600000);
//		}
//	}

	@Autowired
    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public SecurityManager getSecurityManager() {
        return this.securityManager;
    }
}