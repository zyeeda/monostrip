package com.zyeeda.framework.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.zyeeda.framework.entities.base.RevisionDomainEntity;

@Entity
@Table(name = "ZDA_SYS_ACTION_HISTORY")
public class ActionHistory extends RevisionDomainEntity {

	private static final long serialVersionUID = 79794909507686169L;
	
	private String processId;
	
	private String processName;
	
	private Long processInstanceId;
	
	private String nodeId;
	
	private String nodeType;
	
	private String nodeInstanceId;
	
	private Boolean alive = true;
	
	@Basic
	@Column(name = "F_PROCESS_ID")
	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	@Basic
	@Column(name = "F_PROCESS_NAME")
	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	@Basic
	@Column(name = "F_PROCESS_INS_ID")
	public Long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(Long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	@Basic
	@Column(name = "F_NODE_ID")
	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	@Basic
	@Column(name = "F_NODE_TYPE")
	public String getNodeType() {
		return nodeType;
	}
	
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	@Basic
	@Column(name = "F_NODE_INS_ID")
	public void setNodeInstanceId(String nodeInstanceId) {
		this.nodeInstanceId = nodeInstanceId;
	}

	public void setAlive(Boolean alive) {
		this.alive = alive;
	}

	@Basic
	@Column(name = "F_ALIVE")
	public Boolean isAlive() {
		return alive;
	}
	
	public String getNodeInstanceId() {
		return nodeInstanceId;
	}

	
}
