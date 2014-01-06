package com.zyeeda.coala.commons.resource.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.zyeeda.coala.commons.base.entity.DomainEntity;
import com.zyeeda.coala.commons.hibernate.listeners.DeleteAttachmentFileEventListener;

/**
 * @author guyong
 *
 */
@Entity
@Table(name = "ZDA_ATTACHMENT")
@EntityListeners(DeleteAttachmentFileEventListener.class)
public class Attachment extends DomainEntity {

    private static final long serialVersionUID = -2215059798590231842L;

    private String path = null;
    private String filename = null;
    private String contentType = null;
    private Boolean draft = true;
    private Date createTime = new Date();

    @Column(name = "F_PATH")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Column(name = "F_FILENAME")
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Column(name = "F_DRAFT")
    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    @Column(name = "F_CREATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Column(name = "F_CONTENT_TYPE")
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
