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

}
