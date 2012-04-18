package com.zyeeda.framework.managers;

public class UserPersistException extends Exception {
	
	private static final long serialVersionUID = 8402227533625324307L;

	public UserPersistException() {}
	
	public UserPersistException(String message) {
		super(message);
	}
	
	public UserPersistException(Throwable t) {
		super(t);
	}
	
}
