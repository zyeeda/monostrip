package com.zyeeda.framework.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.mozilla.javascript.Context;
import org.ringojs.engine.RhinoEngine;
import org.ringojs.engine.RingoConfiguration;
import org.ringojs.repository.FileRepository;
import org.ringojs.repository.Repository;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * @author guyong
 *
 */
@ContextConfiguration("classpath:spring-test-context.xml")
public abstract class AbstractTestNGRingoSupportTests extends AbstractTestNGSpringContextTests {

    private RhinoEngine engine = null;
    
    protected String getDataSourceJndiName() {
        return "jdbc/defaultDS";
    }
    
    protected List<String> getRingoModules() {
        List<String> userModules = new ArrayList<String>();
        userModules.add("javascript");
        userModules.add("coffee");
        
        return userModules;
    }
    
    protected List<String> getRingoSystemModules() {
        List<String> systemModules = new ArrayList<String>();
        systemModules.add("modules");
        systemModules.add("packages");
        
        return systemModules;
    }
    
    protected boolean isDebugEnabled() {
        return false;
    }
    
    protected void configRingo(RingoConfiguration config) {
        config.setDebug(isDebugEnabled());
        config.setReloading(false);
    }
    
    private ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null ) {
            cl = getClass().getClassLoader();
        }
        return cl;
    }
    
    protected void initJNDIBindings() throws Exception {
        PoolingDataSource pds = new PoolingDataSource();
        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        pds.setUniqueName("defaultDS");
        pds.setAutomaticEnlistingEnabled(true);
        pds.setAllowLocalTransactions(true);
        pds.setMaxPoolSize(5);
        Properties p = new Properties();
        p.put("driverClassName", "org.h2.Driver");
        p.put("url", "jdbc:h2:testDb");
        pds.setDriverProperties(p);
        
        pds.init();
        
        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        Object tm = TransactionManagerServices.getTransactionManager();
        builder.bind(getDataSourceJndiName(), pds);
        builder.bind("java:comp/TransactionManager", tm);
        builder.bind("java:comp/UserTransaction", tm);
        builder.bind("java:comp/TransactionSynchronizationRegistry", TransactionManagerServices.getTransactionSynchronizationRegistry());
        builder.activate();
    }
    
    protected void initRingoEngine() throws Exception {
        File testClassPathRoot = new File(getClassLoader().getResource(".").toURI());
        File classPathRoot = new File(testClassPathRoot, "../classes");
        Repository home = new FileRepository(classPathRoot);
        Repository base = new FileRepository(testClassPathRoot);
        
        RingoConfiguration rc = new RingoConfiguration(home, base, getRingoModules(), getRingoSystemModules());
        configRingo(rc);
        engine = new RhinoEngine(rc, null);
    }
    
    @BeforeSuite
    public void prepareTesting() throws Exception {
        initJNDIBindings();
        initRingoEngine();
    }
    
    @Test
    public void startRingoTests() throws Exception {
        Context cx = engine.getContextFactory().enterContext();
        try {
            List<String> modules  = getRingoModules();
            engine.invoke("coala/test/test-runner", "run", cx, engine, modules.toArray());
        } finally {
            Context.exit();
        }
    }
}
