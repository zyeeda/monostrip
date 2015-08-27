/* $Id$ */

package com.zyeeda.cdeio.commons.organization.entity;

import java.util.Set;
import java.util.HashSet;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import com.zyeeda.cdeio.commons.annotation.scaffold.Scaffold;
import com.zyeeda.cdeio.commons.authz.entity.Role;
import com.zyeeda.cdeio.commons.base.entity.DomainEntity;
import com.zyeeda.cdeio.validation.constraint.Matches;
import com.zyeeda.cdeio.validation.constraint.NullableSize;
import com.zyeeda.cdeio.validation.constraint.Unique;
import com.zyeeda.cdeio.validation.group.Create;
import com.zyeeda.cdeio.validation.group.Update;

/**
 * 账户.
 *
 * @author $Author$
 *
 */
@Entity
@Table(name = "CDE_ACCOUNT")
@Scaffold("/system/accounts")
@Unique.List({
        @Unique(groups = { Create.class }, namedQuery = "findDuplicateUsernameCountOnCreate", bindingProperties = "userName"),
        @Unique(groups = { Update.class }, namedQuery = "findDuplicateUsernameCountOnUpdate", bindingProperties = "userName")
})
@Matches(source = "password", target = "password2", bindingProperties = "password")
public class Account extends DomainEntity {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = 8017378952695485417L;

    /**
     * 账户名.
     */
    private String accountName;

    /**
     * 密码.
     */
    private String password;

    /**
     * 密码（用于验证）.
     */
    private String password2;

    /**
     * 真实姓名.
     */
    private String realName;

    /**
     * Email.
     */
    private String email;

    /**
     * 移动电话.
     */
    private String mobile;

    /**
     * 电话.
     */
    private String telephone;

    /**
     * 所属部门.
     */
    private Department department;

    /**
     * 禁用标记.
     */
    private Boolean disabled;

    /**
     * 删除标记.
     */
    private Boolean deleted;

    /**
     * 帐号拥有的角色.
     */
    private Set<Role> roles = new HashSet<Role>();

    @Basic
    @Column(name = "F_ACCOUNT_NAME", length = 30)
    @NotBlank
    @NullableSize(min = 2, max = 30)
    public String getAccountName() {
        return this.accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Basic
    @Column(name = "F_PASSWORD", length = 60)
    @NotNull
    @NullableSize(min = 6, max = 60)
    public String getPassword() {
        return this.password;
    }

    /**
     * 设置密码.
     * 同时设置校验用密码.
     *
     * @param password 密码
     */
    public void setPassword(final String password) {
        this.password = password;
        this.setPassword2(password);
    }

    @Transient
    @NotNull
    @NullableSize(min = 6, max = 60)
    public String getPassword2() {
        return this.password2;
    }

    public void setPassword2(final String password2) {
        this.password2 = password2;
    }

    @Basic
    @Column(name = "F_REALNAME", length = 30)
    @NotBlank
    @NullableSize(min = 2, max = 30)
    public String getRealName() {
        return this.realName;
    }

    public void setRealName(final String realName) {
        this.realName = realName;
    }

    @Basic
    @Column(name = "F_EMAIL", length = 100)
    @NotBlank
    @NullableSize(min = 6, max = 100)
    @Email
    public String getEmail() {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "F_MOBILE", length = 30)
    @NullableSize(min = 6, max = 30)
    public String getMobile() {
        return this.mobile;
    }

    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    @Basic
    @Column(name = "F_TELEPHONE", length = 30)
    @NullableSize(min = 6, max = 30)
    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(final String telephone) {
        this.telephone = telephone;
    }

    @ManyToOne
    @JoinColumn(name = "F_DEPARTMENT_ID")
    public Department getDepartment() {
        return this.department;
    }

    public void setDepartment(final Department department) {
        this.department = department;
    }

    @Basic
    @Column(name = "F_DISABLED")
    public Boolean getDisabled() {
        return this.disabled;
    }

    public void setDisabled(final Boolean disabled) {
        this.disabled = disabled;
    }

    @Basic
    @Column(name = "F_DELETED")
    public Boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    @ManyToMany
    @JoinTable(
            name = "CDE_ROLE_ACCOUNT",
            joinColumns = @JoinColumn(name = "F_ACCOUNT_ID"),
            inverseJoinColumns = @JoinColumn(name = "F_ROLE_ID"))
    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }
}
