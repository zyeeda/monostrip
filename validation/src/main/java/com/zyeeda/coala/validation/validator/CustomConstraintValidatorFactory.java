package com.zyeeda.coala.validation.validator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManagerFactory;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomConstraintValidatorFactory implements ConstraintValidatorFactory {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomConstraintValidatorFactory.class);
    
	private final EntityManagerFactory emf;
	
	public CustomConstraintValidatorFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
	    LOGGER.debug("constraint validator class = {}", key.getClass().getName());
	    try {
	        Constructor<T> ctor;
	        T constraintValidator;
	        if (key.getSuperclass().equals(CustomConstraintValidator.class)) {
	            ctor = key.getConstructor(EntityManagerFactory.class);
	            constraintValidator = ctor.newInstance(this.emf);
	        } else {
	            ctor = key.getConstructor();
	            constraintValidator = ctor.newInstance();
	        }
	        return constraintValidator;
	    } catch (SecurityException e) {
            throw new ValidationException(e);
        } catch (NoSuchMethodException e) {
            throw new ValidationException(e);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e);
        } catch (InstantiationException e) {
            throw new ValidationException(e);
        } catch (IllegalAccessException e) {
            throw new ValidationException(e);
        } catch (InvocationTargetException e) {
            throw new ValidationException(e);
        }
	}

}
