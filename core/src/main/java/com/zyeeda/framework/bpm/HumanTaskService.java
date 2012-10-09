package com.zyeeda.framework.bpm;
import java.util.Map;

import org.jbpm.task.Task;
import org.jbpm.task.service.ContentData;

public interface HumanTaskService {
    
    void createTask(Task task, ContentData content);
    
    void exitTaskByWorkItemId(Long workItemId);
    
    /**
     * 开始任务.
     * 
     * @param taskId 任务 ID
     * @param userId 用户 ID
     */
    public void start(Long taskId, String userId);
    
    /**
     * 领取任务.
     * 
     * @param taskId 任务 ID
     * @param userId 用户 ID
     */
    public void claim(Long taskId, String userId);
    
    /**
     * 完成任务.
     * 
     * @param taskId 任务 ID
     * @param comment 备注
     * @param results 附加信息
     */
    public void complete(Long taskId, String comment, Map<String, Object> results) throws InvalidTaskStatusException;
    
    /**
     * 回退任务.
     * 
     * @param taskId 任务 ID
     */
    public void reject(Long taskId) throws TaskServiceException;
    
    /**
     * 召回任务.
     * 
     * @param taskId 任务 ID
     */
    public void revoke(Long taskId) throws TaskServiceException;

}
