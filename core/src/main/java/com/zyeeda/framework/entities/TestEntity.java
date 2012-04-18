package com.zyeeda.framework.entities;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "testEntity")
public class TestEntity {

	private String name;
	
	private List<TestEntity> children;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TestEntity> getChildren() {
		return children;
	}

	public void setChildren(List<TestEntity> children) {
		this.children = children;
	}
	
}
