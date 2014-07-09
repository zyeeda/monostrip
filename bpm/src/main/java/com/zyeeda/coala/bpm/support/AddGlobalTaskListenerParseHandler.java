package com.zyeeda.coala.bpm.support;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author guyong
 *
 */
public class AddGlobalTaskListenerParseHandler extends AbstractBpmnParseHandler<UserTask> {
	
	private TaskListener listener = null;

	@Autowired
	public void setListener(TaskListener listener) {
		this.listener = listener;
	}

	public Class< ? extends BaseElement> getHandledType() {
	    return UserTask.class;
	}
	
	@Override
	protected void executeParse(BpmnParse bpmnParse, UserTask element) {
		ActivityImpl activity = this.findActivity(bpmnParse, element.getId());
		TaskDefinition taskDefinition = (TaskDefinition)activity.getProperty(UserTaskParseHandler.PROPERTY_TASK_DEFINITION);
		taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, listener);
	}

}
