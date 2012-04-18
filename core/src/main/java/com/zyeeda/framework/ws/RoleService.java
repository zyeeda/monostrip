package com.zyeeda.framework.ws;

import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.Search;
import com.zyeeda.framework.entities.Role;
import com.zyeeda.framework.entities.TheThirdRoleAndUser;
import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.managers.RoleManager;
import com.zyeeda.framework.managers.TheThirdUserAndRoleManager;
import com.zyeeda.framework.managers.UserManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.managers.internal.DefaultRoleManager;
import com.zyeeda.framework.managers.internal.DefaultTheThirdTableManager;
import com.zyeeda.framework.managers.internal.DefaultUserManager;
import com.zyeeda.framework.managers.internal.LdapUserManager;
import com.zyeeda.framework.utils.DatetimeUtils;
import com.zyeeda.framework.viewmodels.RoleVo;
import com.zyeeda.framework.viewmodels.UserNameVo;
import com.zyeeda.framework.viewmodels.UserVo;
import com.zyeeda.framework.ws.base.ResourceService;

@Path("/roles")
public class RoleService extends ResourceService {

	private static final Logger logger = LoggerFactory
			.getLogger(RoleService.class);

	// private final static String ROAM_PERMISSION_FILE = "roamPermission.xml";

	// private final static String PERMISSION_FILE = "permission.xml";

	@GET
	@Path("/get_role_with_out_dept")
	@Produces("application/json")
	public List<Role> getRoleWithOutDept() {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Search search = new Search();
		search.addFilterEmpty("deptepmentId");
		search.addFilterNull("deptepmentId");
		List<Role> list = roleMgr.search(search);
		return list;
	}

	@GET
	@Path("/{id}/get_role")
	@Produces("application/json")
	public Role getOneRolesById(@PathParam("id") String id) {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		return roleMgr.find(id);
	}

	@DELETE
	@Path("/")
	@Produces("application/json")
	public boolean getRoles(@QueryParam("ids") String ids) {
		TheThirdUserAndRoleManager roleAndUserMgr = new DefaultTheThirdTableManager(
				this.getPersistenceService());
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		boolean bool = true;
		try {
			if (roleMgr != null) {
				String[] id = ids.split(";");
				for (String sigleId : id) {
					TheThirdRoleAndUser theThirdRoleAndUser = roleAndUserMgr
							.find(sigleId);
					roleAndUserMgr.remove(theThirdRoleAndUser);
					roleMgr.removeById(sigleId);
				}
			}
		} catch (Exception e) {
			bool = false;
		}
		return bool;
	}

	@DELETE
	@Path("/defect_zizhi")
	@Produces("application/json")
	public List<Role> getRolesReturn(@QueryParam("ids") String ids) {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		TheThirdUserAndRoleManager roleAndUserMgr = new DefaultTheThirdTableManager(
				this.getPersistenceService());
		Search search = new Search();
		if (roleMgr != null) {
			String[] id = ids.split(";");
			for (String sigleId : id) {
				search.clear();
				search.addFilterEqual("roleId", sigleId);
				List<TheThirdRoleAndUser> userNameVoList = roleAndUserMgr
						.search(search);
				for (TheThirdRoleAndUser theThirdRoleAndUser : userNameVoList) {
					roleAndUserMgr.remove(theThirdRoleAndUser);
				}
				roleMgr.removeById(sigleId);
			}
		}
		search.clear();
		search.addFilterEmpty("deptepmentId");
		search.addFilterNull("deptepmentId");
		return roleMgr.search(search);
	}

