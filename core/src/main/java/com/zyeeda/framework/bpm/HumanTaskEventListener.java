package com.zyeeda.framework.bpm;

public interface HumanTaskEventListener {

    void taskCreated(Long taskId);
    
    void taskExited(Long taskId);
    
    void taskClaimed(Long taskId);
    
    void taskStarted(Long taskId);
    
    void taskCompleted(Long taskId);
    
    void taskRejected(Long taskId);
    
    void taskRevoked(Long taskId);
    
}
