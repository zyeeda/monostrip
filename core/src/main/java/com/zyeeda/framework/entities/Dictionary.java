package com.zyeeda.framework.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.zyeeda.framework.entities.base.SimpleDomainEntity;

@Entity
@Table(name = "ZDA_SYS_DICT")
@XmlRootElement(name = "dict")
public class Dictionary extends SimpleDomainEntity {

	private static final long serialVersionUID = 5516157716776374792L;

	@Column(name = "F_VALUE")
	private String value;
	
	@Column(name = "F_TYPE")
	private String type;
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}

}