	@GET
	@Path("/{id}/role_and_auth")
	@Produces("application/json")
	public List<TheThirdRoleAndUser> getRolesById(@PathParam("id") String id)
			throws XPathExpressionException, IOException, UserPersistException {
		TheThirdUserAndRoleManager roleAndUserMgr = new DefaultTheThirdTableManager(
				this.getPersistenceService());
		Search search = new Search();
	
		search.addFilterEqual("roleId", id);
		List<TheThirdRoleAndUser> roleWithUserVo = roleAndUserMgr
				.search(search);

		return roleWithUserVo;
	}
	public List<TheThirdRoleAndUser> getRolesByName( String name)
			throws XPathExpressionException, IOException, UserPersistException {
		TheThirdUserAndRoleManager roleAndUserMgr = new DefaultTheThirdTableManager(
				this.getPersistenceService());
		Search search = new Search();
		search.addFilterEqual("name", name);
		List<TheThirdRoleAndUser> roleWithUserVo = roleAndUserMgr
				.search(search);

		return roleWithUserVo;
	}
	@GET
	@Path("/{id}/search_role_and_auth")
	@Produces("application/json")
	public List<TheThirdRoleAndUser> searchRoleAndAuth(
			@PathParam("id") String id, @QueryParam("param") String param) {
		TheThirdUserAndRoleManager roleAndUserMgr = new DefaultTheThirdTableManager(
				this.getPersistenceService());
		Search search = new Search();
		search.addFilterEqual("roleId", id);
		search.addFilterOr(
				Filter.like("userName", (StringUtils.isBlank(param)) ? null
						: "%" + param.trim() + "%"), Filter.like(
						"userId",
						(StringUtils.isBlank(param)) ? null : "%"
								+ param.trim() + "%"), Filter.like(
						"belongToDept", (StringUtils.isBlank(param)) ? null
								: "%" + param.trim() + "%"));
		List<TheThirdRoleAndUser> userNameVoList = roleAndUserMgr
				.search(search);
		return userNameVoList;
	}

	@POST
	@Path("/{id}/edite")
	@Produces("application/json")
	public Role editeRole(@PathParam("id") String id, @FormParam("") Role role) {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role newRole = roleMgr.find(id);
		newRole.setName(role.getName());
		newRole.setDescription(role.getDescription());
		this.getPersistenceService().getCurrentSession().flush();
		return roleMgr.find(id);
	}

	@POST
	@Path("/{id}/zizhi_edite")
	@Produces("application/json")
	public List<Role> editeRoleReturnAll(@PathParam("id") String id,
			@FormParam("") Role role) {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role newRole = roleMgr.find(id);
		newRole.setName(role.getName());
		newRole.setDescription(role.getDescription());
		Search search = new Search();
		search.addFilterEmpty("deptepmentId");
		search.addFilterNull("deptepmentId");
		this.getPersistenceService().getCurrentSession().flush();
		return roleMgr.search(search);
	}

	@POST
	@Path("/")
	@Produces("application/json")
	public Role creatRole(@FormParam("") Role role) {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		roleMgr.persist(role);
		this.getPersistenceService().getCurrentSession().flush();
		return roleMgr.find(role.getId());
	}

	@POST
	@Path("/return_all")
	@Produces("application/json")
	public List<Role> creatRoleReturnAll(@FormParam("") Role role) {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());

