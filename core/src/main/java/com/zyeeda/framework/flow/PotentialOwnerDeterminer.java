package com.zyeeda.framework.flow;

import java.util.List;

import org.drools.runtime.process.WorkItem;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.User;

/**
 * Use to determine users who will be the potential of a task
 *
 * @author guyong
 *
 */
public interface PotentialOwnerDeterminer {

	//line:reportLineName/role:roleName/point:processor_of_taskId
	//line:reportLineName/role:roleName/point:submitter
	//line:reportLineName/role:roleName/point:previous_processor
	//submitter
	//previous_processor
	//processor_of_taskId
	//assigned_user_id
	String ACCORDING_LINE = "line";
	String ACCORDING_ROLE = "role";

	String ACCORDING_POINT = "point";
	String PROCESS_SUBMITTER = "submitter";
	String TASK_PROCESSOR = "processor_of_";
	String PREVIOUS_TASK_PROCESSOR = "previous_processor";
	
	String USER_ASSIGNED = "assigned";
	String ASSIGNED_USER_ID = "assigned_user_id";
	
	String RULE_SEPERATOR = ",";
	String TOKEN_SEPERATOR = "/";
	String IDENTIFIER = ":";
	
	/**
	 * all rules separated by ","
	 * each rule contains three partition, end each of the partition separated by "/"
	 * partitions follow the key:value pattern
	 * 
	 * there are several special rules, 
	 * submitter, assigned_user_id
	 * 
	 * from now on, only support forms like below:
	 * submitter
	 * assigned_user_id
	 * role:roleName
	 */
	List<OrganizationalEntity> determine(String rules, WorkItem workItem);
	
	User determineTaskCreator(WorkItem workItem);
	
}
