package com.zyeeda.coala.commons.annotation.scaffold;

/**
 * @author guyong
 *
 */
public enum ActionType {
    
    DEFAULT("default"),
    LIST("list"),
    GET("get"),
    CREATE("create"),
    UPDATE("update"),
    REMOVE("remove"),
    BATCH_REMOVE("batchRemove");
    
    private String name;
    
    ActionType(String name) {
        this.name = name;
    }
    
    public String toString() {
        return name;
    }
}
