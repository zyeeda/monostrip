package com.zyeeda.framework.managers;

import java.util.List;
import java.util.Set;

import com.googlecode.genericdao.dao.jpa.GenericDAO;
import com.zyeeda.framework.entities.ActionHistory;

public interface ActionHistoryManager extends GenericDAO<ActionHistory, String> {

	public ActionHistory findAlive(Long processInsId);
	
	public ActionHistory findAlive(Long processInsId, String nodeInsId);

	public List<ActionHistory> findListByProcessId(Long parseLong);
	
	public List<Long> findListByProcessCreator(String name, String processName);
	
	public List<ActionHistory> getActionHistoryByInstanceId(Long process) throws UserPersistException;
	
	public Set<String> getFlowStateByInstanceId(Long instanceId);
	
	public List<ActionHistory> findListByProcessIdSpecialDefect(Long processInsId);
	
	public void delByProcessId(Long processId);
}
