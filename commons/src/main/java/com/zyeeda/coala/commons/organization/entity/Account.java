/* $Id$ */

package com.zyeeda.coala.commons.organization.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import com.zyeeda.coala.commons.annotation.scaffold.Scaffold;
import com.zyeeda.coala.commons.base.entity.DomainEntity;
import com.zyeeda.coala.validation.constraint.Matches;
import com.zyeeda.coala.validation.constraint.NullableSize;
import com.zyeeda.coala.validation.constraint.Unique;
import com.zyeeda.coala.validation.group.Create;
import com.zyeeda.coala.validation.group.Update;

/**
 * 用户账户.
 *
 * @author $Author$
 *
 */
@Entity
@Table(name = "ZDA_ACCOUNT")
@Scaffold("/system/accounts")
@Unique.List({
        @Unique(groups = { Create.class }, namedQuery = "findDuplicateUsernameCountOnCreate", bindingProperties = "username"),
        @Unique(groups = { Update.class }, namedQuery = "findDuplicateUsernameCountOnUpdate", bindingProperties = "username")
})
@Matches(source = "password", target = "password2", bindingProperties = "password")
public class Account extends DomainEntity {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = 8017378952695485417L;

    /**
     * 用户名.
     */
    private String username;

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

    @Basic
    @Column(name = "F_USERNAME", length = 30)
    @NotBlank
    @NullableSize(min = 6)
    public String getUsername() {
        return this.username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "F_PASSWORD", length = 60)
    @NotNull
    @NullableSize(min = 6)
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
    @NullableSize(min = 6)
    public String getPassword2() {
        return this.password2;
    }

    public void setPassword2(final String password2) {
        this.password2 = password2;
    }

    @Basic
    @Column(name = "F_REALNAME", length = 30)
    @NotBlank
    @NullableSize(min = 2)
    public String getRealName() {
        return this.realName;
    }

    public void setRealName(final String realName) {
        this.realName = realName;
    }

    @Basic
    @Column(name = "F_EMAIL", length = 100)
    @NotBlank
    @NullableSize(min = 6)
    @Email
    public String getEmail() {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "F_MOBILE", length = 30)
    @NullableSize(min = 6)
    public String getMobile() {
        return this.mobile;
    }

    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    @Basic
    @Column(name = "F_TELEPHONE", length = 30)
    @NullableSize(min = 6)
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

}

