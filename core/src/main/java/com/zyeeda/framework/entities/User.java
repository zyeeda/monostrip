package com.zyeeda.framework.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity(name = "ZDA_SYS_USER")
public class User/* extends SimpleDomainEntity*/ implements Serializable {

	private static final long serialVersionUID = -411862891641683217L;

	private String id;
	private String username;
	private String password;
	private String assignPassword;
	private String gender;
	private String position;
	private String degree;
	private String email;
	private String mobile;
	private Date birthday;
	private Date dateOfWork;
	private Boolean status;
	private Boolean postStatus;
//	private byte[] photo;
	private String departmentName;
	private String deptFullPath;
	private String departmentNo;
	private String selectedDeptFullPath;

	@Id @Column(name = "F_ID")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "F_USERNAME", nullable = false)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "F_PASSWORD", nullable = false, length = 46)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	@Column(name = "F_ASSIGNPASSWORD", length = 46)
	public String getAssignPassword() {
		return assignPassword;
	}
	
	public void setAssignPassword(String assignPassword) {
		this.assignPassword = assignPassword;
	}
	
	@Column(name = "F_GENDER", length = 4)
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@Column(name = "F_POSITION", length = 100)
	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	@Column(name = "F_DEGREE", length = 100)
	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	@Column(name = "F_EMAIL", length = 50)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "F_MOBILE", length = 15)
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	
	@javax.persistence.Temporal(TemporalType.TIMESTAMP)
	@javax.persistence.Column(name = "F_BIRTHDAY")
	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	@javax.persistence.Temporal(TemporalType.TIMESTAMP)
	@javax.persistence.Column(name = "F_DATEOFWORK")
	public Date getDateOfWork() {
		return dateOfWork;
	}

	public void setDateOfWork(Date dateOfWork) {
		this.dateOfWork = dateOfWork;
	}

	@Column(name = "STATUS")
	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	@Column(name = "POSTSTATUS")
	public Boolean getPostStatus() {
		return postStatus;
	}

	public void setPostStatus(Boolean postStatus) {
		this.postStatus = postStatus;
	}

//	public byte[] getPhoto() {
//		return photo;
//	}
//
//	public void setPhoto(byte[] photo) {
//		this.photo = photo;
//	}

	@Column(name = "F_DEPARTMENTNAME", length = 100)
	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	@Column(name = "F_DEPTFULLPATH", length = 100)
	public String getDeptFullPath() {
		return deptFullPath;
	}

	public void setDeptFullPath(String deptFullPath) {
		this.deptFullPath = deptFullPath;
	}

	@Column(name = "F_DEPARTMENT_NO", length = 100) 
	public String getDepartmentNo() {
		return this.departmentNo;
	}
	
	public void setDepartmentNo(String departmentNo) {
		this.departmentNo = departmentNo;
	}

	@Transient
	public String getSelectedDeptFullPath() {
		return selectedDeptFullPath;
	}

	public void setSelectedDeptFullPath(String selectedDeptFullPath) {
		this.selectedDeptFullPath = selectedDeptFullPath;
	}
 }