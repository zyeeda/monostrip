// Copyright 2011, Zyeeda, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Zyeeda, Inc.

package com.zyeeda.framework.managers.internal;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.shiro.realm.ldap.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.framework.entities.Account;
import com.zyeeda.framework.helpers.AccountHelper;
import com.zyeeda.framework.ldap.LdapService;
import com.zyeeda.framework.ldap.LdapTemplate;
import com.zyeeda.framework.ldap.SearchControlsFactory;
import com.zyeeda.framework.managers.AccountManager;
import com.zyeeda.framework.managers.UserPersistException;

/**
 * system account manager implement
 * 
 * @author Qi Zhao
 * @date 2011-06-15
 * 
 * @LastChanged
 * @LastChangedBy $LastChangedBy: $
 * @LastChangedDate $LastChangedDate: $
 * @LastChangedRevision $LastChangedRevision: $
 */
public class SystemAccountManager implements AccountManager {
	
	private static final Logger logger = LoggerFactory.getLogger(SystemAccountManager.class);

	public static final String DEFAULT_DN_PREFIX = ",dc=ehv,dc=csg,dc=cn";

	private LdapService ldapSvc;

	public SystemAccountManager(LdapService ldapSvc) {
		this.ldapSvc = ldapSvc;
	}

	public List<Account> findByUserId(String userId)
			throws UserPersistException {
		String fullDN = userId; //+ DEFAULT_DN_PREFIX;
		String filter = "(systemName=*)";
		
		logger.debug("full dn = {}", fullDN);
		logger.debug("filter = {}", filter);
		return this.getAccountsByDNAndFilter(fullDN, filter);
	}

	public Account findByUserIdAndSystemName(String userId, String systemName)
			throws UserPersistException {
		String fullDN = userId;// + DEFAULT_DN_PREFIX;
		String filter = "(systemName=" + systemName + ")";

		logger.debug("find by user id and sys name's full dn = {}", fullDN);
		logger.debug("filter = {}", filter);
		List<Account> accounts = this.getAccountsByDNAndFilter(fullDN, filter);

		if (accounts != null && accounts.size() > 0) {
			return accounts.get(0);
		}

		return null;
	}

//	public void update(Account account) throws UserPersistException {
//		LdapContext context = null;
//		try {
//			context = this.getLdapContext();
//			Attributes attributes = AccountHelper.convertAccountToAttributes(account);
//			String dn = "username=" + account.getUserName() + "," + account.getUserFullPath();
//			NamingEnumeration<SearchResult> ne = context.search(account.getUserFullPath(), "username=" + account.getUserName(),
//						   SearchControlsFactory.getSearchControls(SearchControls.SUBTREE_SCOPE));
//			if (ne.hasMore()) {
//				context.modifyAttributes(dn, DirContext.REPLACE_ATTRIBUTE, attributes);
//			} else {
//				context.bind(dn, null, attributes);
//			}
//		} catch (NamingException e) {
//			throw new UserPersistException(e);
//		}
//	}
	
	public void update(Account account) throws UserPersistException {
		Attributes attributes = AccountHelper.convertAccountToAttributes(account);
		try {
			LdapTemplate ldapTemplate = this.getLdapTemplate();
			ldapTemplate.bind("uid=" + account.getUserName() + "," + account.getUserFullPath(), attributes);
		} catch (NamingException e) {
			throw new UserPersistException(e);
		}
	}
	
	private LdapContext getLdapContext() throws NamingException {
		return this.ldapSvc.getLdapContext();
	}

	private List<Account> getAccountsByDNAndFilter(String dn, String filter)
			throws UserPersistException {
		LdapContext context = null;
		List<Account> accounts = new ArrayList<Account>();

		try {
			context = this.getLdapContext();
//			dn += ",dc=ehv,dc=csg,dc=cn";
			NamingEnumeration<SearchResult> nes = context.search(dn, filter,
					SearchControlsFactory
							.getSearchControls(SearchControls.SUBTREE_SCOPE));
			if (nes == null) {
				return accounts;
			}

			accounts = AccountHelper
					.convertNameingEnumeractionToAccountList(nes);

			return accounts;
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} finally {
			LdapUtils.closeContext(context);
		}
	}

	/*
	 * Delete a context by systemName
	 */
	public void remove(String systemName) throws UserPersistException {
		try {
			LdapTemplate ldapTemplate = this.getLdapTemplate();
			ldapTemplate.unbind(systemName, false);
		} catch (NamingException e) {
			throw new UserPersistException(e);
		}
	}	
	
	private LdapTemplate getLdapTemplate() throws NamingException {
		return new LdapTemplate(this.ldapSvc.getLdapContext());
	}
}
