package com.zyeeda.framework.knowledge.support;

import javax.persistence.EntityManagerFactory;

import org.drools.KnowledgeBase;
import org.drools.impl.EnvironmentFactory;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import com.zyeeda.framework.knowledge.KnowledgeService;

/**
 * Domain object for retrieving pre-configured KnowledgeSessions.
 * Designed for use with Spring.
 */
public class DefaultKnowledgeService implements KnowledgeService {
	private static final Logger logger = LoggerFactory.getLogger(DefaultKnowledgeService.class);
	
	private KnowledgeBase kbase;
	private EntityManagerFactory entityManagerFactory;
	private AbstractPlatformTransactionManager transactionManager;
	
	public void setKbase(KnowledgeBase kbase) {
        this.kbase = kbase;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void setTransactionManager(AbstractPlatformTransactionManager transactionManager) {
        logger.debug("Setting transaction manager: {}", transactionManager);
        this.transactionManager = transactionManager;
    }
	
	@Override
	public StatefulKnowledgeSession createKnowledgeSession() {
	    logger.debug("Create a new StatefulKnowledgeSession.");
	    
	    Environment env = this.getPersistenceEnvironment();
	    StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(this.kbase, null, env);
	    this.prepareKnowledgeSession(ksession, env);
	    return ksession;
	}
	
	@Override
	public StatefulKnowledgeSession getKnowledgeSession(int sessionId) {
	    logger.debug("Load StatefulKnowledgeSession with id {}.", sessionId);
	    
	    Environment env = this.getPersistenceEnvironment();
	    StatefulKnowledgeSession ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, this.kbase, null, env);
	    this.prepareKnowledgeSession(ksession, env);
	    return ksession;
	}
	
	private void prepareKnowledgeSession(StatefulKnowledgeSession ksession, Environment env) {
	    KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
	    
	    // TODO: Rewrite HistoryLogger
	    JPAProcessInstanceDbLog.setEnvironment(env);
	    new JPAWorkingMemoryDbLogger(ksession);
	}
	
	/**
	 * The jBPM persistence layer will obtain persistence resources from an Environment
	 * object. This method configures the object and returns it for use by the jBPM
	 * persistence objects.
	 * @return configured Environment object
	 */
	private Environment getPersistenceEnvironment() {
		Environment env = EnvironmentFactory.newEnvironment();
		
		logger.debug("Setting environmental EntityManagerFactory to {}", entityManagerFactory);
		env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory);
		
		logger.debug("Setting environmental TransactionManager to {}", transactionManager);
		env.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
		
		return env;
	}

}
