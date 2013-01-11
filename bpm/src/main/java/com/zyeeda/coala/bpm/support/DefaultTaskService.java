package com.zyeeda.coala.bpm.support;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.coala.bpm.TaskService;

public class DefaultTaskService implements TaskService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskService.class);
    
    private static final String REJECT_SIGNAL_PREFIX = "reject-";
    private static final String REVOKE_SIGNAL_PREFIX = "revoke-";

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
    public void addComment(String taskId, String processInstanceId, String message) {
        this.taskService.addComment(taskId, processInstanceId, message);
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
    
    public void revoke(String historicTaskId) {
        HistoricTaskInstance task = this.historyService.createHistoricTaskInstanceQuery().taskId(historicTaskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Cannot find historic task by taskId " + historicTaskId);
        }
        String signal = REVOKE_SIGNAL_PREFIX + task.getTaskDefinitionKey();
        LOGGER.debug("revoke signal event = {}", signal);
        this.runtimeService.signalEventReceived(signal,  task.getExecutionId());
    }

}
