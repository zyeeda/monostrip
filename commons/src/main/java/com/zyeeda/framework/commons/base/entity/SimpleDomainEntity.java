package com.zyeeda.framework.commons.base.entity;

import javax.persistence.Basic;
import javax.persistence.Column;


@javax.persistence.MappedSuperclass
public class SimpleDomainEntity extends DomainEntity {

    private static final long serialVersionUID = -2200108673372668900L;
	
    private String name;
    private String description;
    
    @Basic
    @Column(name = "F_NAME")
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @Basic
    @Column(name = "F_DESC", length = 2000)
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

}
