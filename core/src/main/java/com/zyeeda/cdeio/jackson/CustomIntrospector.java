package com.zyeeda.cdeio.jackson;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class CustomIntrospector extends JacksonAnnotationIntrospector {
    
    private static final long serialVersionUID = -3422638073664129517L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomIntrospector.class);

    @Override
    public Object findFilterId(AnnotatedClass ac) {
        Object id = super.findFilterId(ac);
        LOGGER.debug("annotated filter id = {}", id);
        if (id == null) {
            String name = ac.getAnnotated().getSimpleName();
            id = WordUtils.uncapitalize(name) + "Filter";
            
            LOGGER.debug("generated filter id = {}", id);
        }
        return id;
    }
}
