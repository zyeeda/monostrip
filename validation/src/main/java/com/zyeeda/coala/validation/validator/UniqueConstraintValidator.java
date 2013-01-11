package com.zyeeda.coala.validation.validator;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import com.zyeeda.coala.validation.constraint.Unique;

public class UniqueConstraintValidator extends CustomConstraintValidator<Unique, Object> {

	private final static Logger LOGGER = LoggerFactory.getLogger(UniqueConstraintValidator.class);
	
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
        EntityManager session = EntityManagerFactoryUtils.getTransactionalEntityManager(this.getEntityManagerFactory(), null);
        Query query = session.createNamedQuery(this.constraint.namedQuery());
        Set<Parameter<?>> params = query.getParameters();
        for (Parameter<?> param : params) {
            try {
                String name = param.getName();
                String replacedName = name.replace('$', '.');
                Object value = null;
                try {
                    value = BeanUtils.getNestedProperty(obj, replacedName);
                } catch (NestedNullException e) {
                    LOGGER.trace(e.getMessage(), e);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("parameter name = {}", name);
                    LOGGER.debug("replaced parameter name = {}", replacedName);
                    LOGGER.debug("parameter value = {}", value);
                }
                
                query.setParameter(name, value);
            } catch (NoSuchMethodException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        synchronized(this) {
            long count = (Long) query.getSingleResult();
            return count == 0;
        }
	}

}
