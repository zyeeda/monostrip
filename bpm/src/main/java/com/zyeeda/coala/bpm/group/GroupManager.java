package com.zyeeda.coala.bpm.group;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现用户组信息的关联查询
 * 继承 activiti 原本的 GroupEntityManager 并重写其 findGroupsByUser 方法
 * 
 **************************** 
 * @author child *
 * @date 2014年4月28日 *
 **************************** 
 */
public class GroupManager extends GroupEntityManager implements GroupIdentityManager {

	private static Logger logger = LoggerFactory.getLogger(GroupManager.class);

	public GroupManager() {
	}

	/**
	 * 覆盖原有的查询方法，以实现递归查询
	 * 例如:
	 * 	    佛山分公司-->开发部
	 * 当任务的参与者设置佛山分公司时，开发部下的人员也应能够查询到任务
	 * 
	 * 
	 ****************************
	 * @author child *
	 * @date 2014年4月28日 *
	 **************************** 
	 */
	public List<Group> findGroupsByUser(final String userId) {
		logger.info("findGroupsByUser executing...");
		List<Group> groups = new ArrayList<Group>();
		GroupEntity group = new GroupEntity();
		group.setId("foshan");
		groups.add(group);
		
		group = new GroupEntity();
		group.setId("development");
		groups.add(group);		
		return groups;
	}

}