package com.zyeeda.framework.web.jsgi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guyong
 * 
 */
public class EntityDescriptor implements Serializable {

    private static final long serialVersionUID = 2370531247761302262L;
    
    private String path = null;
    private Class<?> entityClass = null;
    private Map<String, Class<?>> fields = null;
    private Map<String, Boolean> fieldIsEntity = null;
    
    public EntityDescriptor() {
        fields = new HashMap<String, Class<?>>();
        fieldIsEntity = new HashMap<String, Boolean>();
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public Map<String, Class<?>> getFields() {
        return fields;
    }

    public void setFields(Map<String, Class<?>> fields) {
        this.fields = fields;
    }

    public void addField(String name, Class<?> clazz, Boolean isEntity) {
        fields.put(name, clazz);
        fieldIsEntity.put(name, isEntity);
    }
    
    public boolean isEntity(String name) {
        if( !fieldIsEntity.containsKey(name) ) return false;
        return fieldIsEntity.get(name);
    }
    
    public Class<?> getFieldClass(String name) {
        return fields.get(name);
    }
    
    public boolean containsField(String name) {
        return fields.containsKey(name);
    }
}
