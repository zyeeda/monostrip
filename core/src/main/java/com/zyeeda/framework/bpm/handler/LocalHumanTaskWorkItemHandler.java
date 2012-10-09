package com.zyeeda.framework.bpm.handler;

import org.drools.runtime.KnowledgeRuntime;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.process.workitem.wsht.AbstractHTWorkItemHandler;
import org.jbpm.task.Task;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.utils.OnErrorAction;

import com.zyeeda.framework.bpm.HumanTaskService;

public class LocalHumanTaskWorkItemHandler extends AbstractHTWorkItemHandler {
    
    private HumanTaskService taskService;

    public LocalHumanTaskWorkItemHandler(KnowledgeRuntime session) {
        super(session);
    }
    
    public LocalHumanTaskWorkItemHandler(KnowledgeRuntime session, OnErrorAction action) {
        super(session, action);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        this.taskService.exitTaskByWorkItemId(workItem.getId());
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        Task task = this.createTaskBasedOnWorkItemParams(workItem);
        ContentData content = this.createTaskContentBasedOnWorkItemParams(workItem);
        
        this.taskService.createTask(task, content);
    }
    
    public void setHumanTaskService(HumanTaskService taskService) {
        this.taskService = taskService;
    }

}
