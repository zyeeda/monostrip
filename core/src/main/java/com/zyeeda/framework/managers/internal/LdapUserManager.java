package com.zyeeda.framework.managers.internal;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;

import com.googlecode.genericdao.search.ExampleOptions;
import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.ISearch;
import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.ldap.LdapService;
import com.zyeeda.framework.ldap.LdapTemplate;
import com.zyeeda.framework.ldap.SearchControlsFactory;
import com.zyeeda.framework.managers.UserManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.utils.DatetimeUtils;

public class LdapUserManager implements UserManager {

	private LdapService ldapSvc;

	public LdapUserManager(LdapService ldapSvc) {
		this.ldapSvc = ldapSvc;
	}

	@Override
	public void persist(User user) throws UserPersistException {
		LdapTemplate ldapTemplate = null;
		try {
			ldapTemplate = this.getLdapTemplate();
			Attributes attrs = LdapUserManager.unmarshal(user);
			ldapTemplate.bind(user.getDeptFullPath(), attrs);
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} catch (UnsupportedEncodingException e) {
			throw new UserPersistException(e);
		} finally {
			if (ldapTemplate != null) {
				ldapTemplate.closeLdapContext();
			}
		}
	}

	@Override
	public void remove(String id) throws UserPersistException {
		LdapTemplate ldapTemplate = null;
		try {
			ldapTemplate = this.getLdapTemplate();
			ldapTemplate.unbind(id, true);
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} finally {
			if (ldapTemplate != null) {
				ldapTemplate.closeLdapContext();
			}
		}
	}

	@Override
	public void update(User user) throws UserPersistException {
		LdapTemplate ldapTemplate = null;
		try {
			String dn = user.getSelectedDeptFullPath();
			Attributes attrs = LdapUserManager.unmarshal(user);
			ldapTemplate = this.getLdapTemplate();
			if (!dn.equals(user.getDeptFullPath())) {
				ldapTemplate.rename(dn, user.getDeptFullPath());
				dn = user.getDeptFullPath();
			}
			ldapTemplate.modifyAttributes(dn, attrs);
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} catch (UnsupportedEncodingException e) {
			throw new UserPersistException(e);
		} finally {
			if (ldapTemplate != null) {
				ldapTemplate.closeLdapContext();
			}
		}
	}

	@Override
	public User findById(String id) throws UserPersistException {
		LdapTemplate ldapTemplate = null;
		try {
			ldapTemplate = this.getLdapTemplate();
			Attributes attrs = ldapTemplate.findByDn(id);
			User user = LdapUserManager.marshal(attrs);
			return user;
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} catch (ParseException e) {
			throw new UserPersistException(e);
		} finally {
			if (ldapTemplate != null) {
				ldapTemplate.closeLdapContext();
			}
		}
	}
	
	public Integer getChildrenCountById(String id,
									    String filter)
								   throws UserPersistException {
		LdapTemplate ldapTemplate = null;
		try {
			ldapTemplate = this.getLdapTemplate();
			SearchControls sc = SearchControlsFactory.getSearchControls(
											SearchControls.ONELEVEL_SCOPE);
			List<Attributes> attrsList = ldapTemplate.getResultList(id,
															 		filter,
															 		sc);
		
			return attrsList.size();
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} finally {
			if (ldapTemplate != null) {
				ldapTemplate.closeLdapContext();
			}
		}
	}

	@Override
	public List<User> findByDepartmentId(String id) 
										 throws UserPersistException {
		SearchControls sc = SearchControlsFactory.getDefaultSearchControls();
		List<User> userList = this.findByDepartmentId(id, sc);
		return userList;
	}
	
