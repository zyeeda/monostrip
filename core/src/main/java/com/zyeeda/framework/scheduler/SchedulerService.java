package com.zyeeda.framework.scheduler;

import com.zyeeda.framework.service.Service;

public interface SchedulerService<T> extends Service {

	public T getScheduler();
	
}
