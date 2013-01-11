package com.zyeeda.coala.web.scaffold;

/**
 * @author guyong
 * 
 */
public class FieldMeta {

    private String name = null;
    private Class<?> type = null;
    private boolean entity = false;
    private String path = null;
    
    private boolean manyToManyOwner = false;
    private Class<?> manyToManyTargetType = null;
    
    private boolean manyToManyTarget = false;
    private Class<?> manyToManyOwnerType = null;
    
    private boolean oneToMany = false;
    private Class<?> manyType = null;
    
    private String mappedBy = null;

    public FieldMeta(String name, Class<?> type, boolean entity, String path) {
        this.name = name;
        this.type = type;
        this.entity = entity;
        this.path = path;
    }

    public FieldMeta(String name, Class<?> type, boolean entity, String path, boolean manyToManyOwner, Class<?> manyToManyTargetType) {
        this.name = name;
        this.type = type;
        this.entity = entity;
        this.path = path;
        this.manyToManyOwner = manyToManyOwner;
        this.manyToManyTargetType = manyToManyTargetType;
    }

    public FieldMeta(String name, Class<?> type, boolean entity, String path,
            boolean manyToManyOwner, Class<?> manyToManyTargetType,
            boolean manyToManyTarget, Class<?> manyToManyOwnerType, String mappedBy) {
        this.name = name;
        this.type = type;
        this.entity = entity;
        this.path = path;
        this.manyToManyOwner = manyToManyOwner;
        this.manyToManyTargetType = manyToManyTargetType;
        this.manyToManyTarget = manyToManyTarget;
        this.manyToManyOwnerType = manyToManyOwnerType;
        this.mappedBy = mappedBy;
    }

    public FieldMeta(String name, Class<?> type, boolean entity, String path,
            boolean manyToManyOwner, Class<?> manyToManyTargetType,
            boolean manyToManyTarget, Class<?> manyToManyOwnerType,
            boolean oneToMany, Class<?> manyType, String mappedBy) {
        this.name = name;
        this.type = type;
        this.entity = entity;
        this.path = path;
        this.manyToManyOwner = manyToManyOwner;
        this.manyToManyTargetType = manyToManyTargetType;
        this.manyToManyTarget = manyToManyTarget;
        this.manyToManyOwnerType = manyToManyOwnerType;
        this.oneToMany = oneToMany;
        this.manyType = manyType;
        this.mappedBy = mappedBy;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isManyToManyOwner() {
        return manyToManyOwner;
    }

    public void setManyToManyOwner(boolean manyToManyOwner) {
        this.manyToManyOwner = manyToManyOwner;
    }

    public Class<?> getManyToManyTargetType() {
        return manyToManyTargetType;
    }

    public void setManyToManyTargetType(Class<?> manyToManyTargetType) {
        this.manyToManyTargetType = manyToManyTargetType;
    }

    public boolean isManyToManyTarget() {
        return manyToManyTarget;
    }

    public void setManyToManyTarget(boolean manyToManyTarget) {
        this.manyToManyTarget = manyToManyTarget;
    }

    public Class<?> getManyToManyOwnerType() {
        return manyToManyOwnerType;
    }

    public void setManyToManyOwnerType(Class<?> manyToManyOwnerType) {
        this.manyToManyOwnerType = manyToManyOwnerType;
    }

    public boolean isOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(boolean oneToMany) {
        this.oneToMany = oneToMany;
    }

    public Class<?> getManyType() {
        return manyType;
    }

    public void setManyType(Class<?> manyType) {
        this.manyType = manyType;
    }

    public String getMappedBy() {
        return mappedBy;
    }

    public void setMappedBy(String mappedBy) {
        this.mappedBy = mappedBy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FieldMeta: {name:").append(name).append(", type:").append(type)
           .append(", path:").append(path).append("} ");
        
        if (entity) {
            sb.append("Many to One");
        } else if (oneToMany) {
            sb.append("One to Many, mappedBy:").append(mappedBy).append(", many side type:").append(manyType);
        } else if (manyToManyOwner) {
            sb.append("Many to Many Owner, ");
        } else if (manyToManyTarget) {
            sb.append("Many to Many Target, ");
        }
        return sb.toString();
    }

}
