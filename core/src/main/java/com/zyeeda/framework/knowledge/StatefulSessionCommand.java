package com.zyeeda.framework.knowledge;

import org.drools.runtime.StatefulKnowledgeSession;

public interface StatefulSessionCommand<T> {

	public T execute(StatefulKnowledgeSession ksession);
	
	
	public int getSessionId(); 
	
}
