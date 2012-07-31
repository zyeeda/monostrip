package com.zyeeda.framework.bpm;
import java.util.List;
import java.util.Map;

import org.jbpm.task.Task;

public interface TaskService {
    // 任务流转类型
    public final static String TRANSITION_TYPE = "transitionType";
    // 默认审批类型
    public final static int TRANSITION_TYPE_DEFAULT = 1;
    // 动态审批(顺序)
    public final static int TRANSITION_TYPE_SEQUENTIAL = 2;
    // 动态审批(并行)
    public final static int TRANSITION_TYPE_PARALLEL = 3;
    // 动态审批(并行子流程)
    public final static int TRANSITION_TYPE_SUBPROCESS = 4;
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
     * 根据任务实例提交任务
     * 
     * @param task
     */
    public void complete(Task task, String comment, Map<String,Object> results);

    /**
     * 根据任务实例id、参与者、动态审批类型提交任务
     * 
     * @param taskId
     * @param actors
     * @param transType
     *            {@link #TRANSITION_TYPE}
     */
    public void complete(Long taskId, List<String> actors, int transitionType);

    /**
     * 根据任务实例、参与者、动态审批类型提交任务
     * 
     * @param task
     * @param actors
     * @param transType
     */
    public void complete(Task task, List<String> actors, int transitionType);

    /**
     * 回退任务
     * 
     * @param taskId
     */
    public void reject(Long taskId);
    /**
     * 回退任务到指定的节点
     * @param taskId
     * @param target
     */
    public void reject(Long taskId, String target);

    /**
     * 回退任务
     * 
     * @param taskId
     */
    public void reject(Task task);
    /**
     * 回退任务到指定的节点
     * @param task
     * @param target
     */
    public void reject(Task task, String target);    

    /**
     * 召回任务
     * 
     * @param taskId
     * @param user
     */
    public void recall(Long taskId);
    /**
     * 召回任务
     * 
     * @param taskId
     */
    public void recall(Task task);

}
