package com.zyeeda.framework.entities;

import javax.persistence.Basic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.zyeeda.framework.entities.base.RevisionDomainEntity;

@Entity
@Table(name = "ZDA_ACTION_LOGS")
@XmlRootElement(name = "actionLogs")
public class ActionLog extends RevisionDomainEntity {

	private static final long serialVersionUID = -7970644520971484077L;
	/**
	 * 执行人
	 */
	private String actor;
	/**
	 * 执行动作
	 */
	private String action;

	/**
	 * 执行人
	 */
	@Basic
	@Column(name = "F_ACTOR", length = 30)
	public String getActor() {
		return actor;
	}

	/**
	 * 执行人
	 */
	public void setActor(String actor) {
		this.actor = actor;
	}

	/**
	 * 执行动作
	 */
	@Basic
	@Column(name = "F_ACTION", length = 50)
	public String getAction() {
		return action;
	}

	/**
	 * 执行动作
	 */
	public void setAction(String action) {
		this.action = action;
	}
}
