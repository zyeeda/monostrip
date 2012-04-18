package com.zyeeda.framework.utils;

import javax.naming.ldap.Control;

public class TreeDeleteControlUtils implements Control {

	private static final long serialVersionUID = 1L;

	@Override
	public byte[] getEncodedValue() {
		return null;
	}

	@Override
	public String getID() {
		return "1.2.840.113556.1.4.805";
	}

	@Override
	public boolean isCritical() {
		return Control.CRITICAL;
	}
	

}
