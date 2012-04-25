package com.zyeeda.framework.openid.consumer.shiro;

import org.apache.shiro.authc.AuthenticationToken;

public class PasswordFreeAuthenticationToken implements AuthenticationToken {

	private static final long serialVersionUID = 6738193152053272419L;
	
	private String userId;
	
	public PasswordFreeAuthenticationToken(String userId) {
		this.userId = userId;
	}

	@Override
	public Object getPrincipal() {
		return this.userId;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

}
