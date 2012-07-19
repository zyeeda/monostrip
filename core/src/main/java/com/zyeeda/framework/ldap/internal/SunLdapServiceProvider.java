package com.zyeeda.framework.ldap.internal;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.framework.ldap.LdapService;
import com.zyeeda.framework.ldap.LdapServiceException;
import com.zyeeda.framework.ldap.SearchControlsFactory;

public class SunLdapServiceProvider implements LdapService {
	
	private static final Logger logger = LoggerFactory.getLogger(SunLdapServiceProvider.class);
	
    private static final String DEFAULT_SECURITY_AUTHENTICATION = "simple";
    private static final String DEFAULT_SECURITY_PRINCIPAL_TEMPLATE = "(uid=%s)";
    private static final String DEFAULT_BASE_DN = "";
	
	private static final String DEFAULT_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	
	private String providerUrl;
    private String securityAuthentication;
    private String systemSecurityPrincipal;
    private String systemSecurityCredentials;
    private String securityPrincipalTemplate;
    private String baseDn;
	
	public String getBaseDn() {
	    if(null == baseDn) {
	        baseDn = DEFAULT_BASE_DN;
	    }
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public String getSecurityAuthentication() {
        if(null == securityAuthentication) {
            securityAuthentication = DEFAULT_SECURITY_AUTHENTICATION;
        }
        return securityAuthentication;
    }

    public void setSecurityAuthentication(String securityAuthentication) {
        this.securityAuthentication = securityAuthentication;
    }

    public String getSystemSecurityPrincipal() {
        return systemSecurityPrincipal;
    }

    public void setSystemSecurityPrincipal(String systemSecurityPrincipal) {
        this.systemSecurityPrincipal = systemSecurityPrincipal;
    }

    public String getSystemSecurityCredentials() {
        return systemSecurityCredentials;
    }

    public void setSystemSecurityCredentials(String systemSecurityCredentials) {
        this.systemSecurityCredentials = systemSecurityCredentials;
    }

    public String getSecurityPrincipalTemplate() {
        if(null == securityPrincipalTemplate) {
            securityAuthentication = DEFAULT_SECURITY_PRINCIPAL_TEMPLATE;
        }
        return securityPrincipalTemplate;
    }

    public void setSecurityPrincipalTemplate(String securityPrincipalTemplate) {
        this.securityPrincipalTemplate = securityPrincipalTemplate;
    }

	@Override
	public LdapContext getLdapContext() throws NamingException {
		Hashtable<String, String> env = this.setupEnvironment();
		env.put(Context.SECURITY_PRINCIPAL, this.systemSecurityPrincipal);
		env.put(Context.SECURITY_CREDENTIALS, this.systemSecurityCredentials);
		return new InitialLdapContext(env, null);
	}

	@Override
	public LdapContext getLdapContext(String username, String password)	throws NamingException, IOException {
		LdapContext ctx = null;
		try {
			ctx = this.getLdapContext();
			SearchControls sc = SearchControlsFactory.getSearchControls(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<SearchResult> ne = ctx.search(this.baseDn, String.format(this.securityPrincipalTemplate, username), sc);
			SearchResult result = ne.hasMore() ? ne.next() : null;
			if (result == null) {
				throw new NamingException("User not found.");
			}
			if (ne.hasMore()) {
				throw new NamingException("More than one user has the same name.");
			}
			
			String principal = result.getNameInNamespace();
			logger.debug("searched principal = {}", principal);
			
			Hashtable<String, String> env = this.setupEnvironment();
			env.put(Context.SECURITY_PRINCIPAL, principal);
			env.put(Context.SECURITY_CREDENTIALS, password);
			return new InitialLdapContext(env, null);
		} finally {
			LdapUtils.closeContext(ctx);
		}
	}
	
	private Hashtable<String, String> setupEnvironment() {
		Hashtable<String, String> env = new Hashtable<String, String>();
		
		env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_INITIAL_CONTEXT_FACTORY);
		if (StringUtils.isBlank(this.providerUrl)) {
			throw new LdapServiceException("The provider url must be specified in form of ldap(s)://<hostname>:<port>/<baseDN>");
		}
		env.put(Context.PROVIDER_URL, this.providerUrl);
		
		env.put(Context.SECURITY_AUTHENTICATION, this.securityAuthentication);
		return env;
	}

}
