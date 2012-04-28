package com.zyeeda.framework.web.jsgi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * @author guyong
 *
 */
public class DynamicModuleHelper {

    private static EntityDescriptor[] descriptorsCache = null;

    public static <T> T newInstance(Class<T> clazz) throws Exception {
        Constructor<T> c = clazz.getConstructor();
        return c.newInstance();
    }

    @SuppressWarnings("unchecked")
    public static <T> T constructByString(Class<T> clazz, String argument) throws SecurityException, NoSuchMethodException,
                        IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

        if( String.class == clazz ) return (T)argument;

        Constructor<T> constructor = clazz.getConstructor(String.class);
        return constructor.newInstance(argument);
    }

    public static EntityDescriptor[] getConfigedEntities(String... packages) {
        if( descriptorsCache != null ) return descriptorsCache;

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        SimpleMetadataReaderFactory factory = new SimpleMetadataReaderFactory();
        Map<String, Class<?>> entities = new HashMap<String, Class<?>>();
        for( String pkg : packages ) {
            entities.putAll(getConfigedEntities(resolver, factory, pkg));
        }

        List<EntityDescriptor> descriptors = new ArrayList<EntityDescriptor>();
        for( Map.Entry<String, Class<?>> entry : entities.entrySet() ) {
            descriptors.add(resolveEntity(entry.getKey(), entry.getValue()));
        }
        descriptorsCache = descriptors.toArray(new EntityDescriptor[0]);
        return descriptorsCache;
    }

    public static EntityDescriptor resolveEntity(String path, Class<?> clazz) {
        EntityDescriptor descriptor = new EntityDescriptor();
        String newPath = path.endsWith("/") ? path.substring(0, path.length()-1) : path;
        newPath = newPath.startsWith("/") ? newPath : "/" + newPath;
        descriptor.setPath(newPath);
        descriptor.setEntityClass(clazz);

        Class<?> classToUse = clazz;
        while(  classToUse != null && classToUse != Object.class) {
            Field[] fields = classToUse.getDeclaredFields();
            for( Field field : fields ) {
                int modifiers = field.getModifiers();
                if( Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) continue;

                String fieldName = field.getName();
                Class<?> fieldClass = field.getType();

                if( Collection.class.isAssignableFrom(fieldClass) ) continue;
                if( Map.class.isAssignableFrom(fieldClass) ) continue;

                boolean isEntity = isEntityClass(fieldClass);
                descriptor.addField(fieldName, fieldClass, isEntity);
            }
            classToUse = classToUse.getSuperclass();
        }
        return descriptor;
    }

    private static boolean isEntityClass(Class<?> clazz) {
        Entity entity = clazz.getAnnotation(Entity.class);
        return entity != null;
    }

    private static Map<String, Class<?>> getConfigedEntities(ResourcePatternResolver resolver, MetadataReaderFactory factory, String pkg) {
        Map<String, Class<?>> result = new HashMap<String, Class<?>>();
        String patten = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(pkg) + "/*.class";
        try {
            Resource[] resources = resolver.getResources(patten);
            for( Resource resource : resources ) {
                MetadataReader reader = factory.getMetadataReader(resource);
                AnnotationMetadata anno = reader.getAnnotationMetadata();
                Map<String, Object> attributes = anno.getAnnotationAttributes(Path.class.getName());
                if( attributes == null ) {
                    continue;
                }
                String className = anno.getClassName();
                result.put(attributes.get("value").toString(), ClassUtils.forName(className,null));
            }
        } catch (Exception e) {
            //ignore all exceptions
            e.printStackTrace();
        }
        return result;
    }

}
