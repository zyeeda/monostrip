package com.zyeeda.coala.bpm.support;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.zyeeda.coala.commons.annotation.scaffold.ProcessStatusAware;

/**
 * 全局流程结束监听器，会将与流程关联实体的状态改为 ‘流程已结束’
 *
 ****************************
 * @author child          *
 * @date   2014年6月30日        *
 ****************************
 */
public class ProcessStatusChangerListener implements ExecutionListener {

	private static final long serialVersionUID = -3062981399278579548L;
	
	private EntityManager entityManager = null;

	@PersistenceContext
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	@Transactional
	public void notify(DelegateExecution execution) {
		String id = (String)execution.getVariable("ENTITY");
		String className = (String)execution.getVariable("ENTITYCLASS");
		try {
			Class<?> clazz = ProcessStatusChangerListener.class.getClassLoader().loadClass(className);
			Object o = entityManager.find(clazz, id);
			if (o != null && o instanceof ProcessStatusAware && execution.getParentId() == null ) {
				ProcessStatusAware obj = (ProcessStatusAware) o;
				obj.setStatus("流程已结束");
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
