package com.zyeeda.framework.web.scaffold.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.zyeeda.framework.web.scaffold.EntityMeta;
import com.zyeeda.framework.web.scaffold.EntityMetaResolver;
import com.zyeeda.framework.web.scaffold.FieldMeta;
import com.zyeeda.framework.web.scaffold.annotation.Scaffold;

/**
 * @author guyong
 *
 */
public class DefaultEntityMetaResolver implements EntityMetaResolver {
    
    private Map<Class<?>, EntityMeta> classCache = null;
    private Map<String, List<EntityMeta>> packageCache = null;
    
    private PathMatchingResourcePatternResolver resolver = null;
    private SimpleMetadataReaderFactory factory = null;
    
    public DefaultEntityMetaResolver() {
        classCache = new HashMap<Class<?>, EntityMeta>(100);
        packageCache = new HashMap<String, List<EntityMeta>>();
        resolver = new PathMatchingResourcePatternResolver();
        factory = new SimpleMetadataReaderFactory();
    }
    
    @Override
    public synchronized EntityMeta[] resolveEntities(String... packages) {
        List<EntityMeta> result = new ArrayList<EntityMeta>();
        for( String pkg : packages ) {
            result.addAll(resovePackage(pkg));
        }
        return result.toArray(new EntityMeta[result.size()]);
    }

    private List<EntityMeta> resovePackage(String pkg) {
        if( packageCache.containsKey(pkg) ) {
            return packageCache.get(pkg);
        }
        
        List<EntityMeta> result = new ArrayList<EntityMeta>();
        
        String patten = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(pkg) + "/*.class";
        try {
            Resource[]  resources = resolver.getResources(patten);
            for( Resource resource : resources ) {
                MetadataReader reader = factory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                Class<?> clazz = ClassUtils.forName(className, null);
                result.add(resolveEntity(clazz));
            }
        } catch (Exception e) {
            //ignore all exceptions
        }
        
        packageCache.put(pkg, result);
        return result;
    }
    
    @Override
    public synchronized EntityMeta resolveEntity(Class<?> clazz) {
        if( classCache.containsKey(clazz) ) {
            return classCache.get(clazz);
        }
        
        final EntityMeta meta = new EntityMeta();
        meta.setEntityClass(clazz);
        
        ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
            
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                FieldMeta fieldMeta = generateFieldMeta(field);
                if (field != null) {
                    meta.addField(fieldMeta);
                }
            }
            
        }, new ReflectionUtils.FieldFilter() {
            
            @Override
            public boolean matches(Field field) {
                int modifiers = field.getModifiers();
                if( Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) ){
                    return false;
                }
                
                Class<?> type = field.getType();
                if( Map.class.isAssignableFrom(type) ) {
                    return false;
                }
                
                return true;
            }
            
        });
        
        classCache.put(clazz, meta);
        return meta;
    }

    private FieldMeta generateFieldMeta(Field field) {
        String name = field.getName();
        Class<?> type = field.getType();
        boolean isEntity = type.getAnnotation(Entity.class) != null;
        String path = null;
        if (isEntity) {
            Scaffold scaffold = type.getAnnotation(Scaffold.class);
            if (scaffold != null) {
                path = scaffold.value();
            }
        }
        
        if( Collection.class.isAssignableFrom(type)) {
            Class<?> parameterizedType = getParameterizedType(field);
            Scaffold scaffold = parameterizedType.getAnnotation(Scaffold.class);
            if (scaffold != null) {
                path = scaffold.value();
            }
            if (isManyToManyOwner(field)) {
                return new FieldMeta(name, type, isEntity, path, true, parameterizedType);
            } else if (isManyToManyTarget(field) != null) {
                return new FieldMeta(name, type, isEntity, path, false, null, true, parameterizedType, isManyToManyTarget(field));
            } else if (isOneToMany(field) != null) {
                return new FieldMeta(name, type, isEntity, path, false, null, false, null, true, parameterizedType, isOneToMany(field));
            }
        } else {
            return new FieldMeta(name, type, isEntity, path);
        }
        
        return null;
    }
    
    @Override
    public synchronized EntityMeta[] resolveScaffoldEntities(String... packages) {
        final List<EntityMeta> result = new ArrayList<EntityMeta>();
        for( String pkg : packages ) {
            List<EntityMeta> metas = resovePackage(pkg);
            for( EntityMeta meta : metas ) {
                Scaffold scaffold = meta.getEntityClass().getAnnotation(Scaffold.class);
                if( scaffold != null ) {
                    meta.setPath(scaffold.value());
                    /*
                    if ("grid".equals(scaffold.type()) && TreeStyleEntity.class.isAssignableFrom(meta.getEntityClass())) {
                        meta.setType("tree");
                    } else if (("tree".equals(scaffold.type()) || "treeTable".equals(scaffold.type())) && !TreeStyleEntity.class.isAssignableFrom(meta.getEntityClass())) {
                        meta.setType("grid");
                    } else {
                        meta.setType(scaffold.type());
                    }
                    String processId = scaffold.processId();
                    meta.setBoundProcess((processId == null || processId.length() == 0) ? null : processId);
                    meta.setExcludedActions(scaffold.excludes());
                    Map<String, Filters> filters = new HashMap<String, Filters>();
                    for( Filters filter : scaffold.filters() ) {
                        filters.put(filter.type().toString(), filter);
                    }
                    meta.setJsonFilters(filters);
                    */
                    result.add(meta);
                }
            }
        }
        return result.toArray(new EntityMeta[result.size()]);
    }

    private boolean isManyToManyOwner(Field field) {
        boolean result = field.getAnnotation(ManyToMany.class) != null && field.getAnnotation(JoinTable.class) != null;
        
        if( !result ) {
            String name = field.getName();
            String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            try {
                Method m = field.getDeclaringClass().getMethod(getter);
                result = m.getAnnotation(ManyToMany.class) != null && m.getAnnotation(JoinTable.class) != null;
            } catch (Exception e) {
                // ignore
            }
        }
        
        return result;
    }
    
    private String isManyToManyTarget(Field field) {
        ManyToMany many = field.getAnnotation(ManyToMany.class);
        if (many == null) {
            String name = field.getName();
            String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            try {
                Method m = field.getDeclaringClass().getMethod(getter);
                many = m.getAnnotation(ManyToMany.class);
            } catch (Exception e) {
                // ignore
            }
        }
        
        if (many == null || many.mappedBy() == null) {
            return null;
        }
        
        return many.mappedBy();
    }
    
    private String isOneToMany(Field field) {
        OneToMany many = field.getAnnotation(OneToMany.class);
        if (many == null) {
            String name = field.getName();
            String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            try {
                Method m = field.getDeclaringClass().getMethod(getter);
                many = m.getAnnotation(OneToMany.class);
            } catch (Exception e) {
                // ignore
            }
        }
        
        return many == null ? null : many.mappedBy();
    }
    
    private Class<?> getParameterizedType(Field field) {
        try {
            
            ParameterizedType t = (ParameterizedType)field.getGenericType();
            return (Class<?>)t.getActualTypeArguments()[0];
            
        } catch (Exception e) {
            return null;
        }
        
    }
}
