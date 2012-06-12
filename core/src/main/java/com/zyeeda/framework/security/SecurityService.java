package com.zyeeda.framework.security;


public interface SecurityService<T> {

	public T getSecurityManager();
	
	public String getCurrentUser();

	
}
