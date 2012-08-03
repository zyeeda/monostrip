package com.zyeeda.framework.bpm;

import org.drools.runtime.StatefulKnowledgeSession;


public interface KnowledgeService {
	
	StatefulKnowledgeSession getProcessSession();
	
	StatefulKnowledgeSession getProcessSession(int sessionId);
	
}
