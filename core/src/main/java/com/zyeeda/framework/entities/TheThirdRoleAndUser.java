package com.zyeeda.framework.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import com.zyeeda.framework.entities.base.DomainEntity;

@Entity
@Table(name = "ZDA_USER_AND_ROLE")
@XmlRootElement(name = "theThirdRoleAndUser")
public class TheThirdRoleAndUser extends DomainEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3892309381471896557L;

	private String roleId;
	private String userId;
	private Date passTime;
	private Boolean isPass;
	private String userName;
	private String belongToDept;
	
	@Column(name="F_BELONG_TO_DEPT")
	public String getBelongToDept() {
		return belongToDept;
	}

	public void setBelongToDept(String belongToDept) {
		this.belongToDept = belongToDept;
	}
	
	@Column(name="F_USER_NAME")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


	@Column(name="F_PASS_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getPassTime() {
		return passTime;
	}
	
	public void setPassTime(Date passTime) {
		this.passTime = passTime;
	}

	@Column(name="F_ROLE_ID")
	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	
	@Column(name="F_USER_ID")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name="F_IS_PASS")
	public boolean getIsPass() {
		return isPass;
	}

	public void setIsPass(boolean isPass) {
		this.isPass = isPass;
	}
}
