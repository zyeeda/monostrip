package com.zyeeda.framework.validation.validator;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import com.zyeeda.framework.validation.constraint.Unique;

public class UniqueConstraintValidator extends CustomConstraintValidator<Unique, Object> {

	private final static Logger logger = LoggerFactory.getLogger(UniqueConstraintValidator.class);
	
	private Unique constraint;
	
	public UniqueConstraintValidator(EntityManagerFactory emf) {
		super(emf);
	}

	@Override
	public void initialize(Unique constraintAnnotation) {
		this.constraint = constraintAnnotation;
	}

	@Override
	public boolean isValid(Object obj, ConstraintValidatorContext ctx) {
        try {
            EntityManager session = EntityManagerFactoryUtils.doGetTransactionalEntityManager(this.getEntityManagerFactory(), null);
            Query query = session.createNamedQuery(this.constraint.namedQuery());
            Set<Parameter<?>> params = query.getParameters();
            for (Parameter<?> param : params) {
                try {
                    String name = param.getName();
                    query.setParameter(name, BeanUtils.getNestedProperty(obj, name));
                } catch (NoSuchMethodException e) {
                    logger.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            long count = (Long) query.getSingleResult();
            return count == 0;
        } catch (PersistenceException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
	}

}
