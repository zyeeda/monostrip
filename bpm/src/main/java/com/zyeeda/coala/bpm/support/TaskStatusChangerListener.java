package com.zyeeda.coala.bpm.support;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.transaction.annotation.Transactional;

import com.zyeeda.coala.commons.annotation.scaffold.ProcessStatusAware;

/**
 * @author guyong
 *
 */
public class TaskStatusChangerListener implements TaskListener {

	private static final long serialVersionUID = -3062981399278579548L;
	
	private EntityManager entityManager = null;

	@PersistenceContext
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	@Transactional
	public void notify(DelegateTask delegateTask) {
		String id = (String)delegateTask.getVariable("ENTITY");
		String className = (String)delegateTask.getVariable("ENTITYCLASS");
		try {
			Class<?> clazz = TaskStatusChangerListener.class.getClassLoader().loadClass(className);
			Object o = entityManager.find(clazz, id);
			if (o != null && o instanceof ProcessStatusAware) {
				ProcessStatusAware obj = (ProcessStatusAware) o;
				obj.setStatus(delegateTask.getName());
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
