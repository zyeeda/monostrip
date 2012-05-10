package com.zyeeda.framework.web.scaffold.annotation;

/**
 * @author guyong
 *
 */
public enum ActionType {
    Default("default"),
    List("list"),
    Get("get"),
    Create("create"),
    Update("update"),
    Remove("remove"),
    BatchRemove("batchRemove");
    
    private String name = null;
    
    ActionType(String name) {
        this.name = name;
    }
    
    public String toString() {
        return name;
    }
}
