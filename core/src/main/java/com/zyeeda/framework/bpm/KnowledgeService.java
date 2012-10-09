package com.zyeeda.framework.bpm;

import org.drools.runtime.StatefulKnowledgeSession;


public interface KnowledgeService {
    
    void initialize();
	
	StatefulKnowledgeSession createKnowledgeSession();
	
	StatefulKnowledgeSession getKnowledgeSession(int sessionId);
	
	HumanTaskService getHumanTaskService();
	
}
