package com.zyeeda.framework.ws.base;

import javax.servlet.ServletContext;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;

import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.ftp.FtpService;
import com.zyeeda.framework.knowledge.KnowledgeService;
import com.zyeeda.framework.ldap.LdapService;
import com.zyeeda.framework.managers.UserManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.managers.internal.DefaultUserManager;
import com.zyeeda.framework.nosql.MongoDbService;
import com.zyeeda.framework.persistence.PersistenceService;
import com.zyeeda.framework.scheduler.SchedulerService;
import com.zyeeda.framework.security.SecurityService;
import com.zyeeda.framework.sync.UserSyncService;

@Deprecated
public class ResourceService {

	protected ServletContext ctx;
	
    private SecurityService<?> securityService = null;
    private LdapService ldapService = null;
    private UserSyncService userSyncService = null;
    private KnowledgeService knowledgeService = null;
    private MongoDbService mongoDbService = null;
    private FtpService ftpService = null;
    
    protected ServletContext getServletContext() {
        return this.ctx;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.ctx = servletContext;
    }

    @Autowired
    public void setSecurityService(SecurityService<?> securityService) {
        this.securityService = securityService;
    }

    @Autowired
    public void setLdapService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @Autowired
    public void setUserSyncService(UserSyncService userSyncService) {
        this.userSyncService = userSyncService;
    }

    @Autowired
    public void setKnowledgeService(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Autowired
    public void setMongoDbService(MongoDbService mongoDbService) {
        this.mongoDbService = mongoDbService;
    }

    @Autowired
    public void setFtpService(FtpService ftpService) {
        this.ftpService = ftpService;
    }

    public UserSyncService getUserSyncService() {
        return userSyncService;
    }
    
    private PersistenceService persistenceService;

    protected PersistenceService getPersistenceService() {
        return persistenceService;
    }
    
    @Autowired
    public void sePersistenceService(PersistenceService persistenceService) {
    	this.persistenceService = persistenceService;
    }

    protected SecurityService<?> getSecurityService() {
        return securityService;
    }

    protected LdapService getLdapService() {
        return ldapService;
    }

    protected UserSyncService getUserSynchService() {
        return userSyncService;
    }

    protected KnowledgeService getKnowledgeService(){
        return knowledgeService;
    }

    protected MongoDbService getMongoDbService() {
        return mongoDbService;
    }
    
    protected FtpService getFtpService() {
        return ftpService;
    }
    
    protected SchedulerService<?> getSchedulerService() {
        return null;
    }

    protected SecurityService<?> getVirtualSchedulerService() {
        return null;
    }

    public String getChinaName() throws UserPersistException {
    	String userId = this.getSecurityService().getCurrentUser();
		UserManager userMgr = new DefaultUserManager(this.getPersistenceService());
		Session session = SecurityUtils.getSubject().getSession();
		User userInfo = null;
		if(session.getAttribute("userInfo") == null) {
			userInfo = userMgr.findById(userId);
			 session.setAttribute("userInfo", userInfo);
		} else {
			userInfo = (User) session.getAttribute("userInfo");
		}
		if(userInfo == null) {
			return null;
		}
		String userName = userInfo.getUsername();
		if(userName == null) {
			return "admin";
		}
		return userName;
    }
    protected String getChinaName(String userId) throws UserPersistException {
		UserManager userMgr = new DefaultUserManager(this.getPersistenceService());
		User user = userMgr.findById(userId);
		if(user == null) {
			return "";
		}
		String userName = user.getUsername();
		return userName;
    }
    
    public  String getDeptByUser() throws UserPersistException {
    	return this.getDeptByUser(null);
    }
    
    
    /**
     *  得到二级部门
     * @param userName
     * @return
     * @throws UserPersistException
     */
    public  String getDeptByUser(String userName) throws UserPersistException {
		//String currentUser = null;
		if(userName == null) {
			userName = this.getSecurityService().getCurrentUser();
		}
//		Session session = SecurityUtils.getSubject().getSession();
		User userInfo = null;
//		if(session.getAttribute("userInfo") == null) {
			UserManager userMgr = new DefaultUserManager(this.getPersistenceService());
			userInfo = userMgr.findById(userName);
//			 session.setAttribute("userInfo", userInfo);
//		} else {
//			userInfo = (User) session.getAttribute("userInfo");
//		}
		if(userInfo == null) {
			return null;
		}
		String deptFullPath = userInfo.getDeptFullPath();
		if(deptFullPath == null) {
			return null;
		}
		String[] deptFullpaths = deptFullPath.split(",");
		int deptLengthNo = 0;
		if(deptFullpaths.length <= 2) {
			deptLengthNo = deptFullpaths.length-1;
		} else {
			deptLengthNo = deptFullpaths.length-2;
		}
		String detp = deptFullpaths[deptLengthNo];
		int deptLength = detp.indexOf("=");
		String deptName = detp.substring(deptLength + 1, detp.length());
		return deptName;
	}
    
    public  String getDeptByUserName() throws UserPersistException{
    	return this.getDeptByUserName(null);
    }
    
    /**
     *得到父级部门
     * @param userName
     * @return
     * @throws UserPersistException
     */
    public  String getDeptByUserName(String userName) throws UserPersistException {
		//String currentUser = null;
		if(userName == null) {
			userName = this.getSecurityService().getCurrentUser();
		}
//		Session session = SecurityUtils.getSubject().getSession();
		User userInfo = null;
//		if(session.getAttribute("userInfo") == null) {
			UserManager userMgr = new DefaultUserManager(this.getPersistenceService());
			userInfo = userMgr.findById(userName);
//			 session.setAttribute("userInfo", userInfo);
//		} else {
//			userInfo = (User) session.getAttribute("userInfo");
//		}
		if(userInfo == null) {
			return null;
		}
		String deptFullPath = userInfo.getDeptFullPath();
		if(deptFullPath == null) {
			return null;
		}
		String[] deptFullpaths = deptFullPath.split(",");
		String detp = deptFullpaths[1];
		if(detp.contains("uid=")) {
			detp = deptFullpaths[0];
		}
		int deptLength = detp.indexOf("=");
		String deptName = detp.substring(deptLength + 1, detp.length());
		return deptName;
	}
    
    protected void copyServicesTo(ResourceService another) {
    	another.setFtpService(ftpService);
    	another.setKnowledgeService(knowledgeService);
    	another.setLdapService(ldapService);
    	another.setMongoDbService(mongoDbService);
    	another.setSecurityService(securityService);
    	another.ctx = ctx;
    	another.setUserSyncService(userSyncService);
    }
}
