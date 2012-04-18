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
import com.zyeeda.framework.service.AbstractService;

public class SunLdapServiceProvider extends AbstractService implements LdapService {
private static final Logger logger = LoggerFactory.getLogger(SunLdapServiceProvider.class);
    
    private static final String DEFAULT_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String DEFAULT_SECURITY_AUTHENTICATION = "simple";
    private static final String DEFAULT_SECURITY_PRINCIPAL_TEMPLATE = "(uid=%s)";
    private static final String DEFAULT_BASE_DN = "";
    
    private String providerUrl = null;
    private String securityAuthentication = null;
    private String systemSecurityPrincipal = null;
    private String systemSecurityCredentials = null;
    private String securityPrincipalTemplate = null;
    private String baseDn = null;
    
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public void setSecurityAuthentication(String securityAuthentication) {
        this.securityAuthentication = securityAuthentication;
    }

    public void setSystemSecurityPrincipal(String systemSecurityPrincipal) {
        this.systemSecurityPrincipal = systemSecurityPrincipal;
    }

    public void setSystemSecurityCredentials(String systemSecurityCredentials) {
        this.systemSecurityCredentials = systemSecurityCredentials;
    }

    public void setSecurityPrincipalTemplate(String securityPrincipalTemplate) {
        this.securityPrincipalTemplate = securityPrincipalTemplate;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    @Override
    public void start() throws Exception {
        if (this.securityAuthentication == null ) {
            this.securityAuthentication = DEFAULT_SECURITY_AUTHENTICATION;
        }
        if (this.securityPrincipalTemplate == null ) {
            this.securityPrincipalTemplate = DEFAULT_SECURITY_PRINCIPAL_TEMPLATE;
        }
        if (this.baseDn == null ) {
            this.baseDn = DEFAULT_BASE_DN;
        }
        
        logger.debug("provider url = {}", this.providerUrl);
        logger.debug("security authentication = {}", this.securityAuthentication);
        logger.debug("system security principal = {}", this.systemSecurityPrincipal);
        logger.debug("system security credentials = ******");
        logger.debug("security principal template = {}", this.securityPrincipalTemplate);
        logger.debug("base dn = {}", this.baseDn);
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
		logger.debug("username = {}", username);
		logger.debug("password = ******");
		
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
