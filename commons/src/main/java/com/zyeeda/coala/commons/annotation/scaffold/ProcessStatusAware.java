package com.zyeeda.coala.commons.annotation.scaffold;

/**
 * @author guyong
 *
 */
public interface ProcessStatusAware {

    void setSubmitter(String submitter);
    void setProcessDefinitionId(String processDefinitionId);
    void setProcessInstanceId(String processInstanceId);
    void setStatus(String taskName);
    
    String getSubmitter();
    String getProcessDefinitionId();
    String getProcessInstanceId();
    String getStatus();
}
