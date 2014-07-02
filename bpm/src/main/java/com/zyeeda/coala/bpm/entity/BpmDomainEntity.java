package com.zyeeda.coala.bpm.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.zyeeda.coala.commons.annotation.scaffold.ProcessStatusAware;
import com.zyeeda.coala.commons.base.entity.DomainEntity;

/**
 * 工作流实体的基类.
 *
 ****************************
 * @author child          *
 * @date   2014年5月30日        *
 ****************************
 */
@MappedSuperclass
public class BpmDomainEntity extends DomainEntity implements Serializable, ProcessStatusAware{

	/**
	 *  自动生成的序列化版本 UID.
	 */
	private static final long serialVersionUID = -5554035711352326839L;
	
	protected String processDefinitionId;
	protected String processInstanceId;
	protected String submitter = null;
	protected String status = null;	
	
	@Column(name="PROCESS_DEFINITION_ID")
	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	@Column(name="PROCESS_INSTANCE_ID")
	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}


	@Override
	@Column(name="SUBMITTER")
	public String getSubmitter() {
		return submitter;
	}

	@Override
	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}
	
	@Override
	@Column(name="STATUS")
	public String getStatus() {
		return status;
	}
	
	@Override
	public void setStatus(String taskName) {
		this.status = taskName;
	}

}
