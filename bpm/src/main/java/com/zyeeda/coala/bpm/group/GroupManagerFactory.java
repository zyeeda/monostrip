package com.zyeeda.coala.bpm.group;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;

/**
 * 组管理工厂，替换 activiti 自带的组工厂 GroupEntityManagerFactory
 * 
 **************************** 
 * @author child *
 * @date 2014年4月28日 *
 **************************** 
 */
public class GroupManagerFactory implements SessionFactory {

	public GroupManagerFactory() {
	}

	@Override
	public Class<?> getSessionType() {
		return GroupIdentityManager.class;
	}

	/**
	 * 返回自定义的 GroupManager
     **************************** 
     * @author child *
     * @date 2014年4月29日 *
     **************************** 
	 */
	@Override
	public Session openSession() {
		return new GroupManager();
	}

}
