package com.zyeeda.framework.ldap;

import javax.naming.directory.SearchControls;

public class SearchControlsFactory {
	
	public static SearchControls getSearchControls(int scope) {
		SearchControls sc = SearchControlsFactory.getDefaultSearchControls();
		sc.setSearchScope(scope);
		return sc;
		
	}

	public static SearchControls getDefaultSearchControls() {
		SearchControls sc = new SearchControls();
		return sc;
	}
}
