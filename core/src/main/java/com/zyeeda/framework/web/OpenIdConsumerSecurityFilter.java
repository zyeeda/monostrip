package com.zyeeda.framework.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.zyeeda.framework.security.SecurityService;

public class OpenIdConsumerSecurityFilter extends SecurityFilter {

    private SecurityService<?> securityService = null;
    
    @Autowired @Qualifier("openIdConsumerSecurityService")
	public void setSecurityService(SecurityService<?> securityService) {
        this.securityService = securityService;
    }

    @Override
	protected SecurityService<?> getSecurityService() {
		return securityService;
	}

}
