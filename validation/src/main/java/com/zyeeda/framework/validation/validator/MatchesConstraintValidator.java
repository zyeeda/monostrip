package com.zyeeda.framework.validation.validator;

import java.lang.reflect.InvocationTargetException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import com.zyeeda.framework.validation.constraint.Matches;

public class MatchesConstraintValidator implements ConstraintValidator<Matches, Object> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchesConstraintValidator.class);
    
    private Matches constraint;

    @Override
    public void initialize(Matches constraintAnnotation) {
        this.constraint = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            String source = BeanUtils.getProperty(value, this.constraint.source());
            String target = BeanUtils.getProperty(value, this.constraint.target());
            return ObjectUtils.nullSafeEquals(source, target);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
        return false;
    }

}
