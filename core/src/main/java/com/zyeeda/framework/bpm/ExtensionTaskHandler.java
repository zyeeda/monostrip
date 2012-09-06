package com.zyeeda.framework.bpm;

import org.drools.runtime.process.WorkItem;
import org.jbpm.task.Task;

public interface ExtensionTaskHandler {
	
	/**
	 * 扩展程序扩展创建任务的方法
	 * @param workItem
	 * @param task
	 */
	public void executeTask(WorkItem workItem, Task task);
	/**
	 * 删除任务，在召回时taskService会主动调用
	 * @param task
	 */
	public void deleteTask(Task task);
	/**
	 * 退出任务,在回退时taskService会主动调用
	 * @param task
	 */
	public void exitTask(Task task);
	
}
