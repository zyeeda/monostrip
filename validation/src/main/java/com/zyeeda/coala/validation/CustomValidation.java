package com.zyeeda.coala.validation;

import javax.persistence.EntityManagerFactory;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import com.zyeeda.coala.validation.validator.CustomConstraintValidatorFactory;

public class CustomValidation {
    
    public static ValidatorFactory buildValidatorFactory(EntityManagerFactory emf) {
        Configuration<?> config = Validation.byDefaultProvider().configure();
        config.constraintValidatorFactory(new CustomConstraintValidatorFactory(emf));
        return config.buildValidatorFactory();
    }
}
