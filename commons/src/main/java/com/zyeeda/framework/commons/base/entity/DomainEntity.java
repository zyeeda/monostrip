package com.zyeeda.framework.commons.base.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
public class DomainEntity implements Serializable {

    private static final long serialVersionUID = 6570499338336870036L;

    private String id;
    
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid2")
    @Column(name = "F_ID")
	public String getId() {
		return id;
	}
    
    public void setId(String id) {
		this.id = id;
	}
    
}
