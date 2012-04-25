package com.zyeeda.framework.entities;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name  = "department")
public class Department/* extends SimpleDomainEntity*/ implements Serializable {
	
	private static final long serialVersionUID = 8606771207286469030L;
	
	private String id;
	
	private String parent;
	
	private String name;
	
	private String deptFullPath;
	
	private String description;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDeptFullPath() {
		return deptFullPath;
	}

	public void setDeptFullPath(String deptFullPath) {
		this.deptFullPath = deptFullPath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
