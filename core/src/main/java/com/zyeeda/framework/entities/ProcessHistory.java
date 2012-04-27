package com.zyeeda.framework.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.zyeeda.framework.entities.base.RevisionDomainEntity;

@Entity
@Table(name = "ZDA_SYS_PROCESS_HISTORY")
public class ProcessHistory extends RevisionDomainEntity {

	private static final long serialVersionUID = 7750232753102958884L;
	
	private String processId;
	
	private Long processInstanceId;
	
	private String currentState;
	
	private Boolean ended = false;

	@Basic
	@Column(name = "F_PROCESS_ID")
	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
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
	@Column(name = "F_CURRENT_STATE")
	public String getCurrentState() {
		return currentState;
	}

	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	@Basic
	@Column(name = "F_ENDED")
	public Boolean isEnded() {
		return ended;
	}

	public void setEnded(Boolean ended) {
		this.ended = ended;
	}

}
