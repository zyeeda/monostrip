package com.zyeeda.framework.bpm.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.drools.SystemEventListenerFactory;
import org.drools.container.spring.beans.persistence.HumanTaskSpringTransactionManager;
import org.drools.definition.process.Connection;
import org.drools.definition.process.Node;
import org.drools.persistence.PersistenceContext;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.info.WorkItemInfo;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.NodeInstanceContainer;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.task.Comment;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.task.service.persistence.TaskSessionSpringFactoryImpl;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import com.zyeeda.framework.bpm.CoalaBpmConstants;
import com.zyeeda.framework.bpm.HumanTaskEventListener;
import com.zyeeda.framework.bpm.HumanTaskService;
import com.zyeeda.framework.bpm.InvalidTaskStatusException;
import com.zyeeda.framework.bpm.KnowledgeService;
import com.zyeeda.framework.bpm.TaskServiceException;

public class DefaultHumanTaskService implements HumanTaskService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHumanTaskService.class);
    
    //private static final String REJECT_EVENT_SUFFIX = "(RejectEvent)";
    //private static final String REVOKE_EVENT_SUFFIX = "(RevokeEvent)";
    
    private EntityManagerFactory emf;
    private AbstractPlatformTransactionManager txMgr;
    private KnowledgeService kservice;
    private List<HumanTaskEventListener> listeners = new ArrayList<HumanTaskEventListener>();
    
    private TaskService taskService;
    
    public void setEntityManagerFactory(EntityManagerFactory emf) {
        this.emf = emf;
    }
    
    public void setTransactionManager(AbstractPlatformTransactionManager txMgr) {
        this.txMgr = txMgr;
    }
    
    public void setKnowledgeService(KnowledgeService kservice) {
        this.kservice = kservice;
    }
    
    public void setEventListeners(List<HumanTaskEventListener> listeners) {
        this.listeners = listeners;
    }
    
    public void initialize() {
        this.taskService = new TaskService();
        this.taskService.setSystemEventListener(SystemEventListenerFactory.getSystemEventListener());
        
        TaskSessionSpringFactoryImpl sessionFactory = new TaskSessionSpringFactoryImpl();
        sessionFactory.setEntityManagerFactory(this.emf);
        sessionFactory.setTransactionManager(new HumanTaskSpringTransactionManager(this.txMgr));
        sessionFactory.setUseJTA(true);
        sessionFactory.setTaskService(this.taskService);
        sessionFactory.initialize();
    }
    
    @Override
    public void createTask(Task task, ContentData content) {
        LocalTaskService localTaskService = new LocalTaskService(this.taskService);
        localTaskService.addTask(task, content);
        
        for (HumanTaskEventListener listener : this.listeners) {
            listener.taskCreated(task.getId());
        }
    }
    
    @Override
    public void exitTaskByWorkItemId(Long workItemId) {
        LocalTaskService localTaskService = new LocalTaskService(this.taskService);
        Task task = localTaskService.getTaskByWorkItemId(workItemId);
        // TODO: Administrator?
        localTaskService.exit(task.getId(), "Administrator");
        
        for (HumanTaskEventListener listener : this.listeners) {
            listener.taskExited(task.getId());
        }
    }
    
    @Override
    public void claim(Long taskId, String userId) {
        LocalTaskService localTaskService = new LocalTaskService(this.taskService);
        localTaskService.claim(taskId, userId);
        
        for (HumanTaskEventListener listener : this.listeners) {
            listener.taskClaimed(taskId);
        }
    }
    
    @Override
    public void start(Long taskId, String userId) {
        LocalTaskService localTaskService = new LocalTaskService(this.taskService);
        localTaskService.start(taskId, userId);
        
        for (HumanTaskEventListener listener : this.listeners) {
            listener.taskStarted(taskId);
        }
    }

    @Override
    public void complete(Long taskId, String comment, Map<String, Object> results) throws InvalidTaskStatusException {
        LocalTaskService localTaskService = new LocalTaskService(this.taskService);
        Task task = localTaskService.getTask(taskId);
        TaskData taskData = task.getTaskData();
        
        if (taskData.getStatus() != Status.InProgress) {
            throw new InvalidTaskStatusException(taskData.getStatus(), Status.InProgress);
        }
        
        StatefulKnowledgeSession ksession = this.kservice.getKnowledgeSession(taskData.getProcessSessionId());
        WorkflowProcessInstance wpi = (WorkflowProcessInstance) ksession.getProcessInstance(taskData.getProcessInstanceId());
        
        long workItemId = taskData.getWorkItemId();
        String userId = taskData.getActualOwner().getId();

        if (results == null) {
            results = new HashMap<String, Object>();
        }
        // TODO: Why need this?
        results.put("ActorId", userId);
        if (StringUtils.isNotBlank(comment)) {
            Comment cmt = new Comment();
            cmt.setAddedAt(new Date());
            cmt.setAddedBy(new User(userId));
            cmt.setText(comment);
            localTaskService.addComment(taskId, cmt);
        }
        
        localTaskService.completeWithResults(taskId, userId, results);
        
        ksession.getWorkItemManager().completeWorkItem(workItemId, results);
        
        for (NodeInstance ni : wpi.getNodeInstances()) {
            System.out.println(ni.getNodeName());
        }
        for (HumanTaskEventListener listener : this.listeners) {
            listener.taskCompleted(taskId);
        }
    }
    
    @Override
    public void reject(Long taskId) throws TaskServiceException {
        LocalTaskService localTaskService = new LocalTaskService(this.taskService);
        Task task = localTaskService.getTask(taskId);
        TaskData taskData = task.getTaskData();
        Status taskStatus = taskData.getStatus();
        
        if (taskStatus == Status.Completed || taskStatus == Status.Failed || taskStatus == Status.Error || taskStatus == Status.Exited || taskStatus == Status.Obsolete) {
            throw new TaskServiceException("The task " + taskId + " is not active.");
        }
        
        StatefulKnowledgeSession ksession = this.kservice.getKnowledgeSession(taskData.getProcessSessionId());
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(taskData.getProcessInstanceId());
        
        // No workitem here, because the task is inactive.
        /*PersistenceContext context = this.getPersistenceContext(ksession);
        WorkItemInfo workItemInfo = context.findWorkItemInfo(taskData.getWorkItemId());
        if (workItemInfo == null) {
            throw new TaskServiceException("The WorkItemInfo of task " + taskId + " is null.");
        }*/
        
        HumanTaskNodeInstance nodeInstance = this.findHumanTaskNodeInstance(taskData.getWorkItemId(), processInstance);
        HumanTaskNode node = nodeInstance.getHumanTaskNode();
        
        String strRejectable = node.getInMapping(CoalaBpmConstants.REJECTABLE);
        boolean rejectable = BooleanUtils.toBoolean(strRejectable);
        LOGGER.debug("task {} rejectable = {}", taskId, rejectable);
        if (!rejectable) {
            throw new TaskServiceException("The task " + taskId + " is not rejectable.");
        }
        
        String eventType = node.getInMapping(CoalaBpmConstants.REJECT_EVENT_TYPE);
        LOGGER.debug("reject event type = {}", eventType);
        if (StringUtils.isBlank(eventType)) {
            throw new TaskServiceException("Reject event type is not specified.");
        }
        
        /*InternalRuleBase internalRuleBase = (InternalRuleBase) ((InternalKnowledgeBase) ksession.getKnowledgeBase()).getRuleBase();
        WorkItem workItem = workItemInfo.getWorkItem(ksession.getEnvironment(), internalRuleBase);
        
        Boolean rejectable = (Boolean) workItem.getParameter(CoalaBpmConstants.REJECTABLE);
        LOGGER.debug("task {} rejectable = {}", taskId, rejectable);
        if (BooleanUtils.isNotTrue(rejectable)) {
            throw new TaskServiceException("The task " + taskId + " is not rejectable.");
        }
        
        String eventType = (String) workItem.getParameter(CoalaBpmConstants.REJECT_EVENT_TYPE);
        LOGGER.debug("reject event type = {}", eventType);
        if (StringUtils.isBlank(eventType)) {
            throw new TaskServiceException("Reject event type is not specified.");
        }*/
        
        this.checkEventNode((WorkflowProcess) processInstance.getProcess(), eventType);
        processInstance.signalEvent(eventType, null);
        
        ksession.getWorkItemManager().abortWorkItem(taskData.getWorkItemId());
        taskData.setStatus(Status.Obsolete);
        
        for (HumanTaskEventListener listener : this.listeners) {
            listener.taskRejected(taskId);
        }
    }
    
    @Override
    public void revoke(Long taskId) throws TaskServiceException {
        TaskServiceSession tss = this.taskService.createSession();
        Task task = tss.getTask(taskId);
        TaskData taskData = task.getTaskData();
        
        if (taskData.getStatus() != Status.Completed) {
            throw new TaskServiceException("Active task cannot be revoked.");
        }
        
        StatefulKnowledgeSession ksession = this.kservice.getKnowledgeSession(taskData.getProcessSessionId());
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(taskData.getProcessInstanceId());
        if (processInstance == null || processInstance.getState() != ProcessInstance.STATE_ACTIVE) {
            throw new TaskServiceException("Process instance no longer exists or is not active.");
        }
        
        HumanTaskNodeInstance humanTaskNodeInstance = this.findHumanTaskNodeInstance(taskData.getWorkItemId(), processInstance);
        HumanTaskNode humanTaskNode = humanTaskNodeInstance.getHumanTaskNode();
        String strRevokable = humanTaskNode.getInMapping(CoalaBpmConstants.REVOKABLE);
        boolean revokable = BooleanUtils.toBoolean(strRevokable);
        if (!revokable) {
            throw new TaskServiceException("The task " + taskId + " is not revokable.");
        }
        
        Map<String, List<Connection>> outgoingConnections = humanTaskNode.getOutgoingConnections();
        List<Long> downstreamHumanTaskNodeIds = new ArrayList<Long>();
        for (Map.Entry<String, List<Connection>> entry : outgoingConnections.entrySet()) {
            List<Connection> conns = entry.getValue();
            for (Connection conn : conns) {
                Node node = conn.getTo();
                if (!(node instanceof HumanTaskNode)) {
                    throw new TaskServiceException("Not all downstream nodes are of HumanTaskNode.");
                }
                downstreamHumanTaskNodeIds.add(node.getId());
            }
        }
        
        Query query = tss.getTaskPersistenceManager().createNewQuery("findDownstreamTasks");
        query.setParameter("processInstanceId", taskData.getProcessInstanceId());
        query.setParameter("createdOn", taskData.getCreatedOn(), TemporalType.TIMESTAMP);
        List<Task> downstreamTasks = query.getResultList();
        if (downstreamTasks.size() == 0) {
            throw new TaskServiceException("No downstream task exists.");
        }
        
        List<Long> downstreamWorkItemIds = new ArrayList<Long>(downstreamTasks.size());
        Map<Long, Task> taskMapping = new HashMap<Long, Task>(downstreamTasks.size());
        for (Task t : downstreamTasks) {
            downstreamWorkItemIds.add(t.getTaskData().getWorkItemId());
            taskMapping.put(t.getTaskData().getWorkItemId(), t);
        }
        List<HumanTaskNodeInstance> downstreamNodeInstances = this.findHumanTaskNodeInstances(downstreamWorkItemIds, processInstance);
        List<Task> potentialTasks = new ArrayList<Task>(downstreamTasks.size());
        for (HumanTaskNodeInstance nodeInstance : downstreamNodeInstances) {
            if (downstreamHumanTaskNodeIds.contains(nodeInstance.getHumanTaskNode().getId())) {
                potentialTasks.add(taskMapping.get(nodeInstance.getWorkItemId()));
            }
        }
        
        if (potentialTasks.size() < downstreamHumanTaskNodeIds.size()) {
            throw new TaskServiceException("Downstream human task instance number is less than human task node number.");
        }
        
        List<Task> selectedTasks = new ArrayList<Task>(potentialTasks.size());
        for (int i = 0; i < downstreamHumanTaskNodeIds.size(); i++) {
            Task t = potentialTasks.get(i);
            if (t.getTaskData().getStatus() != Status.Ready) {
                throw new TaskServiceException("Some task has been claimed or completed.");
            }
            selectedTasks.add(t);
        }
        
        System.out.println(selectedTasks);
        
        /*TaskServiceSession tss = this.taskService.createSession();
        Task task = tss.getTask(taskId);
        TaskData taskData = task.getTaskData();
        
        StatefulKnowledgeSession ksession = this.kservice.getKnowledgeSession(task.getTaskData().getProcessSessionId());
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(task.getTaskData().getProcessInstanceId());
        if (processInstance == null || processInstance.getState() != ProcessInstance.STATE_ACTIVE) {
            throw new TaskServiceException("Process instance does not exist or is not active.");
        }
        
        Boolean revokable = (Boolean) processInstance.getVariable(TaskHelper.getVariableName(task.getId(), BpmConstants.COALA_RECALL_ABLE));
        if (BooleanUtils.isNotTrue(revokable)) {
            throw new TaskServiceException("The task " + task.getId() + " is not revokable.");
        }


        
        long workItemId = task.getTaskData().getWorkItemId();
        //faultName 用于存放task节点的名字
        String rejectNodeName = task.getTaskData().getFaultName() + SIGNAL_SUFFIX;
        
        
        ksession.signalEvent(rejectNodeName, null, task.getTaskData().getProcessInstanceId());
        
        Environment env = ksession.getEnvironment();
        PersistenceContext context = ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();
        //删除workitem、标记task为完成状态，因为signalEvent后workitem和task 依然存在
        WorkItemInfo workIntmInfo = context.findWorkItemInfo(workItemId);
        context.remove(workIntmInfo);
        
        tss.setTaskStatus(task.getId(), Status.Exited);
        if (this.taskHandler != null){
            taskHandler.deleteTask(task);
        }*/
    }
    
    private PersistenceContext getPersistenceContext(StatefulKnowledgeSession ksession) {
        Environment env = ksession.getEnvironment();
        PersistenceContext context = ((PersistenceContextManager) env.get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER)).getApplicationScopedPersistenceContext();
        return context;
    }
    
    private WorkItemInfo getWorkItemInfo(StatefulKnowledgeSession ksession, Long workItemId) {
        PersistenceContext context = this.getPersistenceContext(ksession);
        WorkItemInfo workItemInfo = context.findWorkItemInfo(workItemId);
        return workItemInfo;
    }
    
    private HumanTaskNodeInstance findHumanTaskNodeInstance(long workItemId, NodeInstanceContainer container) {
        List<HumanTaskNodeInstance> instances = this.findHumanTaskNodeInstances(Arrays.asList(workItemId), container);
        if (instances.size() > 0) {
            return instances.get(0);
        }
        return null;
    }
    
    private List<HumanTaskNodeInstance> findHumanTaskNodeInstances(List<Long> workItemIds, NodeInstanceContainer container) {
        List<HumanTaskNodeInstance> instances = new ArrayList<HumanTaskNodeInstance>(workItemIds.size());
        for (NodeInstance nodeInstance : container.getNodeInstances()) {
            if (nodeInstance instanceof HumanTaskNodeInstance) {
                HumanTaskNodeInstance humanTaskNodeInstance = (HumanTaskNodeInstance) nodeInstance;
                if (workItemIds.contains(humanTaskNodeInstance.getWorkItemId())) {
                    instances.add(humanTaskNodeInstance);
                }
            } else if (nodeInstance instanceof NodeInstanceContainer) {
                List<HumanTaskNodeInstance> results = this.findHumanTaskNodeInstances(workItemIds, ((NodeInstanceContainer) nodeInstance));
                instances.addAll(results);
            }
        }
        return instances;
    }
    
    /**
     * 检查流程定义中是否包含给定的节点.
     * 
     * @param workflowProcess
     * @param nodeName
     *
     * @return
     */
    private void checkEventNode(WorkflowProcess workflowProcess, String eventType) throws TaskServiceException {
        for (Node node : workflowProcess.getNodes()) {
            if (node instanceof EventNode) {
                EventNode eventNode = (EventNode) node;
                if (eventNode.getType() != null && eventNode.getType().equals(eventType)) {
                    return;
                }
            }
        }
        throw new TaskServiceException("Event node of type [" + eventType + "] does not exist.");
    }
    

    
