package com.zyeeda.framework.viewmodels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "menuAndPermission")
public class MenuAndPermission {
	
	private List<MenuVo> listMenu = new ArrayList<MenuVo>();
	
	private List<PermissionVo> listPermission = new ArrayList<PermissionVo>();
	
	private String userName;
	
	private String deptName;
	
	private String parentDeptName;
	
	public String getParentDeptName() {
		return parentDeptName;
	}

	public void setParentDeptName(String parentDeptName) {
		this.parentDeptName = parentDeptName;
	}

	private Set<String> menuValue = new HashSet<String>();

	public Set<String> getMenuValue() {
		return menuValue;
	}

	public void setMenuValue(Set<String> menuValue) {
		this.menuValue = menuValue;
	}
	
	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<MenuVo> getListMenu() {
		return listMenu;
	}

	public void setListMenu(List<MenuVo> listMenu) {
		this.listMenu = listMenu;
	}

	public List<PermissionVo> getListPermission() {
		return listPermission;
	}

	public void setListPermission(List<PermissionVo> listPermission) {
		this.listPermission = listPermission;
	}
}
