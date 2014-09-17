/* $Id: Role.java,v 42c279a9a032 2013/09/06 08:59:11 tangrui $ */

package com.zyeeda.cdeio.commons.authc.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import com.zyeeda.cdeio.commons.annotation.scaffold.Scaffold;
import com.zyeeda.cdeio.commons.base.entity.DomainEntity;
import com.zyeeda.cdeio.commons.organization.entity.Account;
import com.zyeeda.cdeio.commons.organization.entity.Department;

/**
 * 角色.
 *
 * @author $Author: tangrui $
 *
 */
@Entity
@Table(name = "ZDA_ROLE")
@Scaffold("/system/roles")
public class Role extends DomainEntity {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = -3317005945161985953L;

    /**
     * 角色名.
     */
    private String name = null;

    /**
     * 角色描述.
     */
    private String description = null;

    /**
     * 角色所在部门.
     */
    private Department department = null;

    /**
     * 角色包含的用户.
     */
    private List<Account> accounts = new ArrayList<Account>();

    /**
     * 角色包含的权限.
     */
    private Set<Permission> permissions = new HashSet<Permission>();

    /**
     * 是否为动态角色.
     */
    private Boolean dynamic = null;

    @Basic
    @Column(name = "F_NAME", length = 30)
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

    @ManyToOne
    @JoinColumn(name = "F_DEPARTMENT_ID")
    public Department getDepartment() {
        return this.department;
    }

    public void setDepartment(final Department department) {
        this.department = department;
    }

    @ManyToMany(mappedBy = "roles")
    public List<Account> getAccounts() {
        return this.accounts;
    }

    public void setAccounts(final List<Account> accounts) {
        this.accounts = accounts;
    }

    @ManyToMany
    @JoinTable(
            name = "ZDA_ROLE_PERMISSION",
            joinColumns = @JoinColumn(name = "F_ROLE_ID"),
            inverseJoinColumns = @JoinColumn(name = "F_PERMISSION_ID"))
    public Set<Permission> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(final Set<Permission> permissions) {
        this.permissions = permissions;
    }

    @Basic
    @Column(name = "F_SCAFFOLD")
    public Boolean getDynamic() {
        return this.dynamic;
    }

    public void setDynamic(final Boolean dynamic) {
        this.dynamic = dynamic;
    }

}
