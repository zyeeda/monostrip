package com.zyeeda.framework.bpm;

/**
 * 工作流常量类
 * @author child
 *
 */
public class CoalaBpmConstants {
	/**未定义*/
	public static final String COALA_UNDEFINED ="coalaUndefined";
	public static final String COALA_LANGUAGE ="en-UK";
	
	/**任务节点的节点名称*/
	public static final String COALA_TASK_NAME ="coalaTaskName";
	/**任务节点的潜在参与组id*/
	public static final String COALA_GROUP_ID ="coalaGroupId";
	/**任务节点的潜在参与用户id*/
	public static final String COALA_ACTOR_ID ="coalaActorId";	
	/**任务节点的注释*/
	public static final String COALA_COMMENT ="coalaComment";
	/**任务节点的优先级*/
	public static final String COALA_PRIORITY ="coalaPriority";	
	
	/**任务节点的发送召回、回退信号的指向*/
	public static final String COALA_SIGNAL_NAME ="coalaSignalName";
	/**任务节点的发送召回、回退信号的指向*/
	public static final String COALA_RECALL_NODE_NAME ="coalaRecallNodeName";
	
	/**多实例任务的参与者变量名称*/
	public static final String COALA_POTENTIAL_OWNERS ="coalaPotentialOwners";
	
	/**任务节点是否允许召回*/
	public static final String REVOKABLE = "Revokable";
	
	/**任务节点是否允许回退*/
	public static final String REJECTABLE = "Rejectable";
	
	public static final String REJECT_EVENT_TYPE = "RejectEventType";
}
