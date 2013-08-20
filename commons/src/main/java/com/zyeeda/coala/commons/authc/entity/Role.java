package com.zyeeda.coala.commons.authc.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.zyeeda.coala.commons.annotation.scaffold.Scaffold;
import com.zyeeda.coala.commons.base.entity.DomainEntity;
import com.zyeeda.coala.commons.organization.entity.Account;
import com.zyeeda.coala.commons.organization.entity.Department;

@Entity
@Table(name="ZDA_ROLE")
@Scaffold("/system/roles")
public class Role extends DomainEntity {

    private static final long serialVersionUID = -3317005945161985953L;

    private List<Permission> permissions = null;
    private String name = null;
    private String description = null;
    private Department department = null;
    private List<Account> accounts = null;
    private Boolean dynamic = null;

    @Column(name = "F_NAME", length = 200)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "F_DESC", length = 2000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne
    @JoinColumn(name = "F_DEPARTMENT")
    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @ManyToMany
    @JoinTable(name = "ZDA_ROLE_ACCOUNTS", joinColumns = @JoinColumn(name = "F_ROLE_ID"), inverseJoinColumns = @JoinColumn(name = "F_ACCOUNT_ID"))
    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    @ManyToMany
    @JoinTable(
            name="ZDA_ROLE_PERMISSION",
            joinColumns={@JoinColumn(name="F_ROLE_ID")},
            inverseJoinColumns={@JoinColumn(name="F_PERMISSION_ID")})
    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    @Column(name = "F_SCAFFOLD")
    public Boolean getDynamic() {
        return dynamic;
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }

}
