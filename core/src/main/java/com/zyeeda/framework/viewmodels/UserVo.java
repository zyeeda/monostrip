package com.zyeeda.framework.viewmodels;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userVo")
public class UserVo implements Serializable{
	
	private static final long serialVersionUID = 8606771207287369030L;
	
	private String id;
	
	private String type;
	
	private String checkName;
	
	private String label;
	
	
	
	private String uid;
	
	private boolean leaf;
	
	private String deptFullPath;

    private String kind;
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCheckName() {
		return checkName;
	}

	public void setCheckName(String checkName) {
		this.checkName = checkName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public String getDeptFullPath() {
		return deptFullPath;
	}

	public void setDeptFullPath(String deptFullPath) {
		this.deptFullPath = deptFullPath;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}
	

}
