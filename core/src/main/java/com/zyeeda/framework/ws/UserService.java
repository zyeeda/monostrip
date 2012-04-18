package com.zyeeda.framework.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zyeeda.framework.account.AccountService;
import com.zyeeda.framework.entities.Account;
import com.zyeeda.framework.entities.Department;
import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.helpers.AccountHelper;
import com.zyeeda.framework.ldap.LdapService;
import com.zyeeda.framework.ldap.LdapTemplate;
import com.zyeeda.framework.ldap.SearchControlsFactory;
import com.zyeeda.framework.managers.AccountManager;
import com.zyeeda.framework.managers.DepartmentManager;
import com.zyeeda.framework.managers.RoleManager;
import com.zyeeda.framework.managers.UserManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.managers.internal.DefaultRoleManager;
import com.zyeeda.framework.managers.internal.DefaultUserManager;
import com.zyeeda.framework.managers.internal.LdapDepartmentManager;
import com.zyeeda.framework.managers.internal.LdapUserManager;
import com.zyeeda.framework.managers.internal.SystemAccountManager;
import com.zyeeda.framework.sync.UserSyncService;
import com.zyeeda.framework.utils.LdapEncryptUtils;
import com.zyeeda.framework.viewmodels.AccountVo;
import com.zyeeda.framework.viewmodels.UserVo;
import com.zyeeda.framework.ws.base.ResourceService;

@Path("/users")
public class UserService extends ResourceService {
	
	private Logger logger = LoggerFactory.getLogger(UserService.class);
	
	private AccountService accountService = null;
    
    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

	private static String createUserDn(String parent, String id) {
		return "uid=" + id + "," + parent;
	}
	
