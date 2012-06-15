package com.zyeeda.framework.flow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.drools.runtime.KnowledgeRuntime;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.task.AccessType;
import org.jbpm.task.I18NText;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.SubTasksStrategy;
import org.jbpm.task.SubTasksStrategyFactory;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;

/**
 * 
 * @author guyong
 * 
 */
public class ServerSideHumanTaskWorkItemHandler implements WorkItemHandler {

    public static final String ADMINISTRATOR_ID = "Administrator";
    
    private TaskService taskService = null;
    private KnowledgeRuntime session = null;
    private PotentialOwnerDeterminer determiner = null;
    
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void setSession(KnowledgeRuntime session) {
        this.session = session;
    }

    public void setDeterminer(PotentialOwnerDeterminer determiner) {
        this.determiner = determiner;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        Task task = createTask(workItem);
        ContentData content = createContentData(workItem);
        
        // TaskServiceSession tss = taskService.createSession();
        org.jbpm.task.TaskService service = new LocalTaskService(taskService);
        service.addTask(task, content);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        // TaskServiceSession tss = taskService.createSession();
        org.jbpm.task.TaskService service = new LocalTaskService(taskService);
        
        Task task = service.getTaskByWorkItemId(workItem.getId());
        service.skip(task.getId(), ADMINISTRATOR_ID);
    }

    protected Task createTask(WorkItem workItem) {
        Task task = new Task();
        
        // set task name
        String taskName = (String) workItem.getParameter("TaskName");
        if (taskName != null) {
            List<I18NText> names = new ArrayList<I18NText>();
            names.add(new I18NText("en-UK", taskName));
            task.setNames(names);
        }
        
        // set task comment to description and subject
        String comment = (String) workItem.getParameter("Comment");
        if (comment != null) {
            List<I18NText> descriptions = new ArrayList<I18NText>();
            descriptions.add(new I18NText("en-UK", comment));
            task.setDescriptions(descriptions);
            List<I18NText> subjects = new ArrayList<I18NText>();
            subjects.add(new I18NText("en-UK", comment));
            task.setSubjects(subjects);
        }
        
        // set priority
        String priorityString = (String) workItem.getParameter("Priority");
        int priority = 0;
        if (priorityString != null) {
            try {
                priority = Integer.parseInt(priorityString);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        task.setPriority(priority);
        
        TaskData taskData = new TaskData();
        
        //set work item id
        taskData.setWorkItemId(workItem.getId());
        
        //set process instance id
        taskData.setProcessInstanceId(workItem.getProcessInstanceId());
        
        //set process id
        if (session != null && session.getProcessInstance(workItem.getProcessInstanceId()) != null) {
            taskData.setProcessId(session.getProcessInstance(workItem.getProcessInstanceId()).getProcess().getId());
        }
        
        // session session id
        if (session != null && (session instanceof StatefulKnowledgeSession)) {
            taskData.setProcessSessionId(((StatefulKnowledgeSession) session).getId());
        }
        
        taskData.setSkipable(!"false".equals(workItem.getParameter("Skippable")));
        
        // whare is the place to set this ParentId?
        // improve this to support Dynamic Audit
        Long parentId = (Long) workItem.getParameter("ParentId");
        if (parentId != null) {
            taskData.setParentId(parentId);
        }
        
        // sub task strategy, need improve too.
        String subTaskStrategiesCommaSeparated = (String) workItem.getParameter("SubTaskStrategies");
        if (subTaskStrategiesCommaSeparated != null && !subTaskStrategiesCommaSeparated.equals("")) {
            String[] subTaskStrategies = subTaskStrategiesCommaSeparated.split(",");
            List<SubTasksStrategy> strategies = new ArrayList<SubTasksStrategy>();
            for (String subTaskStrategyString : subTaskStrategies) {
                SubTasksStrategy subTaskStrategy = SubTasksStrategyFactory.newStrategy(subTaskStrategyString);
                strategies.add(subTaskStrategy);
            }
            task.setSubTaskStrategies(strategies);
        }
        

        PeopleAssignments assignments = new PeopleAssignments();
        taskData.setCreatedBy(getTaskCreator(workItem));
        assignments.setPotentialOwners(getPotentialOwners(workItem));
        assignments.setBusinessAdministrators(getBusinessAdministrators());
        task.setPeopleAssignments(assignments);
        
        task.setTaskData(taskData);
        
        return task;
    }
    
    protected ContentData createContentData(WorkItem workItem) {
        ContentData content = null;
        Object contentObject = workItem.getParameter("Content");
        if (contentObject != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(contentObject);
                out.close();
                content = new ContentData();
                content.setContent(bos.toByteArray());
                content.setAccessType(AccessType.Inline);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            contentObject = workItem.getParameters();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(contentObject);
                out.close();
                content = new ContentData();
                content.setContent(bos.toByteArray());
                content.setAccessType(AccessType.Inline);
                content.setType("java.util.map");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }
    
    protected List<OrganizationalEntity> getPotentialOwners(WorkItem workItem){
        String rules = (String)workItem.getParameter("ActorId");
        if( workItem.getParameter("GroupId") != null ) {
            rules += PotentialOwnerDeterminer.RULE_SEPERATOR + (String)workItem.getParameter("GroupId");
        }
        return determiner.determine(rules, workItem);
    }
    
    protected User getTaskCreator(WorkItem workItem) {
        return determiner.determineTaskCreator(workItem);
    }
    
    protected List<OrganizationalEntity> getBusinessAdministrators() {
        List<OrganizationalEntity> businessAdministrators = new ArrayList<OrganizationalEntity>();
        businessAdministrators.add(new User(ADMINISTRATOR_ID));
        return businessAdministrators;
    }
}
