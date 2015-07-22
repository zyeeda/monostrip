package com.zyeeda.cdeio.bpm.support;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.cdeio.bpm.TaskService;

public class DefaultTaskService implements TaskService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskService.class);
    
    private static final String REJECT_SIGNAL_PREFIX = "reject-from-";
    private static final String RECALL_SIGNAL_PREFIX = "recall-to-";

    private ProcessEngine processEngine;
    private org.activiti.engine.TaskService taskService;
    private RuntimeService runtimeService;
    private HistoryService historyService;
    
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
        this.taskService = this.processEngine.getTaskService();
        this.runtimeService = this.processEngine.getRuntimeService();
        this.historyService = this.processEngine.getHistoryService();
    }
    
    @Override
    public Task newTask() {
        return this.taskService.newTask();
    }
    
    @Override
    public Task newTask(String taskId) {
        return this.taskService.newTask(taskId);
    }
    
    @Override
    public void saveTask(Task task) {
        this.taskService.saveTask(task);
    }
    
    @Override
    public void deleteTask(String taskId) {
        this.taskService.deleteTask(taskId);
    }
    
    @Override
    public void deleteTasks(Collection<String> taskIds) {
        this.taskService.deleteTasks(taskIds);
    }
    
    @Override
    public void deleteTask(String taskId, boolean cascade) {
        this.taskService.deleteTask(taskId, cascade);
    }
    
    @Override
    public void deleteTasks(Collection<String> taskIds, boolean cascade) {
        this.taskService.deleteTasks(taskIds, cascade);
    }
    
    @Override
    public void claim(String taskId, String userId) {
        this.taskService.claim(taskId, userId);
    }
    
    @Override
    public void complete(String taskId) {
        this.taskService.complete(taskId);
    }
    
    @Override
    public void delegateTask(String taskId, String userId) {
        this.taskService.delegateTask(taskId, userId);
    }
    
    @Override
    public void resolveTask(String taskId) {
        this.taskService.resolveTask(taskId);
    }
    
    @Override
    public void complete(String taskId, Map<String, Object> variables) {
        this.taskService.complete(taskId, variables);
    }
    
    @Override
    public void setAssignee(String taskId, String userId) {
        this.taskService.setAssignee(taskId, userId);
    }
    
    @Override
    public void setOwner(String taskId, String userId) {
        this.taskService.setOwner(taskId, userId);
    }
    
    @Override
    public List<IdentityLink> getIdentityLinksForTask(String taskId) {
        return this.taskService.getIdentityLinksForTask(taskId);
    }
    
    @Override
    public void addCandidateUser(String taskId, String userId) {
        this.taskService.addCandidateUser(taskId, userId);
    }
    
    @Override
    public void addCandidateGroup(String taskId, String groupId) {
        this.taskService.addCandidateGroup(taskId, groupId);
    }
    
    @Override
    public void addUserIdentityLink(String taskId, String userId, String identityLinkType) {
        this.taskService.addUserIdentityLink(taskId, userId, identityLinkType);
    }
    
    @Override
    public void addGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
        this.taskService.addGroupIdentityLink(taskId, groupId, identityLinkType);
    }
    
    @Override
    public void deleteCandidateUser(String taskId, String userId) {
        this.taskService.deleteCandidateUser(taskId, userId);
    }
    
    @Override
    public void deleteCandidateGroup(String taskId, String groupId) {
        this.taskService.deleteCandidateGroup(taskId, groupId);
    }
    
    @Override
    public void deleteUserIdentityLink(String taskId, String userId, String identityLinkType) {
        this.taskService.deleteUserIdentityLink(taskId, userId, identityLinkType);
    }
    
    @Override
    public void deleteGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
        this.taskService.deleteGroupIdentityLink(taskId, groupId, identityLinkType);
    }
    
    @Override
    public void setPriority(String taskId, int priority) {
        this.taskService.setPriority(taskId, priority);
    }
    
    @Override
    public TaskQuery createTaskQuery() {
        return this.taskService.createTaskQuery();
    }
    
    @Override
    public void setVariable(String taskId, String variableName, Object value) {
        this.taskService.setVariable(taskId, variableName, value);
    }
    
    @Override
    public void setVariables(String taskId, Map<String, ? extends Object> variables) {
        this.taskService.setVariables(taskId, variables);
    }
    
    @Override
    public void setVariableLocal(String taskId, String variableName, Object value) {
        this.taskService.setVariableLocal(taskId, variableName, value);
    }
    
    @Override
    public void setVariablesLocal(String taskId, Map<String, ? extends Object> variables) {
        this.taskService.setVariablesLocal(taskId, variables);
    }
    
    @Override
    public Object getVariable(String taskId, String variableName) {
        return this.taskService.getVariable(taskId, variableName);
    }
    
    @Override
    public Object getVariableLocal(String taskId, String variableName) {
        return this.taskService.getVariableLocal(taskId, variableName);
    }
    
    @Override
    public Map<String, Object> getVariables(String taskId) {
        return this.taskService.getVariables(taskId);
    }
    
    @Override
    public Map<String, Object> getVariablesLocal(String taskId) {
        return this.taskService.getVariablesLocal(taskId);
    }
    
    @Override
    public Map<String, Object> getVariables(String taskId, Collection<String> variableNames) {
        return this.taskService.getVariables(taskId, variableNames);
    }
    
    @Override
    public  Map<String, Object> getVariablesLocal(String taskId, Collection<String> variableNames) {
        return this.taskService.getVariablesLocal(taskId, variableNames);
    }
    
    @Override
    public Comment addComment(String taskId, String processInstanceId, String message) {
        return this.taskService.addComment(taskId, processInstanceId, message);
    }
    
    @Override
    public List<Comment> getTaskComments(String taskId) {
        return this.taskService.getTaskComments(taskId);
    }
    
    @Override
    public List<Event> getTaskEvents(String taskId) {
        return this.taskService.getTaskEvents(taskId);
    }
    
    @Override
    public List<Comment> getProcessInstanceComments(String processInstanceId) {
        return this.taskService.getProcessInstanceComments(processInstanceId);
    }
    
    @Override
    public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content) {
        return this.taskService.createAttachment(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, content);
    }
    
    @Override
    public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, String url) {
        return this.taskService.createAttachment(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, url);
    }
    
    @Override
    public void saveAttachment(Attachment attachment) {
        this.taskService.saveAttachment(attachment);
    }
    
    @Override
    public Attachment getAttachment(String attachmentId) {
        return this.taskService.getAttachment(attachmentId);
    }
    
    @Override
    public InputStream getAttachmentContent(String attachmentId) {
        return this.taskService.getAttachmentContent(attachmentId);
    }
    
    @Override
    public List<Attachment> getTaskAttachments(String taskId) {
        return this.taskService.getTaskAttachments(taskId);
    }
    
    @Override
    public List<Attachment> getProcessInstanceAttachments(String processInstanceId) {
        return this.taskService.getProcessInstanceAttachments(processInstanceId);
    }
    
    @Override
    public void deleteAttachment(String attachmentId) {
        this.taskService.deleteAttachment(attachmentId);
    }
    
    @Override
    public List<Task> getSubTasks(String parentTaskId) {
        return this.taskService.getSubTasks(parentTaskId);
    }
    
    public void reject(String taskId) {
        Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Cannot find task by taskId " + taskId);
        }
        String signal = REJECT_SIGNAL_PREFIX + task.getTaskDefinitionKey();
        LOGGER.debug("reject signal event = {}", signal);
        this.runtimeService.signalEventReceived(signal, task.getExecutionId());
    }
    
    public void recall(String historicTaskId) {
        HistoricTaskInstance task = this.historyService.createHistoricTaskInstanceQuery().taskId(historicTaskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Cannot find historic task by taskId " + historicTaskId);
        }
        String signal = RECALL_SIGNAL_PREFIX + task.getTaskDefinitionKey();
        LOGGER.debug("recall signal event = {}", signal);
        String executionId = null;
        List<Execution> executions = this.runtimeService.createExecutionQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<Execution> executionsWithoutProcessInstance = new ArrayList<Execution>();
        
        //排除流程实例
        for(Execution e:executions){
        	if(e.getParentId()!=null){
        		executionsWithoutProcessInstance.add(e);
        	}
        }
        if(executionsWithoutProcessInstance.size()>1){
        	throw new ActivitiException("流程引擎不支持多分支情况下的召回操作。");
        }
        executionId = executionsWithoutProcessInstance.get(0).getId();
        this.runtimeService.signalEventReceived(signal,  executionId);
    }

    @Override
    public void deleteTask(String taskId, String deleteReason) {
        taskService.deleteTask(taskId, deleteReason);
    }

    @Override
    public void deleteTasks(Collection<String> taskIds, String deleteReason) {
        taskService.deleteTasks(taskIds, deleteReason);
    }

    @Override
    public void unclaim(String taskId) {
        taskService.unclaim(taskId);
    }

    @Override
    public void resolveTask(String taskId, Map<String, Object> variables) {
        taskService.resolveTask(taskId, variables);
    }

    @Override
    public void setDueDate(String taskId, Date dueDate) {
        taskService.setDueDate(taskId, dueDate);
    }

    @Override
    public NativeTaskQuery createNativeTaskQuery() {
        return taskService.createNativeTaskQuery();
    }

    @Override
    public boolean hasVariable(String taskId, String variableName) {
        return taskService.hasVariable(taskId, variableName);
    }

    @Override
    public boolean hasVariableLocal(String taskId, String variableName) {
        return taskService.hasVariableLocal(taskId, variableName);
    }

    @Override
    public void removeVariable(String taskId, String variableName) {
        taskService.removeVariable(taskId, variableName);
        
    }

    @Override
    public void removeVariableLocal(String taskId, String variableName) {
        taskService.removeVariableLocal(taskId, variableName);
    }

    @Override
    public void removeVariables(String taskId, Collection<String> variableNames) {
        taskService.removeVariables(taskId, variableNames);
    }

    @Override
    public void removeVariablesLocal(String taskId,
            Collection<String> variableNames) {
        taskService.removeVariablesLocal(taskId, variableNames);
    }

    @Override
    public Comment addComment(String taskId, String processInstanceId,
            String type, String message) {
        return taskService.addComment(taskId, processInstanceId, type, message);
    }

    @Override
    public Comment getComment(String commentId) {
        return taskService.getComment(commentId);
    }

    @Override
    public void deleteComments(String taskId, String processInstanceId) {
        taskService.deleteComments(taskId, processInstanceId);
    }

    @Override
    public void deleteComment(String commentId) {
        taskService.deleteComment(commentId);
    }

    @Override
    public List<Comment> getTaskComments(String taskId, String type) {
        return taskService.getTaskComments(taskId, type);
    }

    @Override
    public List<Comment> getCommentsByType(String type) {
        return taskService.getCommentsByType(type);
    }

    @Override
    public Event getEvent(String eventId) {
        return taskService.getEvent(eventId);
    }

	@Override
	public void complete(String taskId, Map<String, Object> variables,
			boolean localScope) {
		taskService.complete(taskId, variables, localScope);
	}

	@Override
	public List<Comment> getProcessInstanceComments(String processInstanceId, String type) {
		return taskService.getProcessInstanceComments(processInstanceId, type);
	}

	@Override
	public <T> T getVariable(String taskId, String variableName, Class<T> variableClass) {
		return taskService.getVariable(taskId, variableName, variableClass);
	}

	@Override
	public <T> T getVariableLocal(String taskId, String variableName, Class<T> variableClass) {
		return taskService.getVariableLocal(taskId, variableName, variableClass);
	}
    
}
