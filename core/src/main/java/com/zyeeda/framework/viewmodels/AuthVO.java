package com.zyeeda.framework.viewmodels;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "authVO")
public class AuthVO {
	private String type;
	private String id;
	private String label;
	private boolean leaf = false;
	private String io;
	private String tag;
	private List<AuthVO> children = new ArrayList<AuthVO>();
	
	public List<AuthVO> getChildren() {
		return children;
	}

	public void setChildren(List<AuthVO> children) {
		this.children = children;
	}

	private Boolean checked = false;

	public String getTag() {
		return tag;
	}

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getIo() {
		return io;
	}

	public void setIo(String io) {
		this.io = io;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

}
