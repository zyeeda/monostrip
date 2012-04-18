package com.zyeeda.framework.persistence.internal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.zyeeda.framework.persistence.PersistenceService;
import com.zyeeda.framework.service.AbstractService;

public abstract class AbstractPersistenceServiceProvider extends AbstractService implements PersistenceService {
	
    private EntityManager entityManager = null;
    private EntityManagerFactory entityManagerFactory = null;

	@Override
	public void stop() throws Exception {
	}
	
	@Override
	public EntityManager openSession() {
	    return getCurrentSession();
	}

	@Override
    public void closeSession() {
    }
	
	@Override
	public EntityManager getCurrentSession() {
	    return entityManager;
	}

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EntityManagerFactory getSessionFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
	
}
