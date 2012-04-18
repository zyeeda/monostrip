package com.zyeeda.framework.viewmodels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "roleWithUserVo")
public class RoleWithUserVo {
	private Set<UserNameVo> userName = new HashSet<UserNameVo>();
	
	private List<PermissionVo> permission = new ArrayList<PermissionVo>();
	
	public List<PermissionVo> getPermission() {
		return permission;
	}

	public void setPermission(List<PermissionVo> permission) {
		this.permission = permission;
	}

	public Set<UserNameVo> getUserName() {
		return userName;
	}

	public void setUserName(Set<UserNameVo> userName) {
		this.userName = userName;
	}



	

}
