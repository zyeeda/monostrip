package com.zyeeda.coala.bpm;

public interface TaskService extends org.activiti.engine.TaskService {

    void reject(String taskId);
    
    void revoke(String historicTaskId);
    
    /*
    最好能在Task中包含entity, 或者entity的id,class
    List<Task> getTasks(String userId)
    List<Task> getCompletedTasks(String userId)
    这个用在审批一个任务的时候查看审批记录
    List<Comment> getComments(Long taskId)
    List<Comment> getComments(DomainEntity entity)
    Task getTask(Long taskId)
    KnowledgeSession getKnowledgeSession(Long taskId)
    
     */
}
