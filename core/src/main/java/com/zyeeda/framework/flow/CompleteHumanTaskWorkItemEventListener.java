package com.zyeeda.framework.flow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItemManager;
import org.jbpm.task.Content;
import org.jbpm.task.Task;
import org.jbpm.task.event.TaskClaimedEvent;
import org.jbpm.task.event.TaskCompletedEvent;
import org.jbpm.task.event.TaskEventListener;
import org.jbpm.task.event.TaskFailedEvent;
import org.jbpm.task.event.TaskSkippedEvent;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;

/**
 * @author guyong
 *
 */
public class CompleteHumanTaskWorkItemEventListener implements TaskEventListener {
    
    private TaskService taskService = null;
    private WorkItemManager manager = null;
    
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void setManager(WorkItemManager manager) {
        this.manager = manager;
    }

    @Override
    public void taskClaimed(TaskClaimedEvent event) {
        // ignore
    }

    @Override
    public void taskSkipped(TaskSkippedEvent event) {
        long taskId = event.getTaskId();
        TaskServiceSession tss = taskService.createSession();
        Task task = tss.getTask(taskId);
        long workItemId = task.getTaskData().getWorkItemId();
        manager.abortWorkItem(workItemId);
    }
    
    @Override
    public void taskFailed(TaskFailedEvent event) {
        long taskId = event.getTaskId();
        TaskServiceSession tss = taskService.createSession();
        Task task = tss.getTask(taskId);
        long workItemId = task.getTaskData().getWorkItemId();
        manager.abortWorkItem(workItemId);
    }
    
    @Override
    public void taskCompleted(TaskCompletedEvent event) {
        long taskId = event.getTaskId();
        TaskServiceSession tss = taskService.createSession();
        Task task = tss.getTask(taskId);
        long workItemId = task.getTaskData().getWorkItemId();
        String userId = task.getTaskData().getActualOwner().getId();
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("ActorId", userId);
        long contentId = task.getTaskData().getOutputContentId();
        if (contentId != -1) {
            Content content = tss.getContent(contentId);
            ByteArrayInputStream bis = new ByteArrayInputStream(content.getContent());
            ObjectInputStream in;
            try {
                in = new ObjectInputStream(bis);
                Object result = in.readObject();
                in.close();
                results.put("Result", result);
                if (result instanceof Map) {
                    Map<?, ?> map = (Map<?,?>) result;
                    for (Map.Entry<?, ?> entry: map.entrySet()) {
                        if (entry.getKey() instanceof String) {
                            results.put((String) entry.getKey(), entry.getValue());
                        }
                    }
                }
                manager.completeWorkItem(task.getTaskData().getWorkItemId(), results);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            manager.completeWorkItem(workItemId, results);
        }
    }

}
