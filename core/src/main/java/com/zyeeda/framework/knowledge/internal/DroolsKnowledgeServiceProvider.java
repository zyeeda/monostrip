package com.zyeeda.framework.knowledge.internal;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SystemEventListenerFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.task.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import com.zyeeda.framework.flow.CompleteHumanTaskWorkItemEventListener;
import com.zyeeda.framework.flow.PotentialOwnerDeterminer;
import com.zyeeda.framework.flow.ServerSideHumanTaskWorkItemHandler;
import com.zyeeda.framework.knowledge.KnowledgeService;
import com.zyeeda.framework.knowledge.StatefulSessionCommand;
import com.zyeeda.framework.persistence.internal.DefaultPersistenceServiceProvider;
import com.zyeeda.framework.service.AbstractService;

public class DroolsKnowledgeServiceProvider extends AbstractService implements KnowledgeService {
	private static final Logger logger = LoggerFactory.getLogger(DroolsKnowledgeServiceProvider.class);
	
	
	private static final String DEFAUL_AUDIT_LOG_FILE_PATH = "jbpm/logs/audit_log";
	private static final int DEFAULT_AUDIT_LOG_FLUSH_INTERVAL = 60 * 60 * 1000;
	
	private String auditLogFilePath = null;
	private int auditLogFlushInterval = -1;
	
	private KnowledgeBase kbase = null;
	
	private EntityManager entityManager = null;
	private EntityManagerFactory entityManagerFactory = null;
	private PlatformTransactionManager transactionManager = null;
	private PotentialOwnerDeterminer determiner = null;
	
	@Autowired
	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

	@Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

	@Autowired
	public void setDeterminer(PotentialOwnerDeterminer determiner) {
        this.determiner = determiner;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setAuditLogFilePath(String auditLogFilePath) {
        this.auditLogFilePath = auditLogFilePath;
    }

    public void setAuditLogFlushInterval(int auditLogFlushInterval) {
        this.auditLogFlushInterval = auditLogFlushInterval;
    }

    @Override
    public TaskService getTaskService() {
        return new TaskService(entityManagerFactory, SystemEventListenerFactory.getSystemEventListener());
    }

    @Override
    public void start() throws Exception {
        
		this.auditLogFilePath = auditLogFilePath == null ? DEFAUL_AUDIT_LOG_FILE_PATH : auditLogFilePath;
		this.auditLogFlushInterval = auditLogFlushInterval < 0 ?  DEFAULT_AUDIT_LOG_FLUSH_INTERVAL : auditLogFlushInterval;
		
		if (logger.isDebugEnabled()) {
			logger.debug("audit log path = {}", this.auditLogFilePath);
			logger.debug("audit log flush interval = {}", this.auditLogFlushInterval);
		}
		
		File auditLogFile = new File(this.auditLogFilePath);
		if (!auditLogFile.exists()) {
			File logDir = auditLogFile.getParentFile();
			if (!logDir.exists()) {
				logger.info("Audit log file container {} does not exist, make dir first.", logDir);
				logDir.mkdirs();
			} else {
				if (!logDir.isDirectory()) {
					logger.warn("Audit log file container {} should be a directory but a file found instead, delete the file then make dir.", logDir);
					logDir.delete();
					logDir.mkdirs();
				}
			}
		}
	}
	
	@Override
	public KnowledgeBase getKnowledgeBase() {
		return this.kbase;
	}
	
	@Autowired
	public void setKbase(KnowledgeBase kbase) {
        this.kbase = kbase;
    }

    @Override
	public <T> T execute(StatefulSessionCommand<T> command) throws Exception {
		StatefulKnowledgeSession ksession = null;
		KnowledgeRuntimeLogger rtLogger = null;

		TaskService taskService = new TaskService(entityManagerFactory, SystemEventListenerFactory.getSystemEventListener());
		
		CompleteHumanTaskWorkItemEventListener listener = new CompleteHumanTaskWorkItemEventListener();
        taskService.addEventListener(listener);
        
        ServerSideHumanTaskWorkItemHandler handler = new ServerSideHumanTaskWorkItemHandler();
        handler.setDeterminer(determiner);
        handler.setTaskService(taskService);
		
		Environment env = KnowledgeBaseFactory.newEnvironment();
		env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory);
		env.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);

		try {
			
			if (command.getSessionId() > 0) {
				ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(command.getSessionId(), this.kbase, null, env);
			} else {
				ksession = JPAKnowledgeService.newStatefulKnowledgeSession(this.kbase, null, env);
			}
			
			WorkItemManager workItemManager = ksession.getWorkItemManager();
			listener.setManager(workItemManager);
			handler.setSession(ksession);
			workItemManager.registerWorkItemHandler("Human Task", handler);
			workItemManager.registerWorkItemHandler("User Task", handler);
			
			rtLogger = KnowledgeRuntimeLoggerFactory.newThreadedFileLogger(ksession, auditLogFilePath, this.auditLogFlushInterval);
			DefaultPersistenceServiceProvider persistenceServiceProvider = new DefaultPersistenceServiceProvider();
			persistenceServiceProvider.setEntityManager(entityManager);
			new HistoryLogger(ksession, persistenceServiceProvider, this.kbase);
			
			T result = command.execute(ksession);
			
			return result;
		} catch (Throwable t) {
			logger.error("Execute command failed.", t);
			throw new Exception(t);
		} finally {
			if (rtLogger != null) {
				try {
					rtLogger.close();
				} catch (Throwable t) {
					logger.error("Close knowledge runtime logger failed.", t);
				}
			}
			if (ksession != null) {
				ksession.dispose();
			}
		}
	}
}
