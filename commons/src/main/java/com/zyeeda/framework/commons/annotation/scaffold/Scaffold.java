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
public @interface Scaffold {

    String path();
    String type() default "grid"; // grid, tree, treeTable
    
    String processId() default "";
    
    String[] excludes() default {};
    Filters[] filters() default {@Filters};
    
}
