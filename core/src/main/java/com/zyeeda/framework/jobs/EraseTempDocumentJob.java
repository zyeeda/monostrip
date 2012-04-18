package com.zyeeda.framework.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zyeeda.framework.managers.DocumentManager;
import com.zyeeda.framework.managers.internal.MongoDbDocumentManager;
import com.zyeeda.framework.nosql.MongoDbService;

public class EraseTempDocumentJob implements Job {
	
	private final static Logger logger = LoggerFactory.getLogger(EraseTempDocumentJob.class);
	
	private MongoDbService mongoSvc;
	
	@Autowired
	public void setMongoSvc(MongoDbService mongoSvc) {
        this.mongoSvc = mongoSvc;
    }

    @Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		DocumentManager mgr = new MongoDbDocumentManager(this.mongoSvc);
		long count = mgr.countByIsTemp();
		logger.info("Erasing temporary documents, {} to erase ...", count);
		mgr.eraseTemp();
		logger.info("Temporary documents erased!");
	}

}
