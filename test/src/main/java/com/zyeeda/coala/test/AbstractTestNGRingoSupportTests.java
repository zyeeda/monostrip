package com.zyeeda.coala.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.mozilla.javascript.Context;
import org.ringojs.engine.RhinoEngine;
import org.ringojs.engine.RingoConfiguration;
import org.ringojs.repository.FileRepository;
import org.ringojs.repository.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
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
@ContextConfiguration("classpath:spring/coala/test-application-context.xml")
public abstract class AbstractTestNGRingoSupportTests extends AbstractTestNGSpringContextTests {

    private RhinoEngine engine = null;
    private JdbcTemplate jdbc = null;
    
    protected String getDataSourceJndiName() {
        return "jdbc/defaultDS";
    }
    
    protected List<String> getRingoModules() {
        List<String> userModules = new ArrayList<String>();
        userModules.add("tests");
        
        return userModules;
    }
    
    protected List<String> getRingoSystemModules() {
        List<String> systemModules = new ArrayList<String>();
        systemModules.add("modules");
        systemModules.add("packages");
        systemModules.add("coala-test-modules");
        return systemModules;
    }
    
    protected boolean isDebugEnabled() {
        return false;
    }
    
    protected void configRingo(RingoConfiguration config) {
        config.setDebug(isDebugEnabled());
        config.setReloading(false);
        try {
            boolean removed = false;
            for (Iterator<Repository> it = config.getRepositories().iterator(); it.hasNext(); ) {
                Repository repo = it.next();
                if (!repo.exists() && "coala-test-modules".equals(repo.getName())) {
                    removed = true;
                    it.remove();
                }
            }
            if (removed) {
                config.addModuleRepository(config.resolveRepository(getClassLoader().getResource("coala-test-modules").getFile(), false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null ) {
            cl = getClass().getClassLoader();
        }
        System.out.println("ClassLoader Used:" + cl);
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
        
        jdbc = new JdbcTemplate(pds);
        
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
        System.out.println(testClassPathRoot + "testClassPathRoot");
        System.out.println(classPathRoot + "classpathroot");
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
            engine.invoke("coala/test/test-runner", "run", cx, engine, modules.toArray(), jdbc);
        } finally {
            Context.exit();
        }
    }
}
