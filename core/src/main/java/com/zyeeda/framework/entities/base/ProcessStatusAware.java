package com.zyeeda.framework.entities.base;

/**
 * @author guyong
 *
 */
public interface ProcessStatusAware {

    void setStatus(String status);
    
    void setProcessId(String processId);
    void setProcessInstanceId(Long id);
    void setSubmitter(String submitter);
}
