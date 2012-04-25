package com.zyeeda.framework.entities;

import java.io.Serializable;

public class Account implements Serializable {

	private static final long serialVersionUID = -7523580183398096125L;

	private String userName;
	private String password;
	private String systemName;
	private String userFullPath;
	private Boolean status = false;

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserFullPath() {
		return this.userFullPath;
	}

	public void setUserFullPath(String userFullPath) {
		this.userFullPath = userFullPath;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}
}