	@Override
	public List<User> findByDepartmentId(String id,
										 SearchControls sc)
									throws UserPersistException {
		List<User> userList = null;
		LdapTemplate ldapTemplate = null;
		try {
			ldapTemplate = this.getLdapTemplate();
			if ("root".equals(id)) {
				id = "";
			}
			NamingEnumeration<SearchResult> ne = ldapTemplate.getSearchResult(id,
															 				  "(objectclass=employee)",
															 		          sc);
			Map<String, Attributes> map = ldapTemplate.searchResultToMap(ne);
			userList = new ArrayList<User>(map.keySet().size());
			for (String key : map.keySet()) {
				User user = LdapUserManager.marshal(map.get(key));
				user.setDeptFullPath(key);
				user.setDepartmentName(LdapTemplate.spiltNameInNamespace(key));
				userList.add(user);
			}
			return userList;
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} catch (ParseException e) {
			throw new UserPersistException(e);
		} finally {
			if (ldapTemplate != null) {
				ldapTemplate.closeLdapContext();
			}
		}
	}

	@Override
	public List<User> findByName(String name) throws UserPersistException {
		SearchControls sc = SearchControlsFactory.getSearchControls(
										SearchControls.ONELEVEL_SCOPE);
		List<User> userList = this.findByName(name, sc);
		return userList;
	}
	
	@Override
	public List<User> findByName(String name, 
							     SearchControls sc) 
							 throws UserPersistException {
		System.out.println("传过来的搜索名称 "+name);
		List<User> userList = null;
		LdapTemplate ldapTemplate = null;

		try {
			ldapTemplate = this.getLdapTemplate();
			NamingEnumeration<SearchResult> ne = ldapTemplate.getSearchResult("",
																			  "(uid=" + name + ")",
															 		          sc);
			Map<String, Attributes> map = ldapTemplate.searchResultToMap(ne);
			userList = new ArrayList<User>(map.keySet().size());
			for (String key : map.keySet()) {
				User user = LdapUserManager.marshal(map.get(key));
				user.setDeptFullPath(key);
				user.setDepartmentName(LdapTemplate.spiltNameInNamespace(key));
				userList.add(user);
			}
			return userList;
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} catch (ParseException e) {
			throw new UserPersistException(e);
		} finally {
			if (ldapTemplate != null) {
				ldapTemplate.closeLdapContext();
			}
		}
	}
	
