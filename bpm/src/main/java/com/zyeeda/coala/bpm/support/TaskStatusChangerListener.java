package com.zyeeda.coala.bpm.support;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.zyeeda.coala.commons.annotation.scaffold.ProcessStatusAware;

/**
 * @author guyong
 *
 */
public class TaskStatusChangerListener implements TaskListener {

	private static final long serialVersionUID = -3062981399278579548L;
	
	private EntityManager entityManager = null;
	private RuntimeService runtimeService = null;

	@PersistenceContext
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	@Autowired
	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
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
	
	private String caculateStatus(TaskEntity task){
		String status = "";
//		runtimeService.createExecutionQuery().executionId(task.gete)
		ExecutionEntity execution = task.getExecution();
		//	并行分支 和 并行多任务实例的情况
		if(execution.isConcurrent()){
			
		}
		if(task.getProcessInstanceId().equals(task.getExecutionId())){
//			task.getp
		}
//		task.get
//		runtimeService.create
		return null;
	}

}
