package com.zyeeda.framework.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.zyeeda.framework.service.Service;

@Deprecated
public interface PersistenceService extends Service {

	public EntityManagerFactory getSessionFactory();
	
	public EntityManager openSession();
	
	public void closeSession();
	
	public EntityManager getCurrentSession();
	
}