	public void updatePassword(String id, String password) throws UserPersistException {
		LdapTemplate ldapTemplate = null;
		try {
			ldapTemplate = this.getLdapTemplate();
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, 
					      				   new BasicAttribute("userPassword", password));
			
			ldapTemplate.modifyAttributes(id, mods);
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} finally {
			if (ldapTemplate != null) {
				ldapTemplate.closeLdapContext();
			}
		}
	}
	
	public void updateAssignPassword(String id, String password) throws UserPersistException {
		LdapTemplate ldapTemplate = null;
		try {
			ldapTemplate = this.getLdapTemplate();
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, 
					      				   new BasicAttribute("assignPassword", password));
			
			ldapTemplate.modifyAttributes(id, mods);
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} finally {
			if (ldapTemplate != null) {
				ldapTemplate.closeLdapContext();
			}
		}
	}
	
	@Override
	public void enable(String... ids) throws UserPersistException {
		try {
			this.setVisible(true, ids);
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} catch (ParseException e) {
			throw new UserPersistException(e);
		}
	}
	
	@Override
	public void disable(String... ids) throws UserPersistException {
		try {
			this.setVisible(false, ids);
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} catch (ParseException e) {
			throw new UserPersistException(e);
		}
	}
	
	public static Attributes unmarshal(User user) throws UnsupportedEncodingException {
		Attributes attrs = new BasicAttributes();

		attrs.put("objectClass", "top");
		attrs.put("objectClass", "person");
		attrs.put("objectClass", "organizationalPerson");
		attrs.put("objectClass", "inetOrgPerson");
		attrs.put("objectClass", "employee");

		if (StringUtils.isNotBlank(user.getUsername())) {
			attrs.put("cn", user.getUsername());
			attrs.put("sn", user.getUsername());
		}
		if (StringUtils.isNotBlank(user.getId())) {
			attrs.put("uid", user.getId());
		}
		if (StringUtils.isNotBlank(user.getPassword())) {
			attrs.put("userPassword", user.getPassword());
		}
		if (StringUtils.isNotBlank(user.getAssignPassword())) {
			attrs.put("assignPassword", user.getAssignPassword());
		}
		if (StringUtils.isNotBlank(user.getGender())) {
			attrs.put("gender", user.getGender());
		} 
		if (StringUtils.isNotBlank(user.getPosition())) {
			attrs.put("position", user.getPosition());
		}
		if (StringUtils.isNotBlank(user.getDegree())) {
			attrs.put("degree", user.getDegree());
		}
		if (StringUtils.isNotBlank(user.getEmail())) {
			attrs.put("mail", user.getEmail());
		}
		if (StringUtils.isNotBlank(user.getMobile())) {
			attrs.put("mobile", user.getMobile());
		}
		if (user.getBirthday() != null) {
			attrs.put("birthday", DatetimeUtils.formatDate(user.getBirthday()));
		}
		if (user.getDateOfWork() != null) {
			attrs.put("dateOfWork", DatetimeUtils.formatDate(user.getDateOfWork()));
		}
		if (user.getStatus() != null) {
			attrs.put("status", user.getStatus().toString());
		}
		if (user.getPostStatus() != null) {
			attrs.put("postStatus", user.getPostStatus().toString());
		}
		if (user.getDepartmentName() != null) {
			attrs.put("deptName", user.getDepartmentName());
		}
		if (user.getDeptFullPath() != null) {
			attrs.put("deptFullPath", user.getDeptFullPath());
		}
		
		return attrs;
	}
	
	public static User marshal(Attributes attrs) throws NamingException,
														 ParseException {
		User user = new User();
		if (attrs.get("sn") != null) {
			user.setUsername((String) attrs.get("sn").get());
		}
		if (attrs.get("uid") != null) {
			user.setId((String) attrs.get("uid").get());
		}
		if (attrs.get("userPassword") != null) {
			user.setPassword(new String((byte[]) attrs.get("userPassword").get()));
		}
		if (attrs.get("assignPassword") != null) {
			user.setAssignPassword((String) attrs.get("assignPassword").get());
		}
		if (attrs.get("gender") != null) {
			user.setGender((String) attrs.get("gender").get());
		}
		if (attrs.get("degree") != null) {
			user.setDegree((String) attrs.get("degree").get());
		}
		if (attrs.get("position") != null) {
			user.setPosition((String) attrs.get("position").get());
		}
		if (attrs.get("mail") != null) {
			user.setEmail((String) attrs.get("mail").get());
		}
		if (attrs.get("mobile") != null) {
			user.setMobile((String) attrs.get("mobile").get());
		}
		if (attrs.get("birthday") != null) {
			user.setBirthday(DatetimeUtils.parseDate(attrs.get("birthday").get().toString()));
		}
		if (attrs.get("dateOfWork") != null) {
			user.setDateOfWork(DatetimeUtils.parseDate(attrs.get("dateOfWork").get().toString()));
		}
		if (attrs.get("status") != null) {
			user.setStatus(new Boolean(attrs.get("status").get().toString()));
		}
		if (attrs.get("postStatus") != null) {
			user.setPostStatus(new Boolean(attrs.get("postStatus").get().toString()));
		}
		if (attrs.get("deptName") != null) {
			user.setDepartmentName(LdapTemplate.spiltNameInNamespace(attrs.get("deptName").get().toString()));
		}
		if (attrs.get("deptFullPath") != null) {
			user.setDeptFullPath(attrs.get("deptFullPath").get().toString());
		}
		
		return user;
	}
	
	private LdapTemplate getLdapTemplate() throws NamingException {
		return new LdapTemplate(this.ldapSvc.getLdapContext());
	}

	@Override
	public List<User> search(String condition) throws UserPersistException {
		List<User> userList = null;
		LdapTemplate ldapTemplate = null;
		try {
			ldapTemplate = this.getLdapTemplate();
			String filter = "(|(uid=*" + condition + "*)(cn=*" + condition + "*))";
			if ("*".equals(condition)) {
				filter = "(objectclass=employee)";
			}
			SearchControls sc = SearchControlsFactory.getSearchControls(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<SearchResult> ne = ldapTemplate.getSearchResult("",
																			  filter,
															 		          sc);
			Map<String, Attributes> map = ldapTemplate.searchResultToMap(ne);
			userList = new ArrayList<User>(map.keySet().size());
			for (String key : map.keySet()) {
				User user = LdapUserManager.marshal(map.get(key));
				user.setDeptFullPath(key);
				user.setDepartmentName(LdapTemplate.spiltNameInNamespace(key));
				userList.add(user);
			}
			return userList;
		} catch (NamingException e) {
			throw new UserPersistException(e);
		} catch (ParseException e) {
			throw new UserPersistException(e);
		} finally {
			if (ldapTemplate != null) {
				ldapTemplate.closeLdapContext();
			}
		}
	}
	
	private void setVisible(Boolean visible, String... ids)
									throws NamingException, 
									       ParseException {
		ModificationItem[] mods = new ModificationItem[1];
		LdapTemplate ldapTemplate = this.getLdapTemplate();
		for (String dn : ids) {
			mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					  new BasicAttribute("status", visible.toString()));
			ldapTemplate.modifyAttributes(dn, mods);
		}
		if (ldapTemplate != null) {
			ldapTemplate.closeLdapContext();
		}
	}
	
	/*
	private LdapTemplate getLdapTemplate(LinkedHashMap<String, Boolean> orderBy)
			throws NamingException, IOException {
		LdapContext ctx = this.ldapSvc.getLdapContext();
		if (orderBy != null) {
			for (String key : orderBy.keySet()) {
				ctx.setRequestControls(new Control[] { new SortControl(key,
						orderBy.get(key)) });
			}
		}
		return new LdapTemplate(ctx);
	}
	*/

	@Override
	public String findStationDivisionByCreator(String creator) throws UserPersistException {
		UserManager userManager = new LdapUserManager(this.ldapSvc);
		SearchControls sc = SearchControlsFactory.getSearchControls(SearchControls.SUBTREE_SCOPE);
		List<User> userList = userManager.findByName(creator, sc);
		String subStation = null;
		List<String> siteDeptList = new ArrayList<String>();
		siteDeptList.add("±500kV广州换流站");
		siteDeptList.add("±500kV宝安换流站");
		siteDeptList.add("500kV福山变电站");
		siteDeptList.add("±500kV肇庆换流站");
		siteDeptList.add("500kV花都变电站");
		siteDeptList.add("±800kV穗东换流站");
		siteDeptList.add("海口分局");
		if (userList != null && userList.size() > 0) {
			if (StringUtils.isNotBlank(userList.get(0).getDeptFullPath())) {
				String fullPath = userList.get(0).getDeptFullPath();
				String[] spilt = StringUtils.split(fullPath, ",");
				for (int i = 0; i < spilt.length; i++) {
					if (spilt[i].indexOf("=") != -1) {
						spilt[i] = StringUtils.substring(spilt[i], spilt[i]
								.indexOf("=") + 1, spilt[i].length());
						if (siteDeptList.contains(spilt[i])) {
							subStation = spilt[i];
							return subStation;
							//setDefect.setStationDivision(subStation);
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public User find(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User[] find(String... ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getReference(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User[] getReferences(String... ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void persist(User... entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public User merge(User entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User[] merge(User... entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User save(User entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User[] save(User... entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(User entity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void remove(User... entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean removeById(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeByIds(String... ids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<User> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <RT> List<RT> search(ISearch search) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <RT> RT searchUnique(ISearch search) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int count(ISearch search) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <RT> com.googlecode.genericdao.search.SearchResult<RT> searchAndCount(
			ISearch search) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAttached(User entity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void refresh(User... entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Filter getFilterFromExample(User example) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Filter getFilterFromExample(User example, ExampleOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getChinaName(String userId) throws UserPersistException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
