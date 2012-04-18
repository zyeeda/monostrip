package com.zyeeda.framework.managers;

import com.googlecode.genericdao.dao.jpa.GenericDAO;
import com.zyeeda.framework.entities.ProcessHistory;

public interface ProcessHistoryManager extends GenericDAO<ProcessHistory, String> {

	public ProcessHistory findByProcessInstanceId(Long processInstanceId);
	
}
