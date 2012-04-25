package com.zyeeda.framework.managers.internal;

import com.googlecode.genericdao.search.Search;
import com.zyeeda.framework.entities.ProcessHistory;
import com.zyeeda.framework.managers.ProcessHistoryManager;
import com.zyeeda.framework.managers.base.DomainEntityManager;
import com.zyeeda.framework.persistence.PersistenceService;

public class DefaultProcessHistoryManager extends DomainEntityManager<ProcessHistory, String>
		implements ProcessHistoryManager {

	public DefaultProcessHistoryManager(PersistenceService persistenceSvc) {
		super(persistenceSvc);
	}
	
	public ProcessHistory findByProcessInstanceId(Long processInstanceId) {
		Search search = new Search();
		search.addFilterEqual("processInstanceId", processInstanceId);
		return this.searchUnique(search);
	}

}
