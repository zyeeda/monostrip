package com.zyeeda.cdeio.knowledge;

import org.drools.runtime.StatefulKnowledgeSession;


public interface KnowledgeService {
    
	StatefulKnowledgeSession createKnowledgeSession();
	
	StatefulKnowledgeSession getKnowledgeSession(int sessionId);
	
}
