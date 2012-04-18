package com.zyeeda.framework.viewmodels;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "userName")
public class UserNameVo {
	
	private String userName;
	
	private String userChinaName;
	
	private String belongDept;
	
	Set<String> setRole = new HashSet<String>();
	
	public Set<String> getSetRole() {
		return setRole;
	}

	public void setSetRole(Set<String> setRole) {
		this.setRole = setRole;
	}

	public String getUserChinaName() {
		return userChinaName;
	}

	public void setUserChinaName(String userChinaName) {
		this.userChinaName = userChinaName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getBelongDept() {
		return belongDept;
	}

	public void setBelongDept(String belongDept) {
		this.belongDept = belongDept;
	}

}
