package com.zyeeda.framework.bpm;
import java.util.Map;

public interface TaskService {
    /**
     * 领取任务
     */
    public void claim(Long taskId, String userId);
    /**
     * 根据任务实例id提交任务
     * 
     * @param taskId
     */
    public void complete(Long taskId, String comment, Map<String,Object> results);
    /**
     * 回退任务
     * 
     * @param taskId
     */
    public void reject(Long taskId);
    /**
     * 召回任务
     * 
     * @param taskId
     * @param user
     */
    public void recall(Long taskId);

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
