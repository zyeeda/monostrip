package com.zyeeda.coala.commons.annotation.scaffold;

/**
 * @author guyong
 *
 */
public interface ProcessStatusAware {

    void setSubmitter(String submitter);
    void setProcessId(String processId);
    void setProcessInstanceId(String processInstanceId);
    void setStatus(String taskName);
    
    String getSubmitter();
    String getProcessId();
    String getProcessInstanceId();
    String getStatus();
}
