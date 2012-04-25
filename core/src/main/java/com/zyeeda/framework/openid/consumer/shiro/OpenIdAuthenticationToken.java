package com.zyeeda.framework.openid.consumer.shiro;

import org.apache.commons.lang.StringUtils;
import org.openid4java.discovery.Identifier;

public class OpenIdAuthenticationToken extends PasswordFreeAuthenticationToken {

	private static final long serialVersionUID = 7305997052059544245L;
	
	public OpenIdAuthenticationToken(Identifier id) {
		super(StringUtils.substringAfter(id.getIdentifier(), "id="));
	}

}
