package com.zyeeda.framework.bpm;

public interface TaskService extends org.activiti.engine.TaskService {

    void reject(String taskId);
    
    void revoke(String historicTaskId);
    
}
