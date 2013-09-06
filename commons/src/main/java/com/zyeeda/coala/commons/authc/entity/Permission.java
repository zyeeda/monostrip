/* $Id$ */

package com.zyeeda.coala.commons.authc.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import com.zyeeda.coala.commons.annotation.scaffold.Scaffold;
import com.zyeeda.coala.commons.base.entity.DomainEntity;

/**
 * 权限.
 *
 * @author $Author$
 *
 */
@Entity
@Table(name = "ZDA_PERMISSION")
@Scaffold("/system/permissions")
public class Permission extends DomainEntity {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = -1467393705578439897L;

    /**
     * 权限名称.
     */
    private String name = null;

    /**
     * 权限描述.
     */
    private String description = null;

    /**
     * 权限值.
     */
    private String value = null;

    /**
     * 是否自动生成.
     */
    private Boolean scaffold = null;

    @Basic
    @Column(name = "F_NAME", length = 100)
    @NotBlank
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "F_DESC", length = 2000)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "F_VALUE", length = 200)
    @NotBlank
    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Basic
    @Column(name = "F_SCAFFOLD")
    public Boolean getScaffold() {
        return this.scaffold;
    }

    public void setScaffold(final Boolean scaffold) {
        this.scaffold = scaffold;
    }

}