	public String getChNameById(String id) throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		SearchControls sc = SearchControlsFactory.getSearchControls(
										SearchControls.SUBTREE_SCOPE);
		List<User> userList = userMgr.findByName(id, sc);
		if (userList != null && userList.size() > 0) {
			return userList.get(0).getUsername();
		} else {
			return "";
		}
	}
	
	@POST
	@Path("/{parent:.*}")
	@Produces("application/json")
	public User persist(@FormParam("") User user, @PathParam("parent") String parent)
				   											throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		UserSyncService userSyncService = this.getUserSynchService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		SearchControls sc = SearchControlsFactory.getSearchControls(
										SearchControls.SUBTREE_SCOPE);
		List<User> userList = userMgr.findByName(user.getId(), sc);
		if (userList != null && userList.size() > 0) {
			throw new RuntimeException("账号不能重复");
		} else {
			user.setDepartmentName(parent);
			user.setDeptFullPath(createUserDn(parent, user.getId()));
			userMgr.persist(user);
			user = userMgr.findById(user.getDeptFullPath());
			userSyncService.persist(user);
			return user;
		}
	}
	
	@DELETE
	@Path("/{id}")
	public String remove(@PathParam("id") String id,
						 @PathParam("cascade") Boolean cascade)
					throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		UserSyncService userSyncService = this.getUserSynchService();
		if (cascade != null) {
			userMgr.remove(id);
			userSyncService.remove(id);
			return "{success: 'true'}";
		} else {
			Integer count = userMgr.getChildrenCountById(id, "(objectclass=*)");
			if (count > 0) {
				return "{\"success\": \"false\"}";
			} else {
				userMgr.remove(id);
				userSyncService.remove(id);
				return "{\"success\": \"true\"}";
			}
		}
	}
	
	@PUT
	@Path("/{id}")
	@Produces("application/json")
	public User update(@FormParam("") User user,
					   @PathParam("id") String id,
					   @FormParam("selectedDeptFullPath") String selectedDeptFullPath)
				throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		UserSyncService userSyncService = this.getUserSynchService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		
		String uid = id.substring(id.indexOf("=") + 1, id.indexOf(","));
		if (!uid.equals(user.getId())) {
			throw new RuntimeException("不能修改账号");
		} else {
			user.setSelectedDeptFullPath(id);
			String newName = "uid=" + uid + "," + selectedDeptFullPath;
			user.setDeptFullPath(newName);
			userMgr.update(user);
			User u = userMgr.findById(newName);
			if (u != null) {
				logger.info("user has been found, this user pwd id is {}", u.getPassword());
				user.setDepartmentName(selectedDeptFullPath);
				user.setPassword(u.getPassword());
				userSyncService.update(user);
			} else {
				logger.info("user has not been found, this user pwd id = {}", user.getPassword());
				user.setDepartmentName(selectedDeptFullPath);
				userSyncService.persist(user);
			}
			user = userMgr.findById(newName);
			return user;
		}
	}
	
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public User findById(@PathParam("id") String id) throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		User user = userMgr.findById(id);
		user.setDepartmentName(LdapTemplate.spiltNameInNamespace(id));
		user.setDeptFullPath(id);
		return user;
	}
	
	@GET
	@Path("/search/{name}")
	@Produces("application/json")
	public List<UserVo> getUserListByName(@PathParam("name") String name) throws UserPersistException {
		logger.debug("进入 search1 {}",name);
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		
		return UserService.fillUserListPropertiesToVo(userMgr.findByName(name));
	}

	
	@GET
	@Path("/synUser")
	@Produces("application/json")
	public Map<String, Object> SynUser() throws UserPersistException {
		Map<String, Object> map = new HashMap<String, Object>();
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		SearchControls sc = SearchControlsFactory.getSearchControls(
				SearchControls.SUBTREE_SCOPE);
		List<User> userList = userMgr.findByDepartmentId("o=广州局", sc);
		if(userList != null && userList.size() > 0) {
			map.put("ResponseCode", "000000");
			map.put("ResponseDesc", "成功");
			map.put("datas", userList);
		}else {
			map.put("ResponseCode", "000001");
			map.put("ResponseDesc", "没有数据可同步");
			map.put("datas", "");
		}
		return map;
	}
	
	@GET
	@Path("/{name}/basicinfo")
	@Produces("application/json")
	public Map<String, Object> getUserInfo(@PathParam("name") String name) throws UserPersistException {
		Map<String, Object> map = new HashMap<String, Object>();
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		SearchControls sc = SearchControlsFactory.getSearchControls(
				SearchControls.SUBTREE_SCOPE);
		List<User> userList = userMgr.findByName(name, sc);
		if(userList != null && userList.size() > 0) {
			map.put("ResponseCode", "000000");
			map.put("ResponseDesc", "成功");
			map.put("datas", userList.get(0));
		}else {
			map.put("ResponseCode", "000001");
			map.put("ResponseDesc", "没有此用户");
			map.put("datas", "");
		}
		return map;
	}
	
	
	@POST
	@Path("/check")
	@Produces("application/json")
	public Map<String, Object> checkLog(@FormParam("username") String name, @FormParam("password") String password) throws UserPersistException, NoSuchAlgorithmException {
		Map<String, Object> map = new HashMap<String, Object>();
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		SearchControls sc = SearchControlsFactory.getSearchControls(
				SearchControls.SUBTREE_SCOPE);
		List<User> userList = userMgr.findByName(name, sc);
		if(userList != null && userList.size() > 0) {
			if(LdapEncryptUtils.verifySHA(userList.get(0).getPassword(), DigestUtils.md5Hex(password).toString())) {
				map.put("ResponseCode", "000000");
				map.put("ResponseDesc", "成功");
				map.put("datas", userList.get(0));
			}else {
				map.put("ResponseCode", "000002");
				map.put("ResponseDesc", "密码错误");
				map.put("datas", "");
			}
			return map;
		}else {
			map.put("ResponseCode", "000001");
			map.put("ResponseDesc", "没有此用户");
			map.put("datas", "");
		}
		return map;
	}
	
	@GET
	@Path("/search")
	@Produces("application/json")
	public String search(@FormParam("name") String name) throws UserPersistException {
	
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		List<User> userList = userMgr.search(name);
		logger.debug("模糊搜索获得的所有数据 = {}",userList.size());
		
		StringBuffer buffer = new StringBuffer("{");
		buffer.append("\"totalRecords\":").append(userList.size())
	      	  .append(",").append("\"startIndex\":").append(0)
	      	  .append(",").append("\"pageSize\":").append(13)
	      	  .append(",").append("\"records\":[");
		for (User user : userList) {
			buffer.append("{\"id\":").append("\"").append(user.getId()).append("\"").append(",")
			      .append("\"username\":").append("\"").append(user.getUsername()).append("\"").append(",")
			      .append("\"mobile\":").append("\"").append(user.getMobile() == null ? "" : user.getMobile()).append("\"").append(",")
			      .append("\"email\":").append("\"").append(user.getEmail() == null ? "" : user.getEmail()).append("\"").append(",")
			      .append("\"status\":").append("\"").append(user.getStatus() == null ? "" : user.getStatus()).append("\"").append(",")
			      .append("\"parent\":").append("\"").append(user.getDepartmentName() == null ? "" : user.getDepartmentName()).append("\"").append(",")
			      .append("\"fullpath\":").append("\"").append(user.getDeptFullPath() == null ? "" : user.getDeptFullPath()).append("\"").append("},");
		}
		if (buffer.lastIndexOf(",") != -1 && userList.size() > 0) {
			buffer.deleteCharAt(buffer.lastIndexOf(","));
		}
		buffer.append("]}");
		
		return buffer.toString();
	}
	
	@GET
	@Path("/userList/{deptId}")
	@Produces("application/json")
	public List<UserVo> getUserListByDepartmentId(@PathParam("deptId") String roleName)
														throws UserPersistException {
		RoleManager roleMgr = new DefaultRoleManager(this.getPersistenceService());
		 List<UserVo> userVo = roleMgr.findSubjectByLikeRoleName(roleName);
		return userVo;
	}
	
	@PUT
	@Path("/{id}/update_password")
	@Produces("application/json")
	public User updatePassword(@PathParam("id") String id,
							   @FormParam("oldPassword") String oldPassword,
							   @FormParam("newPassword") String newPassword)
						  throws UserPersistException,
						  		 NoSuchAlgorithmException, NamingException, IOException {
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		
		User u = userMgr.findById(id);
		String inputPw = oldPassword;
		try {
			ldapSvc.getLdapContext(u.getId(), inputPw);
			userMgr.updatePassword(id, newPassword);
		} catch (Exception e) {
			throw new RuntimeException("旧密码输入错误");
		}
		return userMgr.findById(id);
	}
	
	@PUT
	@Path("/{id}/update_assign_password")
	@Produces("application/json")
	public User updateAssignPassword(@PathParam("id") String id,
							   @FormParam("oldPassword") String oldPassword,
							   @FormParam("newPassword") String newPassword)
						  throws UserPersistException,
						  		 NoSuchAlgorithmException, NamingException, IOException {
		LdapService ldapSvc = this.getLdapService();
		UserSyncService userSyncService = this.getUserSynchService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		
		User u = userMgr.findById(id);
		String inputPw = oldPassword;
		String oldAssPw = u.getAssignPassword();
		if ( StringUtils.isNotBlank(oldAssPw) && !inputPw.equals(oldAssPw)) {
			throw new RuntimeException("旧签名密码输入错误");
		} else {
			userMgr.updateAssignPassword(id, newPassword);
			userSyncService.updateAssignPassword(id, newPassword);
		}
		
		return userMgr.findById(id);
	}
	
	@PUT
	@Path("/{id}/enable")
	@Produces("application/json")
	public User enable(@PathParam("id") String id, @FormParam("status") Boolean visible)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		UserSyncService userSyncService = this.getUserSynchService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		userMgr.enable(id);
		userSyncService.enable(id);
		return userMgr.findById(id);
	}
	
	@PUT
	@Path("/{id}/unenable")
	@Produces("application/json")
	public User disable(@PathParam("id") String id,
						@FormParam("status") Boolean visible)
	 		       throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		UserSyncService userSyncService = this.getUserSynchService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		
		userMgr.disable(id);
		userSyncService.disable(id);
		return userMgr.findById(id);
	}
	
	@POST
	@Path("/{id}")
	@Produces("application/json")
	public void uploadPhoto(@Context HttpServletRequest request,
							@PathParam("id") String id)
					   throws Throwable {
		InputStream in = request.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	    byte[] b = new byte[1024];  
	    int len = 0;  
	  
	    while ((len = in.read(b, 0, 1024)) != -1) {  
	        baos.write(b, 0, len);  
	    }  
	    baos.flush();
	  
//	    byte[] bytes = baos.toByteArray();
	    LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		User user = new User();
		user.setId("china");
//		user.setPhoto(bytes);
		
		userMgr.update(user);
	}
	
	@GET
	@Path("/current_user_in_dept_all_user")
	@Produces("application/json")
	public List<UserVo> getCurrentUserInDepartmentAllUser() throws UserPersistException {
		String currentUser = this.getSecurityService().getCurrentUser();
		LdapService ldapSvc = this.getLdapService();
		UserManager userManager = new LdapUserManager(ldapSvc);
		SearchControls sc = SearchControlsFactory.getSearchControls(
										SearchControls.SUBTREE_SCOPE);
		List<User> users = userManager.findByName(currentUser, sc);
		User user = null;
		if (users != null && users.size() > 0) {
			user = users.get(0);
			if (user != null && StringUtils.isNotBlank(user.getDeptFullPath())) {
				String secondDept = user.getDeptFullPath();
				secondDept = secondDept.substring(secondDept.indexOf(",") + 1,
						secondDept.length());
				String[] spilt = StringUtils.split(secondDept);
				if (spilt.length >= 2) {
					secondDept = spilt[spilt.length - 2] + ","
							+ spilt[spilt.length - 1];
				}
				users = userManager.findByDepartmentId(secondDept, sc);
			}

		}
		List<UserVo> listUser = fillUserListPropertiesToVo(users, "task");
		return listUser;
	}
	
	public static UserVo fillUserPropertiesToVo(User user) {
		UserVo userVo = new UserVo();
		userVo.setId(user.getId());
		userVo.setType("io");
		userVo.setLabel(user.getUsername());
		userVo.setCheckName(user.getUsername());
		userVo.setLeaf(true);
		userVo.setUid(user.getId());
		userVo.setDeptFullPath(user.getDeptFullPath());
		userVo.setKind("user");
		return userVo;
	}
	public static UserVo fillUserPropertiesToVoWk(User user) {
		UserVo userVo = new UserVo();
		userVo.setId(user.getId());
		userVo.setType("task");
		userVo.setLabel(user.getUsername());
		userVo.setCheckName(user.getUsername());
		userVo.setLeaf(true);
		userVo.setUid(user.getId());
		userVo.setDeptFullPath(user.getDeptFullPath());
		userVo.setKind("user");
		return userVo;
	}
	public static UserVo fillUserPropertiesToVo(User user, String type) {
		UserVo userVo = new UserVo();

		userVo.setId(user.getId());
		userVo.setType(type);
		userVo.setLabel( user.getUsername());
		userVo.setCheckName(user.getUsername());
		userVo.setLeaf(true);
		userVo.setUid(user.getId());
		userVo.setDeptFullPath(user.getDeptFullPath());
		userVo.setKind("user");

		return userVo;
	}

	public static List<UserVo> fillUserListPropertiesToVo(List<User> userList) {
		List<UserVo> userVoList = new ArrayList<UserVo>(userList.size());
		UserVo userVo = null;
		for (User user : userList) {
			userVo = UserService.fillUserPropertiesToVo(user);
			userVoList.add(userVo);
		}
		return userVoList;
	}
	public static List<UserVo> fillUserListPropertiesToVoWk(List<User> userList) {
		List<UserVo> userVoList = new ArrayList<UserVo>(userList.size());
		UserVo userVo = null;
		for (User user : userList) {
			userVo = UserService.fillUserPropertiesToVoWk(user);
			userVoList.add(userVo);
		}
		return userVoList;
	}
	public static List<UserVo> fillUserListPropertiesToVo(List<User> userList, String type) {
		List<UserVo> userVoList = new ArrayList<UserVo>(userList.size());
		UserVo userVo = null;
		for (User user : userList) {
			userVo = UserService.fillUserPropertiesToVo(user, type);
			userVoList.add(userVo);
		}
		return userVoList;
	}
	
	/**
	 * 配置系统信息
	 * 将旧的数据删除，保存新的数据。
	 * Json list
	 * return  userList
	 */
	@POST
	@Path("/accounts/{id}")
	@Produces("application/json")
	public List<Account> updateAccounts(@FormParam("userList") String userListJson, @PathParam("id") String id) throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		AccountManager objAccountManager = new SystemAccountManager(ldapSvc);
		ObjectMapper mapper = new ObjectMapper();
		List<Account> userList = null;
		try {
			userList = mapper.readValue(userListJson,
					new TypeReference<List<Account>>() {
					});
			List<Account> tempAccountList = new ArrayList<Account>();
			LdapContext ctx  = ldapSvc.getLdapContext();
			NamingEnumeration<SearchResult> ns = ctx.search(id, "objectclass=*", SearchControlsFactory.getDefaultSearchControls());
			while (ns.hasMore()) {
				Account  ac = AccountHelper.convertAttributesToAccount(ns.next().getAttributes());
				tempAccountList.add(ac);
			}
			for (Account account : tempAccountList) {
				account.setUserFullPath(id);
				objAccountManager.remove("username=" + account.getUserName() + "," + id);
			}
			DefaultUserManager dum = new DefaultUserManager(this.getPersistenceService());
			User user = dum.findById(id);
			if( user != null ){
				for (Account account : userList) {
					account.setUserFullPath(user.getDeptFullPath());
					objAccountManager.update(account);
				}
			}
//			logger.debug("UserList size is {}", userList.size());
		} catch (JsonParseException e) {
			logger.error(e.getMessage(), e);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
		}
		return userList;
	}