/*
	private final String SIGNAL_SUBFIX = "-Signal";
	private org.jbpm.task.service.TaskService humanTaskService = null;
	private KnowledgeBase kbase = null;
    private ExtensionTaskHandler taskHandler = null;
//    private StatefulKnowledgeSession ksession = null;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private TransactionManager transactionManager;
    
    public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.entityManagerFactory = emf;
	}
//	public void setKsession(StatefulKnowledgeSession ksession) {
//		this.ksession = ksession;
//	}
	public void setEntityManager(EntityManager em) {
		this.entityManager = em;
	}
	public void setTransactionManager(TransactionManager tm) {
		this.transactionManager = tm;
	}
	public void setKbase(KnowledgeBase kbase) {
    	this.kbase = kbase;
    }
    public void setTaskHandler(ExtensionTaskHandler taskHandler) {
		this.taskHandler = taskHandler;
	}
    public void setHumanTaskService(org.jbpm.task.service.TaskService humanTaskService) {
        this.humanTaskService = humanTaskService;
    }
    private StatefulKnowledgeSession getKsession(int ksessionId){
    	Environment env = KnowledgeBaseFactory.newEnvironment();
    	env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory);
		env.set(EnvironmentName.APP_SCOPED_ENTITY_MANAGER, entityManager);
//		env.set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, entityManager);
		env.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
//		env.set(EnvironmentName.TRANSACTION, null);
//		env.set(EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY, null);
//		env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, null);
//		
		StatefulKnowledgeSession ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(
				ksessionId, this.kbase, null, env);
    	return ksession;
    }

    @Transactional
    @Override
	public void claim(Long taskId, String userId) {
		LocalTaskService localTaskService = new LocalTaskService(humanTaskService);
		localTaskService.claim(taskId, userId);
	}
    @Transactional
	@Override
	public void complete(Long taskId, String comment, Map<String,Object> results) {
		TaskServiceSession tss = humanTaskService.createSession();
        Task task = tss.getTask(taskId);
        long workItemId = task.getTaskData().getWorkItemId();
        String userId = task.getTaskData().getActualOwner().getId();

        //task 在 start后(处于InProgress状态)才能complete
        tss.taskOperation(Operation.Start, task.getId(), userId, null, null, null);
		ContentData contentData = null;
		if (results != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out;
			try {
				results.put("ActorId", userId);
				out = new ObjectOutputStream(bos);
				out.writeObject(results);
				out.close();
				contentData = new ContentData();
				contentData.setContent(bos.toByteArray());
				contentData.setAccessType(AccessType.Inline);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			results = new HashMap<String, Object>();
			results.put("ActorId", userId);
		}
        //complete 任务
        tss.taskOperation(Operation.Complete, task.getId(), userId, null, contentData, null);
        Comment cmt = null;
        if(comment!=null&&"".equals(comment.trim())){
        	cmt = new Comment();
        	cmt.setAddedAt(new Date());
        	cmt.setAddedBy(new User(userId));
        	cmt.setText(comment);
        	tss.addComment(task.getId(), cmt);
        }
//        tss.setTaskStatus(task.getId(), Status.Completed);
//        StatefulKnowledgeSession ksession = getKsession(task.getTaskData().getProcessSessionId());
//        tss.dispose();//不能销毁，否则tss的操作将不会在事务内提交
        StatefulKnowledgeSession ksession = getKsession(task.getTaskData().getProcessSessionId());
        ksession.getWorkItemManager().completeWorkItem(workItemId, results);
		
	}
    @Transactional
	@Override
	public void reject(Long taskId) {
		TaskServiceSession tss = humanTaskService.createSession();
        Task task = tss.getTask(taskId);
		long workItemId = task.getTaskData().getWorkItemId();
//		task.getTaskData().getWorkItemId()
        //faultName 用于存放task节点的名字
        String rejectNodeName = task.getTaskData().getFaultName()+SIGNAL_SUBFIX;
//        ProcessInstance processInstance = ksession.getProcessInstance(task.getTaskData().getProcessInstanceId());
        //通过下面的方式无法获取到workItem??
//        WorkItem workItem = ((org.drools.process.instance.WorkItemManager)manager).getWorkItem(workItemId);
//        String signalName = (String)workItem.getParameter("signalName");
        StatefulKnowledgeSession ksession = getKsession(task.getTaskData().getProcessSessionId());
		ksession.signalEvent(rejectNodeName, null, task.getTaskData().getProcessInstanceId());
//		manager.abortWorkItem(workItemId);
//		manager.completeWorkItem(id, results)//无论是complete 还是abort流程都会继续向下走，所以需要删除原来的workitem并标记task为exited
		Environment env = ksession.getEnvironment();
		PersistenceContext context = ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();
		WorkItemInfo workIntmInfo = context.findWorkItemInfo(workItemId);
		context.remove(workIntmInfo);
        tss.setTaskStatus(task.getId(), Status.Exited);
        if(taskHandler!=null){
        	taskHandler.exitTask(task);
        }
	}
    @Transactional
	@Override
	public void recall(Long taskId) {
		TaskServiceSession tss = humanTaskService.createSession();
        Task task = tss.getTask(taskId);
		long workItemId = task.getTaskData().getWorkItemId();
//		task.getTaskData().getWorkItemId()
        //faultName 用于存放task节点的名字
        String rejectNodeName = task.getTaskData().getFaultName()+SIGNAL_SUBFIX;
        StatefulKnowledgeSession ksession = getKsession(task.getTaskData().getProcessSessionId());
        ksession.signalEvent(rejectNodeName, null, task.getTaskData().getProcessInstanceId());
		Environment env = ksession.getEnvironment();
		PersistenceContext context = ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();
		//删除workitem、标记task为完成状态，因为signalEvent后workitem和task 依然存在
		WorkItemInfo workIntmInfo = context.findWorkItemInfo(workItemId);
		context.remove(workIntmInfo);
        tss.setTaskStatus(task.getId(), Status.Exited);
        if(taskHandler!=null){
        	taskHandler.deleteTask(task);
        }
	}
	*/
}
