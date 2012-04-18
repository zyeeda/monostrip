                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          package com.zyeeda.framework.entities;

import java.util.List;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.util.CollectionUtils;

import com.zyeeda.framework.entities.base.SimpleDomainEntity;

@Entity
@Table(name = "SYS_ROLE")
@NamedNativeQuery(name = "getRolesBySubject", query = "SELECT * "
		+ "FROM SYS_ROLE r "
		+ "LEFT JOIN SYS_SUBJECT s ON s.F_ROLE_ID = r.F_ID "
		+ "WHERE s.F_SUBJECT = :subject", resultClass = Role.class)
@NamedQuery(name = "getRoles", query = "SELECT r FROM Role r")
@XmlRootElement(name = "role")
public class Role extends SimpleDomainEntity {

	private static final long serialVersionUID = 1665902703034523260L;
	private static final char PERMISSION_SEPARATOR = ';';
	// add by dengjiu 2011-05-56
	// private String state;
	private Set<String> subjects;
	private String permissions;
	private String scopeType;
	private String scopeId;
	private String deptepment;
	private String deptepmentId;
	private String ramoPermissions;

	
	
	public String getRamoPermissions() {
		return ramoPermissions;
	}

	public void setRamoPermissions(String ramoPermissions) {
		this.ramoPermissions = ramoPermissions;
	}

	@ElementCollection
	@CollectionTable(name = "SYS_SUBJECT", joinColumns = @JoinColumn(name = "F_ROLE_ID"))
	@Column(name = "F_SUBJECT")
	public Set<String> getSubjects() {
		return this.subjects;
	}

	public void setSubjects(Set<String> subjects) {
		this.subjects = subjects;
	}

	@javax.persistence.Lob
	@javax.persistence.Column(name = "F_PERMISSIONS")
	public String getPermissions() {
		return this.permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	@Transient
	public List<String> getRoamPermissionList() {
		String permissions = this.getPermissions();
		if(StringUtils.isNotBlank(permissions)){
			int flog = permissions.indexOf("&");
			if(flog >= 0) {
				permissions = permissions.substring(flog + 1, permissions.length());
			} 
		}
		String[] permissionArray = StringUtils.split(permissions,
				PERMISSION_SEPARATOR);
		return CollectionUtils.asList(permissionArray);
	}
	
	
	@Transient
	public List<String> getPermissionsList() {
		String permissions = this.getPermissions();
		if(StringUtils.isNotBlank(permissions)){
			int flog = permissions.indexOf("&");
			if(flog >= 0) {
				permissions = permissions.substring(0, flog);
			} 
		}
		String[] permissionArray = StringUtils.split(permissions,
				PERMISSION_SEPARATOR);
		return CollectionUtils.asList(permissionArray);
	}
	
	@Transient
	public List<String> getPermissionList() {
		String permissions = this.getPermissions();
		if(StringUtils.isNotBlank(permissions)){
			int flog = permissions.indexOf("&");
			if(flog > 0) {
				permissions.replace("&", ";");
			} else if(flog == 0) {
				permissions.replace("&", "").trim();
			}
		}
		String[] permissionArray = StringUtils.split(permissions,
				PERMISSION_SEPARATOR);
		return CollectionUtils.asList(permissionArray);
	}

	@Basic
	@Column(name = "F_SCOPE_TYPE")
	public String getScopeType() {
		return this.scopeType;
	}

	public void setScopeType(String scopeType) {
		this.scopeType = scopeType;
	}

	@Basic
	@Column(name = "F_SCOPE_ID")
	public String getScopeId() {
		return this.scopeId;
	}

	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}

	@Basic
	@Column(name = "F_DEPTEPMENT")
	public String getDeptepment() {
		return deptepment;
	}

	public void setDeptepment(String deptepment) {
		this.deptepment = deptepment;
	}

	@Basic
	@Column(name = "F_DEPTEPMENT_ID")
	public String getDeptepmentId() {
		return deptepmentId;
	}

	public void setDeptepmentId(String deptepmentId) {
		this.deptepmentId = deptepmentId;
	}

	/*
	 * @Basic
	 * 
	 * @Column(name = "F_STATE") public String getState() { return state; }
	 * public void setState(String state) { this.state = state; }
	 */
}
