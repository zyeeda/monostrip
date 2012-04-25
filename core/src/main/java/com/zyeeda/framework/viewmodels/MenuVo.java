package com.zyeeda.framework.viewmodels;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.zyeeda.framework.utils.MenuListComparator;

@XmlRootElement(name = "menuVo")
public class MenuVo {
	private String id;
	private String name;
	private String auth;
	private List<MenuVo> permissionSet = new ArrayList<MenuVo>();
	private MenuVo parentMenu;
	private String orderBy;
	
	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public MenuVo getParentMenu() {
		return parentMenu;
	}

	public List<MenuVo> getPermissionSet() {
		if(permissionSet.size() > 0) {
			MenuListComparator comparator = new  MenuListComparator();
			Collections.sort(permissionSet, comparator);
		}
		return permissionSet;
	}

	public void setPermissionSet(List<MenuVo> permissionSet) {
		this.permissionSet = permissionSet;
	}

	public void setParentMenu(MenuVo parentMenu) {
		this.parentMenu = parentMenu;
	}


	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

//	public Set<MenuVo> getPermissionSet() {
//		return permissionSet;
//	}
//
//	public void setPermissionSet(Set<MenuVo> permissionSet) {
//		this.permissionSet = permissionSet;
//	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
