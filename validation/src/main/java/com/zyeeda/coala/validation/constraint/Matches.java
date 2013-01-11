package com.zyeeda.coala.validation.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.zyeeda.coala.validation.validator.MatchesConstraintValidator;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatchesConstraintValidator.class)
@Documented
public @interface Matches {
    
    String message() default "{com.zyeeda.coala.validation.constraint.Matches.message}";
    
    String bindingProperties();
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};

    String source();
    
    String target();
    
}
