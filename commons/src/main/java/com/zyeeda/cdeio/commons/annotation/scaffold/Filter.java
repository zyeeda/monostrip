package com.zyeeda.cdeio.commons.annotation.scaffold;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author guyong
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Filter {
    
    String name();
    String[] fields() default {};
    
}
