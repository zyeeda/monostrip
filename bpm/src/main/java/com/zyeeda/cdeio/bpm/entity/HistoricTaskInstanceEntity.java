package com.zyeeda.cdeio.bpm.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * activiti Task 历史实体，用于与系统内的业务实体进行映射，以便查询使用
 *
 ****************************
 * @author child          *
 * @date   2014年6月16日        *
 ****************************
 */

@Entity
@Table(name = "ACT_HI_TASKINST")
public class HistoricTaskInstanceEntity {
	
	private String id;
	private String assignee;
	private String processDefinitionId;
	private String deleteReason;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(name="ID_")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Column(name="ASSIGNEE_")
	public String getAssignee() {
		return assignee;
	}
	
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	@Column(name="PROC_DEF_ID_")
	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}
	
	@Column(name="DELETE_REASON_")
	public String getDeleteReason() {
		return deleteReason;
	}

	public void setDeleteReason(String deleteReason) {
		this.deleteReason = deleteReason;
	}
	
}
