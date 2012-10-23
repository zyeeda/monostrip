package com.zyeeda.framework.commons.organization.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.codehaus.jackson.map.annotate.JsonFilter;

import com.zyeeda.framework.commons.annotation.scaffold.Scaffold;
import com.zyeeda.framework.commons.base.data.TreeNode;
import com.zyeeda.framework.commons.base.entity.SimpleDomainEntity;

@Entity
@Table(name = "ZDA_DEPARTMENT")
@Scaffold(path = "/system/departments")
@JsonFilter("departmentFilter")
public class Department extends SimpleDomainEntity implements TreeNode<Department> {

    private static final long serialVersionUID = -3470409560313841985L;
    
    private Department parent;
    private List<Department> children = new ArrayList<Department>();
    private List<Account> accounts = new ArrayList<Account>();
    
    @Override
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "F_PARENT_ID")
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
