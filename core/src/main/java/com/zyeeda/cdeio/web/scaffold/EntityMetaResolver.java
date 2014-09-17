package com.zyeeda.cdeio.web.scaffold;

/**
 * @author guyong
 *
 */
public interface EntityMetaResolver {

    EntityMeta[] resolveEntities(String... packages);
    
    EntityMeta[] resolveScaffoldEntities(String... packages);
    
    EntityMeta resolveEntity(Class<?> clazz);
    
}
