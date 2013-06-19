package com.zyeeda.coala.commons.base.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 具有自动修订功能的业务领域实体类型.
 *
 * @author $Author$
 *
 */
@MappedSuperclass
public class RevisionDomainEntity extends DomainEntity {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = 6433855715060022196L;

    /**
     * 创建者.
     */
    private String creator;

    /**
     * 创建者名称.
     */
    private String creatorName;

    /**
     * 创建时间.
     */
    private Date createdTime;

    /**
     * 最后修改者.
     */
    private String lastModifier;

    /**
     * 最后修改者名称.
     */
    private String lastModifierName;

    /**
     * 最后修改时间.
     */
    private Date lastModifiedTime;

    @Basic
    @Column(name = "F_CREATOR", length = 50)
    public String getCreator() {
        return this.creator;
    }
    public void setCreator(final String creator) {
        this.creator = creator;
    }

    @Basic
    @Column(name = "F_CREATOR_NAME", length = 30)
    public String getCreatorName() {
        return this.creatorName;
    }
    public void setCreatorName(final String creatorName) {
        this.creatorName = creatorName;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREATED_TIME")
    public Date getCreatedTime() {
        return this.createdTime;
    }
    public void setCreatedTime(final Date createdTime) {
        this.createdTime = createdTime;
    }

    @Basic
    @Column(name = "F_LAST_MODIFIER", length = 50)
    public String getLastModifier() {
        return this.lastModifier;
    }
    public void setLastModifier(final String lastModifier) {
        this.lastModifier = lastModifier;
    }

    @Basic
    @Column(name = "F_LAST_MODIFIER_NAME", length = 30)
    public String getLastModifierName() {
        return this.lastModifierName;
    }
    public void setLastModifierName(final String lastModifierName) {
        this.lastModifierName = lastModifierName;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_LAST_MODIFIED_TIME")
    public Date getLastModifiedTime() {
        return this.lastModifiedTime;
    }
    public void setLastModifiedTime(final Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

}
