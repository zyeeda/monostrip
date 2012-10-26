package com.zyeeda.framework.commons.organization.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.zyeeda.framework.commons.annotation.scaffold.Scaffold;
import com.zyeeda.framework.commons.base.data.TreeNode;
import com.zyeeda.framework.commons.base.entity.DomainEntity;

@Entity
@Table(name = "ZDA_DEPARTMENT")
@Scaffold(path = "/system/departments", type = "tree")
@JsonFilter("departmentFilter")
@Audited
public class Department extends DomainEntity implements TreeNode<Department> {

    private static final long serialVersionUID = -3470409560313841985L;
    
    private String name;
    private Department parent;
    private List<Department> children = new ArrayList<Department>();
    private List<Account> accounts = new ArrayList<Account>();
    
    @Basic
    @Column(name = "F_NAME", length = 30)
    @NotBlank
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Override
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "F_PARENT_ID")
    //@JsonBackReference
    public Department getParent() {
        return this.parent;
    }
    
    @Override
    public void setParent(Department parent) {
        this.parent = parent; 
    }

    @Override
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("name")
    //@JsonManagedReference
    public List<Department> getChildren() {
        return this.children;
    }
    
    @Override
    public void setChildren(List<Department> children) {
        this.children = children;
    }

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @OrderBy("username")
    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
    
}
