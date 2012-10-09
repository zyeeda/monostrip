package com.zyeeda.framework.bpm;

import org.jbpm.task.Status;;

public class InvalidTaskStatusException extends Exception {

    private static final long serialVersionUID = 7667814251932150729L;
    
    private Status current;
    private Status expected;
    
    public InvalidTaskStatusException(Status current, Status expected) {
        this.current = current;
        this.expected = expected;
    }
    
    @Override
    public String getMessage() {
        return "Current task tatus is " + current + ", but " + expected + " is required.";
    }

}
