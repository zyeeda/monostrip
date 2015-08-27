/* $Id: Permission.java,v 42c279a9a032 2013/09/06 08:59:11 tangrui $ */

package com.zyeeda.cdeio.commons.authz.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import com.zyeeda.cdeio.commons.annotation.scaffold.Scaffold;
import com.zyeeda.cdeio.commons.base.entity.DomainEntity;

/**
 * 权限.
 *
 * @author $Author: tangrui $
 *
 */
@Entity
@Table(name = "CDE_PERMISSION")
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

    /**
     * 该权限被包含在哪些角色中.
     */
    private Set<Role> roles = new HashSet<Role>();

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

    @ManyToMany(mappedBy = "permissions")
    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }

}
