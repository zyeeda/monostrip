package com.zyeeda.framework.bpm;

import org.drools.runtime.StatefulKnowledgeSession;


public interface KnowledgeService {
	
	StatefulKnowledgeSession createKnowledgeSession();
	
	StatefulKnowledgeSession getKnowledgeSession(int sessionId);
	
}
