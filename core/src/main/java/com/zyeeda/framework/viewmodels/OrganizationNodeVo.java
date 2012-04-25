package com.zyeeda.framework.viewmodels;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "organizationNodeVo")
public class OrganizationNodeVo implements Serializable{
	
	private static final long serialVersionUID = 8606771207287369030L;
	
	private String id;
	
	private String type;
	
	private String checkName;
	
	private String label;
	
	private boolean leaf;
	
	private String io;
	
	private String fullPath;
	private Boolean checkedAuth;
	
	private Boolean checked;

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	/*标识是用户还是部门*/
	private String kind;

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

	public String getIo() {
		return io;
	}

	public void setIo(String io) {
		this.io = io;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}
	
	public Boolean getCheckedAuth() {
		return checkedAuth;
	}

	public void setCheckedAuth(Boolean checkedAuth) {
		this.checkedAuth = checkedAuth;
	}
}
