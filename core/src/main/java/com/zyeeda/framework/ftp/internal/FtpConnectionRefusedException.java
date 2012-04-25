package com.zyeeda.framework.ftp.internal;

public class FtpConnectionRefusedException extends Exception {

	private static final long serialVersionUID = 3210431136164484133L;

	public FtpConnectionRefusedException(String msg) {
		super(msg);
	}
}
