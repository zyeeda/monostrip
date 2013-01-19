package com.zyeeda.coala.commons.base.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
public class RevisionDomainEntity extends DomainEntity {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = 6433855715060022196L;
    
    private String creator;
    private String creatorName;
    private Date createdTime;
    private String lastModifier;
    private String lastModifierName;
    private Date lastModifiedTime;
    
    @Basic
    @Column(name = "F_CREATOR", length = 50)
    public String getCreator() {
        return this.creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    @Basic
    @Column(name = "F_CREATOR_NAME", length = 30)
    public String getCreatorName() {
        return creatorName;
    }
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREATED_TIME")
    public Date getCreatedTime() {
        return this.createdTime;
    }
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    
    @Basic
    @Column(name = "F_LAST_MODIFIER", length = 50)
    public String getLastModifier() {
        return this.lastModifier;
    }
    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }
    
    @Basic
    @Column(name = "F_LAST_MODIFIER_NAME", length = 30)
    public String getLastModifierName() {
        return lastModifierName;
    }
    public void setLastModifierName(String lastModifierName) {
        this.lastModifierName = lastModifierName;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_LAST_MODIFIED_TIME")
    public Date getLastModifiedTime() {
        return this.lastModifiedTime;
    }
    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

}
