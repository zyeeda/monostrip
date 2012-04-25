package com.zyeeda.framework.viewmodels;

import java.util.ArrayList;
import java.util.List;



public class PermissionVo {
	
	private String id;
	private String name;
	private String value;
	private Boolean isHaveIO;
	private int path;
	private String orderBy;

	private  List<PermissionVo> permissionList = new ArrayList<PermissionVo>();

	public List<PermissionVo> getPermissionList() {
		return permissionList;
	}
	
	public void setPermissionList(List<PermissionVo> permissionList) {
		this.permissionList = permissionList;
	}
	
	public String getOrderBy() {
		return orderBy;
	}


	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public int getPath() {
		return path;
	}

	public void setPath(int path) {
		this.path = path;
	}

	private Boolean checked;

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	public Boolean getIsHaveIO() {
		return isHaveIO;
	}

	public void setIsHaveIO(Boolean isHaveIO) {
		this.isHaveIO = isHaveIO;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
