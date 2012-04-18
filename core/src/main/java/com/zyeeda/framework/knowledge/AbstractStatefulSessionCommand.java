package com.zyeeda.framework.knowledge;

public abstract class AbstractStatefulSessionCommand<T> implements StatefulSessionCommand<T> {

	private int sessionId = -1;
	
	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}
	
	public int getSessionId() {
		return this.sessionId;
	}

}
