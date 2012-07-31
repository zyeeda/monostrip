package com.zyeeda.framework.knowledge;

import org.drools.runtime.StatefulKnowledgeSession;


public interface KnowledgeService {
	
	StatefulKnowledgeSession getProcessSession();
	
	StatefulKnowledgeSession getProcessSession(int sessionId);
	
}
