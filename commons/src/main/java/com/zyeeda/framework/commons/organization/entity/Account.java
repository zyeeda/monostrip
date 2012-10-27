package com.zyeeda.framework.commons.organization.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import com.zyeeda.framework.commons.annotation.scaffold.Scaffold;
import com.zyeeda.framework.commons.base.entity.DomainEntity;
import com.zyeeda.framework.validation.constraint.Matches;
import com.zyeeda.framework.validation.constraint.Unique;
import com.zyeeda.framework.validation.group.Create;
import com.zyeeda.framework.validation.group.Update;

@Entity
@Table(name = "ZDA_ACCOUNT")
@Scaffold(path = "/system/accounts")
@Unique.List({
        @Unique(groups = { Create.class }, namedQuery = "findDuplicateUsernameCountOnCreate", bindingProperties = "username"),
        @Unique(groups = { Update.class }, namedQuery = "findDuplicateUsernameCountOnUpdate", bindingProperties = "username")
})
@Matches(source = "password", target = "password2", bindingProperties = "password")
@Audited
public class Account extends DomainEntity {

    private static final long serialVersionUID = 8017378952695485417L;
    
    private String username;
    private String password;
    private String password2;
    private String nickname;
    private String firstName;
    private String familyName;
    private Gender gender;
    private Date birthday;
    private String email;
    private String mobile;
    private String telephone;
    private Department department;
    private boolean disabled;
    
    @Basic
    @Column(name = "F_USERNAME", length = 30)
    @NotBlank
    @Size(min = 6)
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    
    @Basic
    @Column(name = "F_PASSWORD", length = 60)
    @NotNull
    @Size(min = 6)
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
        this.setPassword2(password);
    }

    @Transient
    @NotNull
    @Size(min = 6, max = 60)
    public String getPassword2() {
        return password2;
    }
    public void setPassword2(String password2) {
        this.password2 = password2;
    }
    
    @Basic
    @Column(name = "F_NICKNAME", length = 30)
    @NotBlank
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    @Basic
    @Column(name = "F_FIRST_NAME", length = 30)
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    @Basic
    @Column(name = "F_FAMILY_NAME", length = 30)
    public String getFamilyName() {
        return familyName;
    }
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "F_GENDER", length = 10)
    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_BIRTHDAY")
    public Date getBirthday() {
        return birthday;
    }
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
    
    @Basic
    @Column(name = "F_EMAIL", length = 100)
    @NotBlank
    @Size(min = 6)
    @Email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Basic
    @Column(name = "F_MOBILE", length = 100)
    @Size(min = 6)
    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    @Basic
    @Column(name = "F_TELEPHONE", length = 100)
    @Size(min = 6)
    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "F_DEPARTMENT_ID")
    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department department) {
        this.department = department;
    }
    
    @Basic
    @Column(name = "F_DISABLED")
    public boolean isDisabled() {
        return disabled;
    }
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
}
