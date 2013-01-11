package com.zyeeda.coala.commons.generator;

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.UUIDHexGenerator;
import org.hibernate.type.Type;

public class FallbackUUIDHexGenerator extends UUIDHexGenerator {
	
    private String entityName;

    @Override
    public void configure(Type type, Properties params, Dialect d)
            throws MappingException {
        entityName = params.getProperty(ENTITY_NAME);
        super.configure(type, params, d);
    }

    @Override
    public Serializable generate(SessionImplementor session, Object object)
            throws HibernateException {            
        Serializable id = session.getEntityPersister(entityName, object)
        		.getIdentifier(object, session);       
        if (id == null) {
            return super.generate(session, object);
        }
        return id;
    }
}