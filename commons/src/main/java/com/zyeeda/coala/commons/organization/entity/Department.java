package com.zyeeda.coala.commons.organization.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import com.zyeeda.coala.commons.annotation.scaffold.Scaffold;
import com.zyeeda.coala.commons.base.data.TreeNode;
import com.zyeeda.coala.commons.base.entity.DomainEntity;
import com.zyeeda.coala.validation.constraint.Unique;
import com.zyeeda.coala.validation.group.Create;
import com.zyeeda.coala.validation.group.Update;

/**
 * 部门.
 *
 * @author $Author$
 *
 */
@Entity
@Table(name = "ZDA_DEPARTMENT")
@Scaffold("/system/departments")
//@Audited
@Unique.List({
        @Unique(groups = Create.class, namedQuery = "findDuplicateDeptNameOnCreate", bindingProperties = "name"),
        @Unique(groups = Update.class, namedQuery = "findDuplicateDeptNameOnUpdate", bindingProperties = "name")
})
public class Department extends DomainEntity implements TreeNode<Department> {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = -3470409560313841985L;

    /**
     * 部门名称.
     */
    private String name;

    /**
     * 上级部门.
     */
    private Department parent;

    /**
     * 下级部门.
     */
    private List<Department> children = new ArrayList<Department>();

    /**
     * 部门下账户.
     */
    private List<Account> accounts = new ArrayList<Account>();

    /**
     * 部门级别路径.
     */
    private String path;

    /**
     * 删除标记.
     */
    private Boolean deleted;

    @Basic
    @Column(name = "F_NAME", length = 30)
    @NotBlank
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @ManyToOne
    @JoinColumn(name = "F_PARENT_ID")
    @Override
    public Department getParent() {
        return this.parent;
    }

    @Override
    public void setParent(final Department parent) {
        this.parent = parent;
    }

    @OneToMany(mappedBy = "parent")
    @OrderBy("name")
    @Override
    public List<Department> getChildren() {
        return this.children;
    }

    @Override
    public void setChildren(final List<Department> children) {
        this.children = children;
    }

    @OneToMany(mappedBy = "department")
    @OrderBy("accountName")
    public List<Account> getAccounts() {
        return this.accounts;
    }

    public void setAccounts(final List<Account> accounts) {
        this.accounts = accounts;
    }

    @Column(name = "F_PATH", length = 3000)
    public String getPath() {
        return this.path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Basic
    @Column(name = "F_DELETED")
    public Boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

}
