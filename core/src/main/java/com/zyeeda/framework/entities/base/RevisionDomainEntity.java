package com.zyeeda.framework.entities.base;

import java.util.Date;

import javax.persistence.TemporalType;

import com.zyeeda.framework.commons.base.entity.SimpleDomainEntity;

@javax.persistence.MappedSuperclass
public class RevisionDomainEntity extends SimpleDomainEntity {

    private static final long serialVersionUID = 2055338408696881639L;
	
    private String creator;
    private Date createdTime;
    private String lastModifier;
    private Date lastModifiedTime;
    
    @javax.persistence.Basic
    @javax.persistence.Column(name = "F_CREATOR", length = 50)
    public String getCreator() {
        return this.creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    @javax.persistence.Temporal(TemporalType.TIMESTAMP)
    @javax.persistence.Column(name = "F_CREATED_TIME")
    public Date getCreatedTime() {
        return this.createdTime;
    }
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    
    @javax.persistence.Basic
    @javax.persistence.Column(name = "F_LAST_MODIFIER", length = 50)
    public String getLastModifier() {
        return this.lastModifier;
    }
    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }
    
    @javax.persistence.Temporal(TemporalType.TIMESTAMP)
    @javax.persistence.Column(name = "F_LAST_MODIFIED_TIME")
    public Date getLastModifiedTime() {
        return this.lastModifiedTime;
    }
    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }
    
    public void prePersist(RevisionDomainEntity e) {
    }
	
}
