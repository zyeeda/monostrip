package com.zyeeda.framework.entities.base;

import java.io.Serializable;

@javax.persistence.MappedSuperclass
public class DomainEntity implements Serializable {

    private static final long serialVersionUID = 6570499338336870036L;

    private String id;
    @javax.persistence.Id
    @javax.persistence.Column(name = "F_ID")
    @javax.persistence.GeneratedValue(generator="system-uuid")
    @org.hibernate.annotations.GenericGenerator(name="system-uuid", strategy = "uuid")
	public String getId() {
		return id;
	}
	
    public void setId(String id) {
		this.id = id;
	}
}