//	objAccountManager.remove(account.getSystemName());
//	Account newAccount = objAccountManager.findByUserIdAndSystemName(id, account.getSystemName());
//	newAccount.setUserName(account.getUserName());
//	newAccount.setPassword(account.getPassword());
//	newAccount.setVisible(account.getVisible());
//	objAccountManager.update(newAccount);
//	account.setUserFullPath(id);
//	objAccountManager.remove(account.getSystemName());//.findByUserIdAndSystemName(id, account.getSystemName());
////	newAccount.setUserName(account.getUserName());
////	newAccount.setPassword(account.getPassword());
////	newAccount.setVisible(account.getVisible());
////	newAccount.setUserFullPath(id);

	/**
	 * get account list
	 */
	@GET
	@Path("/accounts/{id}")
	@Produces("application/json")
	public AccountVo getAccounts(@PathParam("id") String id) throws UserPersistException{
		LdapService ldapSvc = this.getLdapService();
		AccountManager objAccountManager = new SystemAccountManager(ldapSvc);
		DefaultUserManager dum = new DefaultUserManager(this.getPersistenceService());
		User user = dum.findById(id);
		if( user != null ){
			List<Account> list = objAccountManager.findByUserId(user.getDeptFullPath());
			AccountVo avo = new AccountVo();
			avo.setAccounts(list);
			return avo;
		}
		return null;
	}
	
