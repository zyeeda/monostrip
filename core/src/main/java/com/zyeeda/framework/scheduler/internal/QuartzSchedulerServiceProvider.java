package com.zyeeda.framework.scheduler.internal;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zyeeda.framework.scheduler.SchedulerService;
import com.zyeeda.framework.service.AbstractService;

public class QuartzSchedulerServiceProvider extends AbstractService implements SchedulerService<Scheduler> {

	private SchedulerFactory schedulerFactory;
	private Scheduler scheduler;
	
	@Autowired
	public void setSchedulerFactory(SchedulerFactory schedulerFactory) {
        this.schedulerFactory = schedulerFactory;
    }

    @Override
    public void start() throws SchedulerException {
		this.scheduler = this.schedulerFactory.getScheduler();
		this.scheduler.start();
	}
	
	@Override
	public void stop() throws SchedulerException {
		this.scheduler.shutdown();
	}

	@Override
	public Scheduler getScheduler() {
		return this.scheduler;
	}

}
