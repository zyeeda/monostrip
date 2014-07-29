package com.zyeeda.coala.commons.base.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

/**
 * 所有业务领域实体的基类.
 *
 * @author $Author$
 *
 */
@MappedSuperclass
public class DomainEntity implements Serializable {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = 6570499338336870036L;

    /**
     * 唯一标识.
     */
    private String id;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.zyeeda.coala.commons.generator.FallbackUUIDHexGenerator")
    // @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(name = "F_ID")
    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
