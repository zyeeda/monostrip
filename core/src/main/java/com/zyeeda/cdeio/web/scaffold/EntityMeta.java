package com.zyeeda.cdeio.web.scaffold;

import java.util.HashMap;
import java.util.Map;

import com.zyeeda.cdeio.commons.annotation.scaffold.Filters;

/**
 * @author guyong
 * 
 */
public class EntityMeta {

    private String path = null;
    private String[] otherPaths = null;
    private String[] excludedActions = null;
    private Map<String, Filters> jsonFilters = null;
    
    private Class<?> entityClass = null;
    private Map<String, FieldMeta> fieldMetas = null;
    
    private String boundProcess = null;
    
    public EntityMeta() {
        fieldMetas = new HashMap<String, FieldMeta>();
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = parsePath(path);
    }

    private String parsePath(String path) {
        if( path == null ) {
            return null;
        }
        
        String newPath = path.endsWith("/") ? path.substring(0, path.length()-1) : path;
        newPath = newPath.startsWith("/") ? newPath : "/" + newPath;
        return newPath;
    }
    
    public String[] getOtherPaths() {
        return otherPaths;
    }

    public void setOtherPaths(String[] otherPaths) {
        if (otherPaths == null) {
            this.otherPaths = null;
            return;
        }
        
        String[] ps = new String[otherPaths.length];
        for (int i = 0; i < otherPaths.length; i ++) {
            ps[i] = parsePath(otherPaths[i]);
        }
        this.otherPaths = ps;
    }

    public String[] getExcludedActions() {
        return excludedActions;
    }

    public void setExcludedActions(String[] excludedActions) {
        this.excludedActions = excludedActions;
    }

    public Map<String, Filters> getJsonFilters() {
        return jsonFilters;
    }

    public void setJsonFilters(Map<String, Filters> jsonFilters) {
        this.jsonFilters = jsonFilters;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }
    
    public void addField(FieldMeta field) {
        fieldMetas.put(field.getName(), field);
    }
    
    public boolean hasField(String name) {
        return fieldMetas.containsKey(name);
    }
    
    public FieldMeta getField(String name) {
        return fieldMetas.get(name);
    }
    
    public FieldMeta[] getFields() {
        return fieldMetas.values().toArray(new FieldMeta[0]);
    }

    public String getBoundProcess() {
        return boundProcess;
    }

    public void setBoundProcess(String boundProcess) {
        this.boundProcess = boundProcess;
    }
    
    public boolean isProcessBound() {
        return boundProcess != null;
    }
}
