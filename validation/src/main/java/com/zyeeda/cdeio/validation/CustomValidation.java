package com.zyeeda.cdeio.validation;

import javax.persistence.EntityManagerFactory;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import com.zyeeda.cdeio.validation.validator.CustomConstraintValidatorFactory;

public class CustomValidation {
    
    public static ValidatorFactory buildValidatorFactory(EntityManagerFactory emf) {
        Configuration<?> config = Validation.byDefaultProvider().configure();
        config.constraintValidatorFactory(new CustomConstraintValidatorFactory(emf));
        return config.buildValidatorFactory();
    }
}
