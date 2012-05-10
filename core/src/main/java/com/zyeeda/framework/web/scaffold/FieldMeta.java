package com.zyeeda.framework.web.scaffold;

/**
 * @author guyong
 * 
 */
public class FieldMeta {

    private String name = null;
    private Class<?> type = null;
    private boolean entity = false;

    public FieldMeta(String name, Class<?> type, boolean entity) {
        this.name = name;
        this.type = type;
        this.entity = entity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public boolean isEntity() {
        return entity;
    }

    public void setEntity(boolean entity) {
        this.entity = entity;
    }

}
