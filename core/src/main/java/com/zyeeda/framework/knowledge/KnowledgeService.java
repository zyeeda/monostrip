package com.zyeeda.framework.knowledge;

import org.drools.KnowledgeBase;
import org.jbpm.task.service.TaskService;

import com.zyeeda.framework.service.Service;

public interface KnowledgeService extends Service {

	public KnowledgeBase getKnowledgeBase();
	
	public TaskService getTaskService();
	
	public <T> T execute(StatefulSessionCommand<T> command) throws Exception;
	
}
