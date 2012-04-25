package com.zyeeda.framework.ldap;

import java.io.IOException;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import com.zyeeda.framework.service.Service;

public interface LdapService extends Service {

	public LdapContext getLdapContext() throws NamingException;
	
	public LdapContext getLdapContext(String username, String password) throws NamingException, IOException;
	
}
