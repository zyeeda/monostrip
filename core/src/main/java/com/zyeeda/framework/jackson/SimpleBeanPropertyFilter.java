package com.zyeeda.framework.jackson;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

public abstract class SimpleBeanPropertyFilter implements BeanPropertyFilter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SimpleBeanPropertyFilter.class);

    protected SimpleBeanPropertyFilter() {
    }

    /**
     * Factory method to construct filter that filters out all properties
     * <b>except</b> ones includes in set
     */
    public static SimpleBeanPropertyFilter filterOutAllExcept(
            Set<String> properties) {
        return new FilterExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter filterOutAllExcept(
            String... propertyArray) {
        HashSet<String> properties = new HashSet<String>(propertyArray.length);
        Collections.addAll(properties, propertyArray);
        return new FilterExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter serializeAllExcept(
            Set<String> properties) {
        return new SerializeExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter serializeAllExcept(
            String... propertyArray) {
        HashSet<String> properties = new HashSet<String>(propertyArray.length);
        Collections.addAll(properties, propertyArray);
        return new SerializeExceptFilter(properties);
    }
    
    // members

    protected abstract Set<String> getProperties();
    protected abstract boolean include(BeanPropertyWriter writer, Object bean);
    
    private Map<String, Integer> state = new HashMap<String, Integer>();
    
    protected boolean include(BeanPropertyWriter writer) {
        return this.include(writer, null);
    }
    
    /**
     * Method called to determine whether property will be included (if 'true'
     * returned) or filtered out (if 'false' returned)
     */
    protected boolean include(BeanPropertyWriter writer, Object bean, boolean include) {
        Set<String> properties = this.getProperties();
        String name = writer.getName();
        
        if (bean == null) {
            return include ? properties.contains(name) : !properties.contains(name);
        }
        
        LOGGER.debug(
                "According to bean '{}', will property '{}' be {} list {}?",
                bean, name, (include ? "INCLUDED IN" : "EXCLUDED FROM"), properties);
        
        for (String prop : properties) {
            if (prop.startsWith(name)) {
                if (prop.length() == name.length()) {
                    LOGGER.debug("{} property '{}' directly.", (include ? "Include" : "Exclude"), name);
                    return include;
                }
                
                Integer counter = this.state.get(name);
                int c = 0;
                if (counter != null) {
                    c = counter;
                }
                String s = name + "(" + c + ")";
                LOGGER.debug("counter = {}", s);
                
                if (prop.equals(s)) {
                    if (c == 0) {
                        LOGGER.debug("{} property '{}'(0).", (include ? "Include" : "Exclude"), name);
                        return include;
                    }
                    
                    this.state.remove(name);
                    LOGGER.debug(
                            "Exclude property '{}', since counter reaches.",
                            name);
                    return false;
                } else {
                    Object related = null;
                    try {
                        related = writer.get(bean);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    LOGGER.debug("related bean = '{}'", related);
                    if (related == null) {
                        this.state.remove(name);
                    } else {
                        this.state.put(name, ++c);
                    }
                    
                    LOGGER.debug("Include property '{}'.", name);
                    return true;
                }
            }
        }
        
        LOGGER.debug("{} property '{}', since it's not in the list.", include ? "Exclude" : "Include", name);
        return !include;
    }

    public void serializeAsField(Object bean, JsonGenerator jgen,
            SerializerProvider provider, BeanPropertyWriter writer)
            throws Exception {
        if (include(writer, bean)) {
            writer.serializeAsField(bean, jgen, provider);
        }
    }

    public void depositSchemaProperty(BeanPropertyWriter writer,
            ObjectNode propertiesNode, SerializerProvider provider)
            throws JsonMappingException {
        if (include(writer)) {
            writer.depositSchemaProperty(propertiesNode, provider);
        }
    }

    public void depositSchemaProperty(BeanPropertyWriter writer,
            JsonObjectFormatVisitor objectVisitor, SerializerProvider provider)
            throws JsonMappingException {
        if (include(writer)) {
            writer.depositSchemaProperty(objectVisitor);
        }
    }

    /**
     * Filter implementation which defaults to filtering out unknown properties
     * and only serializes ones explicitly listed.
     */
    public static class FilterExceptFilter extends SimpleBeanPropertyFilter
            implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Set of property names to serialize.
         */
        private final Set<String> _propertiesToInclude;

        public FilterExceptFilter(Set<String> properties) {
            this._propertiesToInclude = properties;
        }
        
        protected Set<String> getProperties() {
            return this._propertiesToInclude;
        }
        
        @Override
        protected boolean include(BeanPropertyWriter writer, Object bean) {
            return this.include(writer, bean, true);
        }
    }

    /**
     * Filter implementation which defaults to serializing all properties,
     * except for ones explicitly listed to be filtered out.
     */
    public static class SerializeExceptFilter extends SimpleBeanPropertyFilter {
        /**
         * Set of property names to filter out.
         */
        protected final Set<String> _propertiesToExclude;

        public SerializeExceptFilter(Set<String> properties) {
            this._propertiesToExclude = properties;
        }
        
        protected Set<String> getProperties() {
            return this._propertiesToExclude;
        }

        @Override
        protected boolean include(BeanPropertyWriter writer, Object bean) {
            return this.include(writer, bean, false);
        }
    }
}