//	public void removeSysConfigure(String systemName) throws UserPersistException{
//		LdapService ldapSvc = this.getLdapService();
//		AccountManager objAccountManager = new SystemAccountManager(ldapSvc);
//		objAccountManager.remove(systemName);
//	}
	
	/*
	@POST
	@Path("/accounts/{id}")
	@Produces("application/json")
	public Account updateAccounts(@FormParam("") Account objAccount, @PathParam("id") String id) throws UserPersistException{
		LdapService ldapSvc = this.getLdapService();
		AccountManager objAccountManager = new SystemAccountManager(ldapSvc);

		if(objAccount == null){
			throw new RuntimeException("用户名或密码为空");
		} else {
			objAccount.setUserFullPath("username=" + objAccount.getUserName() + "," + id);
			objAccountManager.update(objAccount);
			return objAccount;
		}
	}
	*/
//	
	@GET
	@Path("/systemUsers/{uid}/{systemName}")
	@Produces("application/json")
	public Map<String, Object> mockSignIn(@PathParam("uid") String uid,@PathParam("systemName") String systemName) throws UserPersistException{
		LdapService ldapSvc = this.getLdapService();
		AccountManager objAccountManager = new SystemAccountManager(ldapSvc);
		
		Collection<ServletContext> contexts = new ArrayList<ServletContext>();
		contexts.add(this.getServletContext());
		
		Map<String,Object> result = new HashMap<String, Object>();
		DefaultUserManager dum = new DefaultUserManager(this.getPersistenceService());
		User user = dum.findById(uid);
		if( user != null ){
			result.put("account", objAccountManager.findByUserIdAndSystemName(user.getDeptFullPath(), systemName));
		}
		result.put("url", accountService.getMockSignInConfig(systemName));
		return result;
	}
	
	@GET
	@Path("/tests/{uid}/{systemName}")
	public String testMethod(@PathParam("uid") String uid,@PathParam("systemName") String systemName) {

		return "uid = "+uid + " systemName = "+systemName;
	}
	
	@GET
	@Path("/userlist_for_defect_by_current_site_user_send_sms/{deptName}")
	@Produces("application/json")
	public List<User> getUserListByDepartmentName(@PathParam("deptName") String deptName)
															throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		DepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		
		SearchControls sc = SearchControlsFactory.getSearchControls(SearchControls.SUBTREE_SCOPE);
		List<Department> deptList = deptMgr.findByName(deptName);
		Department dept = null;
		if (deptList != null && deptList.size() > 0) {
			dept = deptList.get(0);
		}
		List<User> userList = userMgr.findByDepartmentId(dept.getDeptFullPath(), sc);
		return userList;
	}
	
	@GET
	@Path("/userlist_by_dept_task/{deptName}")
	@Produces("application/json")
	public List<UserVo> getUserByDeptTask(@PathParam("deptName") String deptName) throws UserPersistException{
		LdapService ldapSvc = this.getLdapService();
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		DepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		
		SearchControls sc = SearchControlsFactory.getSearchControls(SearchControls.SUBTREE_SCOPE);
		List<Department> deptList = deptMgr.findByName(deptName);
		Department dept = null;
		if (deptList != null && deptList.size() > 0) {
			dept = deptList.get(0);
		}
		UserVo userVo = null;
		List<User> userList = userMgr.findByDepartmentId(dept.getDeptFullPath(), sc);
		List<UserVo> userVoList = new ArrayList<UserVo>(userList.size());
		for (User user : userList) {
			userVo = UserService.fillUserPropertiesToVoDefect(user);
			userVoList.add(userVo);
		}
		return userVoList;
	}
	
	@GET
	@Path("/current_all_user_defect")
	@Produces("application/json")
	public List<UserVo> getCurrentUserInDepartmentAllUserDefect() throws UserPersistException {
		String currentUser = this.getSecurityService().getCurrentUser();
		LdapService ldapSvc = this.getLdapService();
		UserManager userManager = new LdapUserManager(ldapSvc);
		SearchControls sc = SearchControlsFactory.getSearchControls(
										SearchControls.SUBTREE_SCOPE);
		List<User> users = userManager.findByName(currentUser, sc);
		User user = null;
		if (users != null && users.size() > 0) {
			user = users.get(0);
			if (user != null && StringUtils.isNotBlank(user.getDeptFullPath())) {
				String secondDept = user.getDeptFullPath();
				secondDept = secondDept.substring(secondDept.indexOf(",") + 1,
						secondDept.length());
				String[] spilt = StringUtils.split(secondDept);
				if (spilt.length >= 2) {
					secondDept = spilt[spilt.length - 2] + ","
							+ spilt[spilt.length - 1];
				}
				users = userManager.findByDepartmentId(secondDept, sc);
			}

		}
		List<UserVo> listUser = fillUserListPropertiesToVoDefect(users);
		return listUser;
	}
	
	public static List<UserVo> fillUserListPropertiesToVoDefect(List<User> userList) {
		List<UserVo> userVoList = new ArrayList<UserVo>(userList.size());
		UserVo userVo = null;
		for (User user : userList) {
			userVo = UserService.fillUserPropertiesToVo(user);
			userVoList.add(userVo);
		}
		return userVoList;
	}
	
	public static UserVo fillUserPropertiesToVoDefect(User user) {
		UserVo userVo = new UserVo();
		userVo.setId(user.getId());
		userVo.setType("task");
		userVo.setLabel(user.getId() );
		userVo.setCheckName(user.getId());
		userVo.setLeaf(true);
		userVo.setUid(user.getId());
		userVo.setDeptFullPath(user.getDeptFullPath());
		userVo.setKind("user");
		return userVo;
	}
	
	
	
}
