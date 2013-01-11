package com.zyeeda.coala.validation.validator;

import java.lang.annotation.Annotation;

import javax.persistence.EntityManagerFactory;
import javax.validation.ConstraintValidator;

public abstract class CustomConstraintValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

	private final EntityManagerFactory emf;

    public CustomConstraintValidator(EntityManagerFactory emf) {
    	this.emf = emf;
    }
    
    protected EntityManagerFactory getEntityManagerFactory() {
    	return this.emf;
    }
    
}
