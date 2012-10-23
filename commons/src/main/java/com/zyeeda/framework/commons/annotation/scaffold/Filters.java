package com.zyeeda.framework.commons.annotation.scaffold;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author guyong
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Filters {
    
    ActionType type() default ActionType.DEFAULT;
    Filter[] includes() default {};
    Filter[] excludes() default {};
    
}
