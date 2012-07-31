package com.zyeeda.framework.bpm.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.persistence.PersistenceContext;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.TransactionManager;
import org.drools.persistence.info.WorkItemInfo;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.task.AccessType;
import org.jbpm.task.Comment;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.User;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.Operation;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;
import org.springframework.transaction.annotation.Transactional;

import com.zyeeda.framework.bpm.ExtensionTaskHandler;
import com.zyeeda.framework.bpm.TaskService;

public class DefaultTaskServiceSupport implements TaskService{

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
}
