package com.zyeeda.framework.nosql.internal;

import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.Mongo;
import com.zyeeda.framework.nosql.MongoDbService;
import com.zyeeda.framework.service.AbstractService;

public class DefaultMongoDbServiceProvider extends AbstractService implements
		MongoDbService {
    private String host = null;
    private int port = -1;
    private String systemUsername = null;
    private String systemPassword = null;
    private String defaultDatabaseName = null;
    private Mongo mongo = null;
    
    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSystemUsername(String systemUsername) {
        this.systemUsername = systemUsername;
    }

    public void setSystemPassword(String systemPassword) {
        this.systemPassword = systemPassword;
    }

    public void setDefaultDatabaseName(String defaultDatabaseName) {
        this.defaultDatabaseName = defaultDatabaseName;
    }

    @Override
    public void start() throws Exception {
        if( port < 0 ) port = DBAddress.defaultPort();
        
        this.mongo = new Mongo(this.host, this.port);
        DB adminDb = this.mongo.getDB("admin");
        if (this.systemUsername != null && systemPassword != null) {
            adminDb.authenticate(this.systemUsername, systemPassword.toCharArray());
        }
    }
    
	@Override
	public void stop() {
		this.mongo.close();
	}

	@Override
	public Mongo getMongo() {
		return this.mongo;
	}

	@Override
	public DB getDefaultDatabase() {
		return this.getDatabase(this.defaultDatabaseName);
	}

	@Override
	public DB getDatabase(String name) {
		return this.mongo.getDB(name);
	}

}
