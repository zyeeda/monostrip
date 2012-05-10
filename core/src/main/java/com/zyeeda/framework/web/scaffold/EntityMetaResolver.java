package com.zyeeda.framework.web.scaffold;

/**
 * @author guyong
 *
 */
public interface EntityMetaResolver {

    EntityMeta[] resolveEntities(String... packages);
    
    EntityMeta[] resolveScaffoldEntities(String... packages);
    
    EntityMeta resolveEntity(Class<?> clazz);
    
}
