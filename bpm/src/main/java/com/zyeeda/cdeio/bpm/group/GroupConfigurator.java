package com.zyeeda.cdeio.bpm.group;

import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * 组查询配置，用以实现根据用户查询关联组列表
 *
 ****************************
 * @author child          *
 * @date   2014年4月28日        *
 ****************************
 */
public class GroupConfigurator extends AbstractProcessEngineConfigurator{
	protected GroupManagerFactory groupManagerFactory;
	
	@Override
	public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		// do nothing
	}

	@Override
	public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {
	    GroupManagerFactory groupManagerFactory = getGroupManagerFactory();
	    //替换 activiti 引擎默认的 groupManagerFactory
	    processEngineConfiguration.getSessionFactories().put(groupManagerFactory.getSessionType(), groupManagerFactory);		
	}
	protected GroupManagerFactory getGroupManagerFactory() {
	    if (this.groupManagerFactory != null) {
	      return groupManagerFactory;
	    }
	    return new GroupManagerFactory();
	}
}
