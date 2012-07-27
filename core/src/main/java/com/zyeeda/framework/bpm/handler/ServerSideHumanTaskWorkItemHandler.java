package com.zyeeda.framework.bpm.handler;

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
import org.jbpm.task.Group;
import org.jbpm.task.I18NText;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.SubTasksStrategy;
import org.jbpm.task.SubTasksStrategyFactory;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.FaultData;
import org.jbpm.task.service.local.LocalTaskService;

import com.zyeeda.framework.bpm.ExtensionTaskHandler;

public class ServerSideHumanTaskWorkItemHandler
  implements WorkItemHandler
{
  public static final String ADMINISTRATOR_ID = "Administrator";
  private org.jbpm.task.service.TaskService humanTaskService = null;

  private KnowledgeRuntime ksession = null;

  private ExtensionTaskHandler taskHandler = null;

  public void setTaskHandler(ExtensionTaskHandler taskHandler) {
    this.taskHandler = taskHandler;
  }
  public void setHumanTaskService(org.jbpm.task.service.TaskService humanTaskService) {
    this.humanTaskService = humanTaskService;
  }
  public void setKsession(KnowledgeRuntime ksession) {
    this.ksession = ksession;
  }

  public void executeWorkItem(WorkItem workItem, WorkItemManager manager)
  {
    Task task = createTask(workItem);
    ContentData content = createContentData(workItem);

    org.jbpm.task.TaskService service = new LocalTaskService(this.humanTaskService);
    service.addTask(task, content);

    if (this.taskHandler != null)
      this.taskHandler.executeTask(workItem, task);
  }

  public void abortWorkItem(WorkItem workItem, WorkItemManager manager)
  {
    org.jbpm.task.TaskService service = new LocalTaskService(this.humanTaskService);

    Task task = service.getTaskByWorkItemId(workItem.getId());
    service.skip(task.getId().longValue(), "Administrator");
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
protected Task createTask(WorkItem workItem)
  {
    Task task = new Task();

    String taskName = (String)workItem.getParameter("TaskName");
    if (taskName != null) {
      List names = new ArrayList();
      names.add(new I18NText("en-UK", taskName));
      task.setNames(names);
    }

    String comment = (String)workItem.getParameter("Comment");
    if (comment != null) {
      List descriptions = new ArrayList();
      descriptions.add(new I18NText("en-UK", comment));
      task.setDescriptions(descriptions);
      List subjects = new ArrayList();
      subjects.add(new I18NText("en-UK", comment));
      task.setSubjects(subjects);
    }

    String priorityString = (String)workItem.getParameter("Priority");
    int priority = 0;
    if (priorityString != null) {
      try {
        priority = Integer.parseInt(priorityString);
      }
      catch (NumberFormatException e)
      {
      }
    }
    task.setPriority(priority);

    TaskData taskData = new TaskData();

    String signalName = (String)workItem.getParameter("SignalName");

    FaultData faultData = new FaultData();
    faultData.setFaultName(signalName);
    taskData.setFault(0L, faultData);

    taskData.setWorkItemId(workItem.getId());

    taskData.setProcessInstanceId(workItem.getProcessInstanceId());

    if ((this.ksession != null) && (this.ksession.getProcessInstance(workItem.getProcessInstanceId()) != null)) {
      taskData.setProcessId(this.ksession.getProcessInstance(workItem.getProcessInstanceId()).getProcess().getId());
    }

    if ((this.ksession != null) && ((this.ksession instanceof StatefulKnowledgeSession))) {
      taskData.setProcessSessionId(((StatefulKnowledgeSession)this.ksession).getId());
    }

    taskData.setSkipable(!"false".equals(workItem.getParameter("Skippable")));

    Long parentId = (Long)workItem.getParameter("ParentId");
    if (parentId != null) {
      taskData.setParentId(parentId.longValue());
    }

    String subTaskStrategiesCommaSeparated = (String)workItem.getParameter("SubTaskStrategies");
    if ((subTaskStrategiesCommaSeparated != null) && (!subTaskStrategiesCommaSeparated.equals(""))) {
      String[] subTaskStrategies = subTaskStrategiesCommaSeparated.split(",");
      List strategies = new ArrayList();
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
      try
      {
        ObjectOutputStream out = new ObjectOutputStream(bos);
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
      try
      {
        ObjectOutputStream out = new ObjectOutputStream(bos);
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

  @SuppressWarnings({ "rawtypes", "unchecked" })
protected List<OrganizationalEntity> getPotentialOwners(WorkItem workItem) {
    List potentialOwners = new ArrayList();
    String actorId = (String)workItem.getParameter("ActorId");
    if ((actorId != null) && (actorId.trim().length() > 0)) {
      String[] actorIds = actorId.split(",");
      for (String id : actorIds) {
        potentialOwners.add(new User(id.trim()));
      }
    }

    String groupId = (String)workItem.getParameter("GroupId");
    if ((groupId != null) && (groupId.trim().length() > 0)) {
      String[] groupIds = groupId.split(",");
      for (String id : groupIds) {
        potentialOwners.add(new Group(id.trim()));
      }
    }
    return potentialOwners;
  }

  protected User getTaskCreator(WorkItem workItem)
  {
    return new User("Administrator");
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
protected List<OrganizationalEntity> getBusinessAdministrators() {
    List businessAdministrators = new ArrayList();
    businessAdministrators.add(new User("Administrator"));
    return businessAdministrators;
  }
}