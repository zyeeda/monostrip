package com.zyeeda.framework.web;

import org.springframework.beans.factory.annotation.Autowired;

import com.zyeeda.framework.security.SecurityService;

public class OpenIdProviderSecurityFilter extends SecurityFilter {

    private SecurityService<?> securityService = null;
    
    @Autowired
    public void setSecurityService(SecurityService<?> securityService) {
        this.securityService = securityService;
    }
    
	@Override
	protected SecurityService<?> getSecurityService() {
		return securityService;
	}

}
