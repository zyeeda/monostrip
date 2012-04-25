package com.zyeeda.framework.flow;

import java.util.ArrayList;
import java.util.List;

import org.drools.runtime.process.WorkItem;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.User;

import com.zyeeda.framework.persistence.PersistenceService;

/**
 * @author guyong
 *
 */
public abstract class AbstractPotentialOwnerDeterminer implements PotentialOwnerDeterminer {

	private PersistenceService persistenceService = null;
	
	public AbstractPotentialOwnerDeterminer() {
		
	}
	
	public AbstractPotentialOwnerDeterminer(PersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}

	protected PersistenceService getPersistenceService() {
		return persistenceService;
	}
	
	public void setPersistenceService(PersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}
	
	@Override
	public List<OrganizationalEntity> determine(String rules, WorkItem workItem) {
		List<OrganizationalEntity> result = new ArrayList<OrganizationalEntity>();
		if( USER_ASSIGNED.equals(rules) ) {
			rules = (String)workItem.getParameter(USER_ASSIGNED);
		}
		
		String[] rs = rules.split(RULE_SEPERATOR);
		for( String rule : rs ) {
			result.addAll(processRule(rule,workItem));
		}
		return result;
	}
	
	protected List<OrganizationalEntity> processRule(String rule, WorkItem workItem) {
		List<OrganizationalEntity> result = new ArrayList<OrganizationalEntity>();
		String[] tokens = rule.split(TOKEN_SEPERATOR);
		
		if( tokens.length == 1 ) {
			processSingleToken(tokens[0], workItem, result);
			return result;
		}

		//TODO to support more conditions
		return null;
	}

	protected void processSingleToken(String token, WorkItem workItem, List<OrganizationalEntity> result) {
		if( PROCESS_SUBMITTER.equals(token) ) {
			result.add(new User(getProcessSubmitter(workItem)));
		}
		if( PREVIOUS_TASK_PROCESSOR.equals(token) ) {
		    result.add(new User(getPreviousTaskProcessor(workItem)));
		}
		if( ASSIGNED_USER_ID.equals(token) ) {
		    result.add(new User(getAssignedUserId(workItem)));
		}
		if( token.startsWith(TASK_PROCESSOR) ) {
		    result.add(new User(getProcessorOfTask(token, workItem)));
		}
		
		if( token.startsWith(ACCORDING_ROLE + IDENTIFIER) ) {
		    String roleName = token.substring((ACCORDING_ROLE + IDENTIFIER).length());
		    List<OrganizationalEntity> potentials = determinePotentials(null, roleName, null, workItem);
		    if( potentials != null ) {
		        result.addAll(potentials);
		    }
		}
	}
	
	protected List<OrganizationalEntity> processThreeTokens(String[] tokens, WorkItem workItem) {
	    // TODO not implement yet.
		return null;
	}
	
	@Override
	public User determineTaskCreator(WorkItem workItem) {
		String id = getPreviousTaskProcessor(workItem);
		if( id == null ) {
			id = getProcessSubmitter(workItem);
		}
		return id == null ? null : new User(id);
	}

	protected String getProcessSubmitter(WorkItem workItem) {
		return (String)workItem.getParameter(PROCESS_SUBMITTER);
	}
	
	protected String getPreviousTaskProcessor(WorkItem workItem) {
		return (String)workItem.getParameter(PREVIOUS_TASK_PROCESSOR);
	}
	
	protected String getAssignedUserId(WorkItem workItem) {
		return (String)workItem.getParameter(ASSIGNED_USER_ID);
	}
	
	protected String getProcessorOfTask(String taskKey, WorkItem workItem) {
		return (String)workItem.getParameter(taskKey);
	}
	
	/**
	 * arguments line and point will be null, because something is not implement yet.
	 */
	protected abstract List<OrganizationalEntity> determinePotentials(String line, String role, String point, WorkItem workItem);
}
