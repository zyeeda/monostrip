package com.zyeeda.framework.managers;

public class DocumentException extends Exception {

	private static final long serialVersionUID = -7159687624749433311L;
	
	public DocumentException(String message) {
		super(message);
	}
	
	public DocumentException(Throwable t) {
		super(t);
	}

}