		String name = role.getName();
		Search search = new Search();
		search.addFilterEqual("name", name);
		search.addFilterEqual("deptepment", role.getDeptepment());
		List<Role> list = roleMgr.search(search);
		if (list.size() > 0) {
			return null;
		} else {
			roleMgr.persist(role);
			search.clear();
			search.addFilterEmpty("deptepmentId");
			search.addFilterNull("deptepmentId");
			this.getPersistenceService().getCurrentSession().flush();
			return roleMgr.search(search);
		}
	}

	@PUT
	@Path("/")
	@Produces("application/json")
	public Role editRole(@FormParam("") Role role) {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role setRole = roleMgr.find(role.getId());
		Role newRole = roleMgr.save(setRole);
		this.getPersistenceService().getCurrentSession().flush();
		return newRole;
	}

	@POST
	@Path("/{id}/assign_user")
	// TODO
	@Produces("application/json")
	public void assignRoleUser(@PathParam("id") String id,
			@QueryParam("ids") String ids) throws UserPersistException {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role role = roleMgr.find(id);
		TheThirdUserAndRoleManager roleAndUserMgr = new DefaultTheThirdTableManager(
				this.getPersistenceService());
		// role.getSubjects().clear();
		if (StringUtils.isNotBlank(ids)) {
			String[] usersName = ids.split(",");
			for (int i = 0; i < usersName.length; i++) {
				if (role.getDeptepmentId() == null) {
					Search search = new Search();
					search.addFilterEqual("roleId", id);
					search.addFilterEqual("userId", usersName[i]);
					int rs = roleAndUserMgr.count(search);
					if (rs == 0) {
						TheThirdRoleAndUser roleAndUser = new TheThirdRoleAndUser();
						roleAndUser.setRoleId(id);
						roleAndUser.setUserId(usersName[i]);
						roleAndUser.setIsPass(true);
						roleAndUser.setPassTime(DatetimeUtils
								.getCurenDatePussMonth());
						String dept  = this.getDeptByUser(usersName[i]);
						roleAndUser.setBelongToDept(dept);
						roleAndUser.setUserName(this.getChinaName(usersName[i]));
						roleAndUserMgr.persist(roleAndUser);
					}
				}
				role.getSubjects().add(usersName[i]);
			}
		}
		this.getPersistenceService().getCurrentSession().flush();
	}

	@GET
	@Path("/{id}/sub_user")
	@Produces("application/json")
	public List<String> getUserByRoleId(@PathParam("id") String id) {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role role = roleMgr.find(id);
		Set<String> user = role.getSubjects();
		List<String> list = new ArrayList<String>();
		for (String userId : user) {
			list.add(userId);
		}
		return list;
	}

	@POST
	@Path("/{id}/assign_auth")
	@Produces("application/json")
	public Role assignRoleAuth(@PathParam("id") String id,
			@FormParam("") Role role) throws XPathExpressionException,
			IOException {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role newRole = roleMgr.find(id);
		String authArray = role.getPermissions();
		String auth = newRole.getPermissions();
		if (StringUtils.isBlank(auth)) {
			newRole.setPermissions(authArray);
		} else {
			int flog = auth.indexOf("&");
			if (flog >= 0) {
				String menuAuth = auth.substring(flog + 1, auth.length());
				String menuPermission = authArray + "&" + menuAuth;
				newRole.setPermissions(menuPermission);
			} else {
				newRole.setPermissions(authArray);
			}
		}
		this.getPersistenceService().getCurrentSession().flush();
		return newRole;
	}

	@POST
	@Path("/{id}/assign_raom_auth")
	@Produces("application/json")
	public Role assignroamRoleAuth(@PathParam("id") String id,
			@FormParam("") Role role) throws XPathExpressionException,
			IOException {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role newRole = roleMgr.find(id);
		String authArray = role.getRamoPermissions();
		logger.debug("this ramoPermissions is : {}", role.getRamoPermissions());
		String auth = newRole.getPermissions();
		if (StringUtils.isBlank(auth)) {
			String menuPermission = "&" + authArray;
			newRole.setPermissions(menuPermission);
		} else {
			int flog = auth.indexOf("&");
			if (flog >= 0) {
				String menuAuth = auth.substring(0, flog);
				String menuPermission = menuAuth + "&" + authArray;
				newRole.setPermissions(menuPermission);
			} else {
				String menuPermission = auth + "&" + authArray;
				logger.debug("this ramoPermissions is : {}", menuPermission);
				newRole.setPermissions(menuPermission);
			}
		}
		this.getPersistenceService().getCurrentSession().flush();
		return newRole;
	}

	@POST
	@Path("/{id}/remove_auth")
	@Produces("application/json")
	public Role removeAuth(@PathParam("id") String id,
			@QueryParam("permission") String permission)
			throws XPathExpressionException, IOException {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role role = roleMgr.find(id);
		String[] permissions = permission.split(";");
		List<String> permissionSet = role.getPermissionList();
		for (String havaermissions : permissions) {
			if (permissionSet.contains(havaermissions)) {
				permissionSet.remove(havaermissions);
			}
		}
		String utils = StringUtils.join(permissionSet, ";");
		role.setPermissions(utils);
		this.getPersistenceService().getCurrentSession().flush();
		return role;

	}

	@POST
	@Path("/{id}/remove_user_zizhi")
	@Produces("application/json")
	public boolean removeUser(@PathParam("id") String id,
			@QueryParam("subject") String subject, @QueryParam("ids") String ids)
			throws XPathExpressionException, IOException {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		TheThirdUserAndRoleManager roleAndUserMgr = new DefaultTheThirdTableManager(
				this.getPersistenceService());
		Role role = roleMgr.find(id);
		String[] subjects = subject.split(";");
		String[] thirdTable = ids.split(";");
		for (String thirdId : thirdTable) {
			roleAndUserMgr.removeById(thirdId);
		}
		boolean result = false;
		for (String subjectsSub : subjects) {
			for (String auth : role.getSubjects()) {
				if (auth.equals(subjectsSub)) {
					result = true;
					logger.debug("this role's subject remove is success");
					break;
				}
			}
			if (result) {
				role.getSubjects().remove(subjectsSub);
			}
		}
		this.getPersistenceService().getCurrentSession().flush();
		return result;
	}

	@POST
	@Path("/{id}/remove_user")
	@Produces("application/json")
	public boolean removeUser(@PathParam("id") String id,
			@QueryParam("subject") String subject)
			throws XPathExpressionException, IOException {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role role = roleMgr.find(id);
		String[] subjects = subject.split(";");
		boolean result = false;
		for (String subjectsSub : subjects) {
			for (String auth : role.getSubjects()) {
				if (auth.equals(subjectsSub)) {
					result = true;
					logger.debug("this role's subject remove is success");
					break;
				}
			}
			if (result) {
				role.getSubjects().remove(subjectsSub);
			}
		}
		this.getPersistenceService().getCurrentSession().flush();
		return result;
	}

	@GET
	@Path("/get_roles_subuser")
	@Produces("application/json")
	public Set<UserVo> getAllSubUser() throws UserPersistException {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		String creator = this.getSecurityService().getCurrentUser();
		UserManager userManager = new LdapUserManager(this.getLdapService());
		String subStationName = userManager
				.findStationDivisionByCreator(creator);
		Set<UserVo> userNameVoList = new HashSet<UserVo>();
		Search search = new Search();
		if ("海口分局".equals(subStationName)) {
			search.addFilterEqual("name", "福山站当班值-值长");
			search.addFilterEqual("deptepment", subStationName);
		} else {
			search.addFilterEqual("name", "当班值-值长");
			search.addFilterEqual("deptepment", subStationName);
		}
		List<Role> roleList = roleMgr.search(search);
		for (Role role : roleList) {
			if (role.getName() != null && role.getDeptepment() != null
					&& subStationName != null) {
				if (("当班值-值长".equals(role.getName()) && role.getDeptepment()
						.equals(subStationName))
						|| ("福山站当班值-值长".equals(role.getName()) && role
								.getDeptepment().equals(subStationName))) {
					for (String user : role.getSubjects()) {
						UserManager userMgr = new DefaultUserManager(
								this.getPersistenceService());
						User userId = userMgr.findById(user);
						if (userId != null) {
							UserVo userVo = new UserVo();
							userVo.setCheckName(userId.getUsername());
							userVo.setLabel(userId.getUsername());
							userVo.setType("task");
							userVo.setLeaf(true);
							if (userNameVoList.size() == 0) {
								userNameVoList.add(userVo);
								continue;
							}
							for (UserVo userNameVo : userNameVoList) {
								if (!(userNameVo.getCheckName().equals(user))) {
									userNameVoList.add(userVo);
									break;
								}
							}
						}
					}
				}
			}
		}
		return userNameVoList;
	}

	@GET
	@Path("/get_roles_not_subuser")
	@Produces("application/json")
	public Set<UserVo> getAllNotSubUser() throws UserPersistException {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		String creator = this.getSecurityService().getCurrentUser();
		UserManager userManager = new LdapUserManager(this.getLdapService());
		String subStationName = userManager
				.findStationDivisionByCreator(creator);
		logger.info("this substationName value is : ", subStationName);
		Set<UserVo> userNameVoList = new HashSet<UserVo>();
		Search search = new Search();
		if ("海口分局".equals(subStationName)) {
			search.addFilterOr(Filter.and(Filter.equal("name", "福山站当班值-值班员"),
					Filter.equal("deptepment", subStationName)), Filter.and(
					Filter.equal("deptepment", subStationName),
					Filter.equal("name", "福山站当班值-值长")));
		} else {
			search.addFilterOr(Filter.and(Filter.equal("name", "当班值-值班员"),
					Filter.equal("deptepment", subStationName)), Filter.and(
					Filter.equal("deptepment", subStationName),
					Filter.equal("name", "当班值-值长")));
		}
		List<Role> roleList = roleMgr.search(search);
		// List<Role> roleList =
		// roleMgr.findRoleBySubStationName(subStationName);
		for (Role role : roleList) {
			if (role.getName() != null && role.getDeptepment() != null
					&& subStationName != null) {
				if ((("当班值-值班员".equals(role.getName()) || "当班值-值长".equals(role
						.getName())) && subStationName.equals(role
						.getDeptepment()))
						|| (("福山站当班值-值班员".equals(role.getName()) || "福山站当班值-值长"
								.equals(role.getName())) && subStationName
								.equals(role.getDeptepment()))) {
					for (String user : role.getSubjects()) {
						UserManager userMgr = new DefaultUserManager(
								this.getPersistenceService());
						User userId = userMgr.findById(user);
						if (userId != null) {
							UserVo userVo = new UserVo();
							userVo.setCheckName(userId.getUsername());
							userVo.setLabel(userId.getUsername());
							userVo.setType("task");
							userVo.setLeaf(true);
							if (userNameVoList.size() == 0) {
								userNameVoList.add(userVo);
								continue;
							}
							for (UserVo userNameVo : userNameVoList) {
								if (!(userNameVo.getCheckName().equals(user))) {
									userNameVoList.add(userVo);
									break;
								}
							}
						}
					}
				}
			}
		}
		return userNameVoList;
	}

	@GET
	@Path("/depts/{id}")
	@Produces("application/json")
	public List<UserVo> getUserVo(@PathParam("id") String id)
			throws UserPersistException {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role role = roleMgr.find(id);
		List<UserVo> listUserVo = roleMgr.getUserVoByRole(role);
		return listUserVo;
	}

	@GET
	@Path("/get_all_roles_vo")
	@Produces("application/json")
	public List<RoleVo> getRolesVo() {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		String hql = "select distinct F_DEPTEMENT_ID, F_DEPTEPMENT from sys_role";
		List<Role> listRole = new ArrayList<Role>();
		listRole = roleMgr.getRoleDistinct(hql);
		logger.debug("this get all roles is success!", listRole.size());
		List<RoleVo> roleVo = roleMgr.deptToVo(listRole);
		return roleVo;
	}

	@GET
	@Path("/dept_and_role/{deptId}/{isLeaf}")
	@Produces("application/json")
	public List<RoleVo> getDeptById(@PathParam("deptId") String deptId, @PathParam("isLeaf") Boolean isLeaf) {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		List<Role> list = new ArrayList<Role>();
		Search search = new Search();
		search.addFilterEqual("deptepmentId", deptId);
		list = roleMgr.search(search);
		List<RoleVo> roleVo = roleMgr.roleToVo(list, isLeaf);
		return roleVo;
	}

	@GET
	@Path("/get_role_user_by_form/{isSureReview}")
	@Produces("application/json")
	public Set<UserVo> getRoleVo(@PathParam("isSureReview") String isSureReview)
			throws UserPersistException {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		List<Role> list = new ArrayList<Role>();
		Search search = new Search();
		String belongToDept = this.getDeptByUser(null);
		if ("true".equals(isSureReview)) {
			search.addFilterEqual("deptepment", "生产技术部");
			logger.debug("this belongToDept value is : {}", belongToDept);
			search.addFilterEqual("name", "局表单管理专责");
		} else {
			search.addFilterEqual("deptepment", belongToDept);
			logger.debug("this belongToDept value is : {}", belongToDept);
			search.addFilterEqual("name", "表单专责");
		}
		Set<UserVo> userNameVoList = new HashSet<UserVo>();
		list = roleMgr.search(search);
		// List<RoleVo> roleVo = roleMgr.roleToVo(list);
		for (Role role : list) {
			for (String user : role.getSubjects()) {
				UserManager userMgr = new DefaultUserManager(
						this.getPersistenceService());
				User userId = userMgr.findById(user);
				UserVo userVo = new UserVo();
				userVo.setCheckName(userId.getUsername());
				userVo.setLabel(userId.getUsername());
				userVo.setType("task");
				userVo.setLeaf(true);
				if (userNameVoList.size() == 0) {
					userNameVoList.add(userVo);
					continue;
				}
				for (UserVo userNameVo : userNameVoList) {
					if (!(userNameVo.getCheckName().equals(user))) {
						userNameVoList.add(userVo);
						break;
					}
				}
			}
		}
		return userNameVoList;
	}

	@GET
	@Path("/get_sub_user_by_subject")
	@Produces("application/json")
	public Set<UserVo> getTransMonitor(
			@QueryParam("isMonitor") Boolean isMonitor)
			throws UserPersistException {
		String subject = this.getSecurityService().getCurrentUser();
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		List<Role> roleList = roleMgr.getRoleBySubject(subject);
		Set<UserVo> userNameVoList = new HashSet<UserVo>();
		// List<String> roleName = new ArrayList<String>();
		for (Role role : roleList) {
			String witchMonitor = null;
			String roleName = role.getName();
			boolean bool = false;
			if ("输电管理所".equals(role.getDeptepment())) {
				if ("输电一班-班员".equals(roleName)) {
					witchMonitor = "输电一班-班长";
					bool = true;
				} else if ("输电二班-班员".equals(roleName)) {
					witchMonitor = "输电二班-班长";
					bool = true;
				} else if ("输电三班-班员".equals(roleName)) {
					witchMonitor = "输电三班-班长";
					bool = true;
				} else if ("输电四班-班员".equals(roleName)) {
					witchMonitor = "输电四班-班长";
					bool = true;
				} else if ("输电五班-班员".equals(roleName)) {
					witchMonitor = "输电五班-班长";
					bool = true;
				} else if ("输电六班-班员".equals(roleName)) {
					witchMonitor = "输电六班-班长";
					bool = true;
				} else if ("输电七班-班员".equals(roleName)) {
					witchMonitor = "输电七班-班长";
					bool = true;
				} else if ("输电八班-班员".equals(roleName)) {
					witchMonitor = "输电八班-班长";
					bool = true;
				} else if ("输电一班-班长".equals(roleName)) {
					witchMonitor = "输电一班-班员";
					bool = true;
				} else if ("输电二班-班长".equals(roleName)) {
					witchMonitor = "输电二班-班员";
					bool = true;
				} else if ("输电三班-班长".equals(roleName)) {
					witchMonitor = "输电三班-班员";
					bool = true;
				} else if ("输电四班-班长".equals(roleName)) {
					witchMonitor = "输电四班-班员";
					bool = true;
				} else if ("输电五班-班长".equals(roleName)) {
					witchMonitor = "输电五班-班员";
					bool = true;
				} else if ("输电六班-班长".equals(roleName)) {
					witchMonitor = "输电六班-班员";
					bool = true;
				} else if ("输电七班-班长".equals(roleName)) {
					witchMonitor = "输电七班-班员";
					bool = true;
				} else if ("输电八班-班长".equals(roleName)) {
					witchMonitor = "输电八班-班员";
					bool = true;
				}

				if (bool) {
					Search search = new Search();
					search.addFilterEqual("name", witchMonitor);
					search.addFilterEqual("deptepment", "输电管理所");
					List<Role> roles = roleMgr.search(search);
					if (witchMonitor != null) {
						if (isMonitor && witchMonitor.contains("长")) {
							for (Role getRole : roles) {
								if (witchMonitor.equals(getRole.getName())
										&& "输电管理所".equals(getRole
												.getDeptepment())) {
									role = getRole;
								}
							}
						} else if (!isMonitor && witchMonitor.contains("员")) {
							for (Role getRole : roles) {
								if (witchMonitor.equals(getRole.getName())
										&& "输电管理所".equals(getRole
												.getDeptepment())) {
									role = getRole;
								}
							}
						}
						for (String user : role.getSubjects()) {
							UserManager userMgr = new DefaultUserManager(
									this.getPersistenceService());
							User userId = userMgr.findById(user);
							UserVo userVo = new UserVo();
							userVo.setCheckName(userId.getUsername());
							userVo.setLabel(userId.getUsername());
							userVo.setType("task");
							userVo.setId(user);
							userVo.setLeaf(true);
							if (userNameVoList.size() == 0) {
								userNameVoList.add(userVo);
								continue;
							}
							for (UserVo userNameVo : userNameVoList) {
								if (!(userNameVo.getId().equals(user))) {
									userNameVoList.add(userVo);
									break;
								}
							}
						}
					}
				}
			}
		}
		return userNameVoList;
	}

	@GET
	@Path("/get_role_and_user_by_subject")
	// TODO
	@Produces("application/json")
	public List<UserNameVo> getRoleAndUserBySubject(
			@QueryParam("userName") String userName) {
		UserManager userMgr = new DefaultUserManager(
				this.getPersistenceService());
		Search search = new Search();
		search.addFilterOr(Filter.like("username", "%" + userName + "%"),
				Filter.like("id", "%" + userName + "%"));
		List<User> userList = userMgr.search(search);
		List<UserNameVo> userNameRoleLists = new ArrayList<UserNameVo>();
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		for (User user : userList) {
			UserNameVo userNameWithRole = new UserNameVo();
			userNameWithRole.setUserChinaName(user.getUsername());
			userNameWithRole.setUserName(user.getId());
			List<String> roleList = roleMgr.getRoleNameBySubject(user.getId());
			userNameWithRole.getSetRole().addAll(roleList);
			userNameRoleLists.add(userNameWithRole);
		}
		return userNameRoleLists;
	}

	@POST
	@Path("/assign_date")
	@Produces("application/json")
	public String assignDateTime(@QueryParam("thirdIds") String thirdIds,
			@QueryParam("date") String date) {
		TheThirdUserAndRoleManager roleAndUserMgr = new DefaultTheThirdTableManager(
				this.getPersistenceService());
		String[] idArray = null;
		if (StringUtils.isNotBlank(thirdIds)) {
			idArray = thirdIds.split(";");
		}
		Date dateTime = null;
		if (StringUtils.isNotBlank(date)) {
			dateTime = DatetimeUtils.stringToDate(date,
					DatetimeUtils.DEFAULT_DATE_FORMAT_PATTERN);
		}
		if (idArray == null) {
			throw new RuntimeException("参数ID为不能为空！");
		}
		roleAndUserMgr.editeTheThirdTableDate(idArray, dateTime);
		return "sucess";
	}

	@GET
	@Path("/currentPerm")
	@Produces("application/json")
	public Set<String> getCurrentPermissions() throws IOException, SQLException {
		String sql = "SELECT RO.F_NAME, RO.f_permissions FROM SYS_SUBJECT JE INNER JOIN SYS_ROLE RO ON(RO.F_ID = JE.F_ROLE_ID)  WHERE F_SUBJECT = ?";
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		List<Object[]> list = roleMgr.getBySql(sql, new String[] { this
				.getSecurityService().getCurrentUser() });
		Set<String> set = new HashSet<String>();
		for (Object[] obj : list) {
			if (obj[1] != null) {
				String clobStr = ((Clob) obj[1]).getSubString(1,
						(int) ((Clob) obj[1]).length());
				// String clobStr = IOUtils.toString(((Clob)
				// obj[1]).getCharacterStream());
				String[] strs = clobStr.split(";");
				for (String str : strs) {
					set.add(str);
				}
			}
		}
		return set;
	}
	@POST
	@Path("/{srcRoleId}/copy_permission/{copyToRoleIds}")
	@Produces("application/json")
	public void copyPermissions(@PathParam("srcRoleId") String srcRoleId, @PathParam("copyToRoleIds") String copyToRoleIds)  {
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role srcRole = roleMgr.find(srcRoleId);
		String[] copyToRoleIdsArry = new String[]{};
		if (StringUtils.isNotBlank(copyToRoleIds)) {
			copyToRoleIdsArry = copyToRoleIds.split(";");
		}
		Search search = new Search();
		search.addFilterIn("id", CollectionUtils.asList(copyToRoleIdsArry));
		List<Role> roleList = roleMgr.search(search);
		for(Role role : roleList) {
			role.setPermissions(srcRole.getPermissions());
		}
		this.getPersistenceService().getCurrentSession().flush();
	}
}
