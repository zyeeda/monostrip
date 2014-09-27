package com.zyeeda.cdeio.bpm.support;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.pvm.PvmEvent;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 全局流程监听解析器
 *
 ****************************
 * @author child          *
 * @date   2014年6月30日        *
 ****************************
 */
public class AddGlobalExecutionListenerParseHandler extends AbstractBpmnParseHandler<Process> {
  
	private ExecutionListener listener;
  
	@Autowired
	public void setListener(ExecutionListener listener) {
		this.listener = listener;
	}  
    protected Class< ? extends BaseElement> getHandledType() {
    	return Process.class;
    }
  
    protected void executeParse(BpmnParse bpmnParse, Process element) {
    	bpmnParse.getCurrentProcessDefinition().addExecutionListener(PvmEvent.EVENTNAME_END, listener);
    }

}
