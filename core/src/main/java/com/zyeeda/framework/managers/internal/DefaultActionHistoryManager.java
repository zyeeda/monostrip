package com.zyeeda.framework.managers.internal;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.Search;
import com.zyeeda.framework.entities.ActionHistory;
import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.managers.ActionHistoryManager;
import com.zyeeda.framework.managers.UserManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.managers.base.DomainEntityManager;
import com.zyeeda.framework.persistence.PersistenceService;

public class DefaultActionHistoryManager extends DomainEntityManager<ActionHistory, String>
		implements ActionHistoryManager {

	public DefaultActionHistoryManager(PersistenceService persistenceSvc) {
		super(persistenceSvc);
	}
	
	public ActionHistory findAlive(Long processInsId) {
		Search search = new Search();
		search.addFilterEqual("processInstanceId", processInsId);
		search.addFilterEqual("alive", true);
		return this.searchUnique(search);
	}
	
	public ActionHistory findAlive(Long processInsId, String nodeInsId) {
		Search search = new Search();
		search.addFilterEqual("processInstanceId", processInsId);
		search.addFilterEqual("nodeInstanceId", nodeInsId);
		search.addFilterEqual("alive", true);
		return this.searchUnique(search);
	}
	
	public List<ActionHistory> findListByProcessId(Long processInsId){
		Search search = new Search();
		search.addFilterEqual("processInstanceId", processInsId);
		search.addFilterOr(Filter.equal("nodeType", "StateNode"), Filter.equal("nodeType", "StartNode"), Filter.equal("nodeType", "Split"));
//		search.addFilterEqual("nodeType", "Split");
		search.addSortDesc("createdTime");
		return this.search(search);
	}
	
	
	public List<ActionHistory> findListByProcessIdSpecialDefect(Long processInsId){
		Search search = new Search();
		search.addFilterOr(Filter.equal("nodeType", "StateNode"), Filter.equal("nodeType", "StartNode"));
		search.addFilterEqual("processInstanceId", processInsId);
		search.addFilterEqual("alive", false);
		search.addSortDesc("createdTime");
		return this.search(search);
	}
	
	@SuppressWarnings("unchecked")
	public List<Long> findListByProcessCreator(String name, String processName){
		String sql = "select distinct f_process_ins_id  FROM ZDA_SYS_ACTION_HISTORY where f_creator = ? and F_PROCESS_NAME = ?";
		Query query  =  this.em().createNativeQuery(sql);
		query.setParameter(1, name);
		query.setParameter(2, processName);
		List<Long> longList = new ArrayList<Long>();
		List<BigDecimal> list = (List<BigDecimal>)query.getResultList();
		for(int i = 0; i < list.size(); i ++) {
			BigDecimal b = (BigDecimal) list.get(i);
			Long longId = b.longValue();
			longList.add(longId);
		}
		return longList;
	}
	
	public List<ActionHistory> getActionHistoryByInstanceId(Long process) throws UserPersistException {
		ActionHistoryManager actionHistoryMgr = new DefaultActionHistoryManager(
				this.getPersistenceService());
		List<ActionHistory> list = actionHistoryMgr
				.findListByProcessId(process);
		UserManager userMgr = new DefaultUserManager(this.getPersistenceService());
		for(ActionHistory actionHistory : list) {
			User userName = userMgr.findById(actionHistory.getCreator());
			if(userName != null) {
				actionHistory.setCreator(userName.getUsername());
			}
		}
		return list;
	}
	
	public void delByProcessId(Long processId) {
		Search search = new Search(ActionHistory.class);
		search.addFilterEqual("processInstanceId", processId);
		List<ActionHistory> actionList = this.search(search);
		Boolean bool = false;
		for(ActionHistory action : actionList){
			bool = this.remove(action);
			if (!bool) {
				throw new RuntimeException("某些数据含有关联关系，请解除后删除！");
			}
		}
	}
	
	public Set<String> getFlowStateByInstanceId(Long instanceId) {
		ActionHistoryManager actionHistoryMgr = new DefaultActionHistoryManager(
				this.getPersistenceService());
		List<ActionHistory> list = actionHistoryMgr.findListByProcessId(instanceId);
		Set<String> listFlowState = new HashSet<String>();
		if (list.size() > 0) {
			for (ActionHistory actionHistory : list) {
				listFlowState.add(actionHistory.getName());
			}
		}
		return listFlowState;
	}
	
	

}
