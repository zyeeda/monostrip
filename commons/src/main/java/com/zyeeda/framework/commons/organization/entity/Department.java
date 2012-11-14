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

import com.zyeeda.framework.commons.annotation.scaffold.Scaffold;
import com.zyeeda.framework.commons.base.data.TreeNode;
import com.zyeeda.framework.commons.base.entity.DomainEntity;
import com.zyeeda.framework.validation.constraint.Unique;
import com.zyeeda.framework.validation.group.Create;
import com.zyeeda.framework.validation.group.Update;

@Entity
@Table(name = "ZDA_DEPARTMENT")
@Scaffold("/system/departments")
@Audited
@Unique.List({
        @Unique(groups = Create.class, namedQuery = "findDuplicateDeptNameOnCreate", bindingProperties = "name"),
        @Unique(groups = Update.class, namedQuery = "findDuplicateDeptNameOnUpdate", bindingProperties = "name")
})
public class Department extends DomainEntity implements TreeNode<Department> {

    private static final long serialVersionUID = -3470409560313841985L;
    
    private String name;
    private Department parent;
    private List<Department> children = new ArrayList<Department>();
    private List<Account> accounts = new ArrayList<Account>();
	private String path;
    
    @Column(name = "F_PATH", length = 3000)
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

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
    @ManyToOne(cascade = CascadeType.PERSIST)
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
