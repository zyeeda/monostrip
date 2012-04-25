package com.zyeeda.framework.nosql;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.zyeeda.framework.service.Service;

public interface MongoDbService extends Service {

	public Mongo getMongo();
	
	public DB getDefaultDatabase();
	
	public DB getDatabase(String name);
	
}
