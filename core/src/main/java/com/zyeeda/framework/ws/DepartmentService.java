package com.zyeeda.framework.ws;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.framework.entities.Department;
import com.zyeeda.framework.entities.Role;
import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.ldap.LdapService;
import com.zyeeda.framework.managers.RoleManager;
import com.zyeeda.framework.managers.UserManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.managers.internal.DefaultRoleManager;
import com.zyeeda.framework.managers.internal.LdapDepartmentManager;
import com.zyeeda.framework.managers.internal.LdapUserManager;
import com.zyeeda.framework.sync.UserSyncService;
import com.zyeeda.framework.viewmodels.DepartmentVo;
import com.zyeeda.framework.viewmodels.OrganizationNodeVo;
import com.zyeeda.framework.viewmodels.UserVo;
import com.zyeeda.framework.ws.base.ResourceService;

@Path("/depts")
public class DepartmentService extends ResourceService {

	private static final Logger logger = LoggerFactory
			.getLogger(DepartmentService.class);

	@POST
	@Path("/{parent}")
	@Produces("application/json")
	public Department persist(@FormParam("") Department dept,
			@PathParam("parent") String parent) throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		if (deptMgr.findByName(dept.getName()) != null
				&& deptMgr.findByName(dept.getName()).size() > 0) {
			throw new RuntimeException("部门名称不能重复");
		} else {
			dept.setParent(parent);
			dept.setId("ou=" + dept.getName() + "," + dept.getParent());
			dept.setDeptFullPath("ou=" + dept.getName() + ","
					+ dept.getParent());
			deptMgr.persist(dept);
			return deptMgr.findById(dept.getId());
		}
	}

	@DELETE
	@Path("/{id}")
	@Produces("application/json")
	public String remove(@PathParam("id") String id,
			@FormParam("cascade") String cascade) throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		LdapUserManager userManager = new LdapUserManager(ldapSvc);
		if (cascade != null) {
			deptMgr.remove(id);
			return "{\"success\": \"true\"}";
		} else {
			Integer deptCount = deptMgr.getChildrenCountById(id,
					"(objectclass=*)");
			Integer userCount = userManager.getChildrenCountById(id,
					"(objectclass=*)");
			if (userCount > 0 || deptCount > 0) {
				return "{\"success\": \"false\"}";
			} else {
				deptMgr.remove(id);
				return "{\"success\": \"true\"}";
			}
		}
	}

	@PUT
	@Path("/{id}")
	@Produces("application/json")
	public Department update(@FormParam("") Department dept,
			@PathParam("id") String id) throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		UserSyncService userSyncService = this.getUserSynchService();
		Department oldDept = deptMgr.findById(id);
		String tmp = oldDept.getDeptFullPath();
		String newDeptFullPath = "ou=" + dept.getName()
				+ tmp.substring(tmp.indexOf(","), tmp.length());
		dept.setDeptFullPath(newDeptFullPath);
		tmp = oldDept.getParent();
		dept.setParent(tmp.substring(0, tmp.lastIndexOf("/") + 1)
				+ dept.getName());
		if (!oldDept.getName().equals(dept.getName())) {
			deptMgr.rename(oldDept.getDeptFullPath(), newDeptFullPath);
			tmp = oldDept.getId();
			dept.setId("ou=" + dept.getName()
					+ tmp.substring(tmp.indexOf(","), tmp.length()));
			userSyncService.updateDeptName(oldDept.getDeptFullPath(),
					newDeptFullPath);
		} else {
			dept.setId(id);
		}
		deptMgr.update(dept);
		return dept;
	}

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Department findById(@PathParam("id") String id)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);

		return deptMgr.findById(id);
	}

	@GET
	@Path("/search/{name}")
	@Produces("application/json")
	public List<DepartmentVo> findByName(@PathParam("name") String name,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		List<Department> deptList = deptMgr.findByName(name);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}

		return DepartmentService.fillPropertiesToVo(deptList);
	}

	@GET
	@Path("/search")
	@Produces("application/json")
	public String search(@FormParam("name") String name,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		
		logger.debug("资质搜索");
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		List<Department> deptList = deptMgr.search(name);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		StringBuffer buffer = new StringBuffer("{");
		buffer.append("\"totalRecords\":").append(deptList.size()).append(",")
				.append("\"startIndex\":").append(0).append(",")
				.append("\"pageSize\":").append(13).append(",")
				.append("\"records\":[");
		for (Department department : deptList) {
			buffer.append("{\"name\":")
					.append("\"")
					.append(department.getId())
					.append("\"")
					.append(",")
					.append("\"parent\":")
					.append("\"")
					.append(department.getParent() == null ? "" : department
							.getParent())
					.append("\"")
					.append(",")
					.append("\"fullpath\":")
					.append("\"")
					.append(department.getDeptFullPath() == null ? ""
							: department.getDeptFullPath())
					.append("\"")
					.append(",")
					.append("\"description\":")
					.append("\"")
					.append(department.getDescription() == null ? ""
							: department.getDescription()).append("\"")
					.append("},");
		}
		if (buffer.lastIndexOf(",") != -1 && deptList.size() > 0) {
			buffer.deleteCharAt(buffer.lastIndexOf(","));
		}
		buffer.append("]}");

		return buffer.toString();
	}

	@GET
	@Path("/search/{parent}/{name}")
	@Produces("application/json")
	public List<DepartmentVo> findByName(@PathParam("parent") String parent,
			@PathParam("name") String name,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		List<Department> deptList = deptMgr.findByName(parent, name);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		return DepartmentService.fillPropertiesToVo(deptList);
	}

	@GET
	@Path("/{id}/children")
	@Produces("application/json")
	public List<OrganizationNodeVo> getChildrenById(
			@Context HttpServletRequest request, @PathParam("id") String id,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();

		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		List<DepartmentVo> deptVoList = null;
		List<UserVo> userVoList = null;
		List<Department> deptList = null;
		String type = request.getParameter("type");
		if (StringUtils.isNotBlank(type) && "task".equals(type)) {
			deptList = deptMgr.getChildrenById(id);
			if ("true".equalsIgnoreCase(isFilter)) {
				this.filterDeptList(deptList);
			}
			deptVoList = DepartmentService.fillPropertiesToVo(deptList, type);
			userVoList = UserService.fillUserListPropertiesToVo(
					userMgr.findByDepartmentId(id), type);
		} else {
			deptList = deptMgr.getChildrenById(id);
			deptVoList = DepartmentService.fillPropertiesToVo(deptList, type);
			deptVoList = DepartmentService.fillPropertiesToVo(deptList);
			userVoList = UserService.fillUserListPropertiesToVo(userMgr
					.findByDepartmentId(id));
		}
		List<OrganizationNodeVo> orgList = this.mergeDepartmentVoAndUserVo(
				deptVoList, userVoList);

		return orgList;
	}

	private List<OrganizationNodeVo> mergeDepartmentVoAndUserVo(
			List<DepartmentVo> deptVoList, List<UserVo> userVoList) {
		List<OrganizationNodeVo> orgNodeVoList = new ArrayList<OrganizationNodeVo>();
		for (DepartmentVo deptVo : deptVoList) {
			OrganizationNodeVo orgNodeVo = new OrganizationNodeVo();
			orgNodeVo.setId(deptVo.getId());
			orgNodeVo.setCheckName(deptVo.getCheckName());
			orgNodeVo.setIo(deptVo.getIo());
			orgNodeVo.setLabel(deptVo.getLabel());
			orgNodeVo.setType(deptVo.getType());
			orgNodeVo.setFullPath(deptVo.getDeptFullPath());
			orgNodeVo.setKind(deptVo.getKind());

			orgNodeVoList.add(orgNodeVo);
		}

		for (UserVo userVo : userVoList) {
			logger.debug("username = {}", userVo.getCheckName());
			OrganizationNodeVo orgNodeVo = new OrganizationNodeVo();
			orgNodeVo.setId(userVo.getDeptFullPath());
			orgNodeVo.setCheckName(userVo.getCheckName());
			orgNodeVo.setIo(userVo.getId());
			orgNodeVo.setLabel(userVo.getLabel());
			orgNodeVo.setType(userVo.getType());
			orgNodeVo.setLeaf(userVo.isLeaf());
			orgNodeVo.setFullPath(userVo.getDeptFullPath());
			orgNodeVo.setKind(userVo.getKind());

			orgNodeVoList.add(orgNodeVo);
		}
		return orgNodeVoList;
	}

	public static DepartmentVo fillPropertiesToVo(Department dept) {
		DepartmentVo deptVo = new DepartmentVo();
		deptVo.setId(dept.getId());
		deptVo.setType("io");
		deptVo.setLabel(dept.getName());
		deptVo.setCheckName(dept.getId());
		deptVo.setLeaf(false);
		if (StringUtils.isBlank(dept.getDeptFullPath())) {
			deptVo.setDeptFullPath("o=" + dept.getId());
		} else {
			deptVo.setDeptFullPath(dept.getDeptFullPath());
		}
		if (StringUtils.isBlank(deptVo.getDeptFullPath())) {
			deptVo.setIo("/rest/depts/root/children");
		} else {
			deptVo.setIo("/rest/depts/" + deptVo.getDeptFullPath()
					+ "/children");
		}
		deptVo.setKind("dept");

		return deptVo;
	}

	public static DepartmentVo fillPropertiesToVoWk(Department dept,String roleId) {
		logger.debug("进入了fillPropertiesToVoWk ");
		logger.debug("roleId = {}",roleId);
		DepartmentVo deptVo = new DepartmentVo();
		deptVo.setId(dept.getId());
		deptVo.setType("task");
		deptVo.setLabel(dept.getName());
		deptVo.setCheckName(dept.getId());
		deptVo.setLeaf(false);
		if (StringUtils.isBlank(dept.getDeptFullPath())) {
			deptVo.setDeptFullPath("o=" + dept.getId());
		} else {
			deptVo.setDeptFullPath(dept.getDeptFullPath());
		}
		if (StringUtils.isBlank(deptVo.getDeptFullPath())) {
			deptVo.setIo("/rest/wtks/root/childrenofwk?roleid="+roleId);
		} else {
			deptVo.setIo("/rest/wtks/" + deptVo.getDeptFullPath()
					+ "/childrenofwk?roleid="+roleId);
		}
		deptVo.setKind("dept");

		return deptVo;
	}

	public static List<DepartmentVo> fillPropertiesToVo(
			List<Department> deptList, String type) {
		List<DepartmentVo> deptVoList = new ArrayList<DepartmentVo>(
				deptList.size());
		DepartmentVo deptVo = null;
		for (Department dept : deptList) {
			deptVo = DepartmentService.fillPropertiesToVo(dept);
			deptVo.setId(dept.getId());
			deptVo.setIo(deptVo.getIo() + "?type=task&isFilter=true");
			deptVo.setType(type);
			deptVoList.add(deptVo);
		}
		return deptVoList;
	}

	public static List<DepartmentVo> fillPropertiesToVowk(
			List<Department> deptList, String type,String roleId) {
		List<DepartmentVo> deptVoList = new ArrayList<DepartmentVo>(
				deptList.size());
		DepartmentVo deptVo = null;
		for (Department dept : deptList) {
			deptVo = DepartmentService.fillPropertiesToVoWk(dept,roleId);
			deptVo.setId(dept.getId());
			deptVo.setIo(deptVo.getIo() + "?type=task&isFilter=true");
			deptVo.setType(type);
			deptVoList.add(deptVo);
		}
		return deptVoList;
	}

	@GET
	@Path("second_level_dept_role/{isLeaf}")
	@Produces("application/json")
	public List<DepartmentVo> getSecondLevelDepartmentAndRole(
			@QueryParam("isFilter") String isFilter, @PathParam("isLeaf") Boolean isLeaf)
			throws UserPersistException {
		List<Department> deptList = null;
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		deptList = deptMgr.getChildrenById("o=广州局");
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		List<DepartmentVo> deptVoList = DepartmentService
				.fillPropertiesToVoAndRoles(deptList, isLeaf);
		return deptVoList;
	}

	public static List<DepartmentVo> fillPropertiesToVoAndRoles(
			List<Department> deptList, Boolean isLeaf) {
		List<DepartmentVo> deptVoList = new ArrayList<DepartmentVo>(
				deptList.size());
		DepartmentVo deptVo = null;
		for (Department dept : deptList) {
			deptVo = DepartmentService.fillPropertiesToVoAndRole(dept, isLeaf);
			deptVoList.add(deptVo);
		}
		return deptVoList;
	}

	public static DepartmentVo fillPropertiesToVoAndRole(Department dept, Boolean isLeaf) {
		DepartmentVo deptVo = new DepartmentVo();
		deptVo.setType("io");
		deptVo.setLabel(dept.getName());
		deptVo.setId(dept.getDeptFullPath());
		deptVo.setCheckName(dept.getId());
		deptVo.setLeaf(false);
		if (StringUtils.isNotBlank(dept.getDeptFullPath())) {
			deptVo.setIo("/rest/roles/dept_and_role/" + dept.getDeptFullPath() + "/" + isLeaf);
		}
		deptVo.setKind("dept");
		return deptVo;
	}

	public static List<DepartmentVo> fillPropertiesToVo(
			List<Department> deptList) {
		List<DepartmentVo> deptVoList = new ArrayList<DepartmentVo>(
				deptList.size());
		DepartmentVo deptVo = null;
		for (Department dept : deptList) {
			deptVo = DepartmentService.fillPropertiesToVo(dept);
			deptVoList.add(deptVo);
		}
		return deptVoList;
	}

	public static List<DepartmentVo> fillPropertiesToVowk(
			List<Department> deptList,String roleId) {
		logger.debug("进入了fillDepartmentListPropertiesToVoByRoleId ");
		List<DepartmentVo> deptVoList = new ArrayList<DepartmentVo>(
				deptList.size());
		DepartmentVo deptVo = null;
		for (Department dept : deptList) {
			deptVo = DepartmentService.fillPropertiesToVoWk(dept,roleId);
			deptVoList.add(deptVo);
		}
		return deptVoList;
	}

	@GET
	@Path("root_and_second_level_dept")
	@Produces("application/json")
	public List<Department> getRootAndSecondLevelDepartment(
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		List<Department> deptList = null;
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		deptList = deptMgr.getRootAndSecondLevelDepartment();
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		return deptList;
	}

	@GET
	@Path("root_and_second_level_dept_vo")
	@Produces("application/json")
	public List<DepartmentVo> getRootAndSecondLevelDepartmentVo(
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		List<Department> deptList = null;
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		deptList = deptMgr.getRootAndSecondLevelDepartment();
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		List<DepartmentVo> deptVoList = DepartmentService
				.fillPropertiesToVo(deptList);
		for (DepartmentVo deptVo : deptVoList) {
			deptVo.setIo("");
		}
		return deptVoList;
	}

	@GET
	@Path("second_level_dept")
	@Produces("application/json")
	public List<Department> getSecondLevelDepartment(
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		List<Department> deptList = null;
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		deptList = deptMgr.getChildrenById("o=广州局");
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}

		return deptList;
	}

	@GET
	@Path("/children/{id}")
	@Produces("application/json")
	public List<OrganizationNodeVo> getChindren(@PathParam("id") String id,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		List<Department> deptList = null;
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		deptList = deptMgr.getChildrenById(id);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		return this.mergeDepartmentVoAndUserVo(
				fillPropertiesToVo(deptList, "task"), new ArrayList<UserVo>());
	}

	public List<Department> searchByCondition(String condition)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		List<Department> deptList = deptMgr.search(condition);

		return deptList;
	}

	@GET
	@Path("/site_dept")
	@Produces("application/json")
	public List<OrganizationNodeVo> getSiteDepartment(
			@QueryParam("condition") String condition)
			throws UserPersistException {
		List<Department> deptList = this.searchByCondition("");
		List<String> siteDeptList = new ArrayList<String>();
		siteDeptList.add("±500kV广州换流站");
		siteDeptList.add("±500kV宝安换流站");
		siteDeptList.add("500kV福山变电站");
		siteDeptList.add("±500kV肇庆换流站");
		siteDeptList.add("500kV花都变电站");
		siteDeptList.add("±800kV穗东换流站");
		List<DepartmentVo> deptVoList = fillPropertiesToVo(deptList, "task");
		List<DepartmentVo> removeDeptVoList = new ArrayList<DepartmentVo>();
		for (DepartmentVo deptVo : deptVoList) {
			deptVo.setIo("");
			logger.info("department name is {}", deptVo.getId());
			if (!siteDeptList.contains(deptVo.getId())) {
				removeDeptVoList.add(deptVo);
			}
		}
		deptVoList.removeAll(removeDeptVoList);
		return this.mergeDepartmentVoAndUserVo(deptVoList,
				new ArrayList<UserVo>());
	}
	
	/**
	 * 适用站点
	 * 
	 * @param condition
	 * @return
	 * @throws UserPersistException
	 */
	@GET
	@Path("/all_site_dept")
	@Produces("application/json")
	public List<DepartmentVo> getAllSiteDepartment(
			@QueryParam("condition") String condition)
			throws UserPersistException {
		List<String> siteDeptList = LdapDepartmentManager.SITE_DEPT_LIST;
		List<DepartmentVo> deptList = new ArrayList<DepartmentVo>();
		for(String deptName : siteDeptList) {
			DepartmentVo department = new DepartmentVo();
			department.setLabel(deptName);
			department.setIo("");
			department.setLeaf(true);
			department.setType("task");
			department.setKind("dept");
			department.setName(deptName);
			department.setId(deptName);
			department.setCheckName(deptName);
			deptList.add(department);
		}
		return deptList;
	}

	/**
	 * 适用班组
	 * 
	 * @return
	 * @throws UserPersistException
	 */
	@GET
	@Path("/site_team/{id}")
	@Produces("application/json")
	public List<OrganizationNodeVo> getSuitTeam(
			@Context HttpServletRequest request, @PathParam("id") String id,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		List<Department> deptList = null;
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		deptList = deptMgr.getChildrenById(id);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		List<DepartmentVo> deptVoList = fillPropertiesToVo(deptList, "task");
		List<DepartmentVo> removeDeptVoList = new ArrayList<DepartmentVo>();
		for (DepartmentVo departmentVo : deptVoList) {
			departmentVo.setIo("");
			logger.info("department name is {}", departmentVo.getId());
			if ("物资管理人员".equals(departmentVo.getId())) {
				removeDeptVoList.add(departmentVo);
			}
		}
		deptVoList.removeAll(removeDeptVoList);
		return this.mergeDepartmentVoAndUserVo(deptVoList,
				new ArrayList<UserVo>());
	}

	/**
	 * 消缺班组
	 * 
	 * @param userId
	 * @return
	 * @throws UserPersistException
	 */
	@GET
	@Path("eliminating_team")
	@Produces("application/json")
	public List<Department> getDepartmentListByUserId(
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		List<Department> deptList = null;
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		UserManager userManager = new LdapUserManager(this.getLdapService());
		String currentUser = this.getSecurityService().getCurrentUser();
		List<User> userList = userManager.findByName(currentUser);
		User user = null;
		if (userList != null && userList.size() > 0) {
			user = userList.get(0);
			if (user != null
					&& StringUtils.isNotBlank(user.getDepartmentName())) {
				String departmentName = user.getDepartmentName();
				if (departmentName.indexOf(",") != -1) {
					departmentName = StringUtils.substring(departmentName,
							departmentName.indexOf(",") + 1,
							departmentName.length());
				}
				deptList = deptMgr.getDepartmentListByUserId(departmentName);
				if ("true".equalsIgnoreCase(isFilter)) {
					this.filterDeptList(deptList);
				}
			}
		}

		return deptList;
	}

	@GET
	@Path("/get_children/{id}")
	@Produces("application/json")
	public List<Department> getChindrenById(@PathParam("id") String id,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		List<Department> deptList = null;
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		deptList = deptMgr.getChildrenById(id);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}

		return deptList;
	}

	@GET
	@Path("/{id}/children/{roleId}")
	@Produces("application/json")
	public List<OrganizationNodeVo> getChildrenNodesByDepartmentIdAndRoleId(
			@Context HttpServletRequest request, @PathParam("id") String id,
			@PathParam("roleId") String roleId,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		RoleManager roleMgr = new DefaultRoleManager(
				this.getPersistenceService());
		Role role = roleMgr.find(roleId);
		Set<String> roleByUser = new HashSet<String>();
		logger.debug("this user by role is :　{}", role.getSubjects());
		if (role != null) {
			roleByUser = role.getSubjects();
		}
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		List<DepartmentVo> deptVoList = null;
		List<UserVo> userVoList = null;
		List<Department> deptList = null;
		String type = request.getParameter("type");

		if ("task".equals(type)) {
			deptList = deptMgr.getChildrenById(id);
			deptVoList = DepartmentService
					.fillDepartmentListPropertiesToVoByRoleId(deptList, type,
							roleId);
		} else {
			deptList = deptMgr.getChildrenById(id);
			deptVoList = DepartmentService.fillPropertiesToVo(deptList);
		}
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		userVoList = UserService.fillUserListPropertiesToVo(userMgr
				.findByDepartmentId(id));
		List<OrganizationNodeVo> orgList = this
				.mergeDepartmentVoAndUserVoCheckUser(deptVoList, userVoList,
						roleByUser);

		return orgList;
	}

	private List<OrganizationNodeVo> mergeDepartmentVoAndUserVoCheckUser(
			List<DepartmentVo> deptVoList, List<UserVo> userVoList,
			Set<String> userId) {
		List<OrganizationNodeVo> orgNodeVoList = new ArrayList<OrganizationNodeVo>();
		for (DepartmentVo deptVo : deptVoList) {
			OrganizationNodeVo orgNodeVo = new OrganizationNodeVo();
			orgNodeVo.setId(deptVo.getId());
			orgNodeVo.setCheckName(deptVo.getCheckName());
			orgNodeVo.setIo(deptVo.getIo());
			orgNodeVo.setLabel(deptVo.getLabel());
			orgNodeVo.setType(deptVo.getType());
			orgNodeVo.setFullPath(deptVo.getId());
			orgNodeVo.setKind(deptVo.getKind());
			orgNodeVoList.add(orgNodeVo);
		}

		for (UserVo userVo : userVoList) {
			System.out.println("*this userVo is :" + userVo.getId());
			OrganizationNodeVo orgNodeVo = new OrganizationNodeVo();
			orgNodeVo.setId(userVo.getCheckName());
			orgNodeVo.setCheckName(userVo.getCheckName());
			orgNodeVo.setIo(userVo.getId());
			for (String id : userId) {
				System.out.println("this userId is : " + id);
				if (id.equals(userVo.getId())) {
					orgNodeVo.setChecked(true);
					break;
				}
			}
			orgNodeVo.setLabel(userVo.getLabel());
			orgNodeVo.setType("task");
			orgNodeVo.setLeaf(userVo.isLeaf());
			orgNodeVo.setFullPath("uid=" + userVo.getId() + ","
					+ userVo.getDeptFullPath());
			orgNodeVo.setKind(userVo.getKind());
			orgNodeVoList.add(orgNodeVo);
		}
		return orgNodeVoList;
	}

	public static DepartmentVo fillDepartmentPropertiesToVoByRole(
			Department dept, String roleId) {
		DepartmentVo deptVo = new DepartmentVo();
		deptVo.setId(dept.getId());
		deptVo.setType("io");
		deptVo.setLabel(dept.getName());
		deptVo.setCheckName(dept.getId());
		deptVo.setLeaf(false);
		// if (StringUtils.isBlank(dept.getParent())) {
		// deptVo.setDeptFullPath("o=" + dept.getId());
		// } else {
		// deptVo.setDeptFullPath("ou=" + dept.getId() + "," +
		// dept.getParent());
		// }
		// if (StringUtils.isBlank(deptVo.getDeptFullPath())) {
		// deptVo.setIo("/rest/depts/root/children");
		// } else {
		// deptVo.setIo("/rest/depts/" + deptVo.getDeptFullPath() +
		// "/children/"+roleId);
		// }
		if (StringUtils.isBlank(dept.getDeptFullPath())) {
			deptVo.setDeptFullPath("o=" + dept.getId());
		} else {
			deptVo.setDeptFullPath(dept.getDeptFullPath());
		}
		if (StringUtils.isBlank(deptVo.getDeptFullPath())) {
			deptVo.setIo("/rest/depts/root/children");
		} else {
			deptVo.setIo("/rest/depts/" + deptVo.getDeptFullPath()
					+ "/children/" + roleId);
		}

		deptVo.setKind("dept");
		return deptVo;
	}

	public static DepartmentVo fillDepartmentPropertiesToVoByRolewk(
			Department dept, String roleId) {
		logger.debug("进入了fillDepartmentPropertiesToVoByRolewk");
		DepartmentVo deptVo = new DepartmentVo();
		deptVo.setId(dept.getId());
		deptVo.setType("task");
		deptVo.setLabel(dept.getName());
		deptVo.setCheckName(dept.getId());
		deptVo.setLeaf(false);

		if (StringUtils.isBlank(dept.getDeptFullPath())) {
			deptVo.setDeptFullPath("o=" + dept.getId());
		} else {
			deptVo.setDeptFullPath(dept.getDeptFullPath());
		}
		if (StringUtils.isBlank(deptVo.getDeptFullPath())) {
			deptVo.setIo("/rest/wtks/root/childrenofwk");
		} else {
			deptVo.setIo("/rest/wtks/" + deptVo.getDeptFullPath()
					+ "/childrenofwk/" + roleId);
		}
		deptVo.setKind("dept");
		return deptVo;
	}

	public static List<DepartmentVo> fillDepartmentListPropertiesToVoByRoleId(
			List<Department> deptList, String type, String roleId) {
		List<DepartmentVo> deptVoList = new ArrayList<DepartmentVo>(
				deptList.size());
		DepartmentVo deptVo = null;
		for (Department dept : deptList) {
			deptVo = DepartmentService.fillDepartmentPropertiesToVoByRole(dept,
					roleId);
			deptVo.setIo(deptVo.getIo() + "?type=task");
			deptVo.setType(type);
			deptVoList.add(deptVo);
		}
		return deptVoList;
	}

	public static List<DepartmentVo> fillDepartmentListPropertiesToVoByRoleIdwk(
			List<Department> deptList, String type, String roleId) {
		logger.debug("进入了fillDepartmentListPropertiesToVoByRoleIdwk");
		List<DepartmentVo> deptVoList = new ArrayList<DepartmentVo>(
				deptList.size());
		DepartmentVo deptVo = null;
		for (Department dept : deptList) {
			deptVo = DepartmentService.fillDepartmentPropertiesToVoByRolewk(
					dept, roleId);
			deptVo.setIo(deptVo.getIo() + "?type=task");
			deptVo.setType(type);
			deptVoList.add(deptVo);
		}
		return deptVoList;
	}

	@GET
	@Path("/{id}/children/for_user_combobox_dept_not_combobox")
	@Produces("application/json")
	public List<OrganizationNodeVo> getChildrenByIdForUserComboboxDeptNotCombobox(
			@Context HttpServletRequest request, @PathParam("id") String id,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();

		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		List<DepartmentVo> deptVoList = null;
		List<UserVo> userVoList = null;
		String type = request.getParameter("type");
		List<Department> deptList = deptMgr.getChildrenById(id);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		deptVoList = DepartmentService.fillPropertiesToVo(deptList);
		for (DepartmentVo deptVo : deptVoList) {
			deptVo.setIo("/rest/depts/" + deptVo.getDeptFullPath()
					+ "/children/for_user_combobox_dept_not_combobox?type=task");
		}
		userVoList = UserService.fillUserListPropertiesToVo(
				userMgr.findByDepartmentId(id), type);
		List<OrganizationNodeVo> orgList = this.mergeDepartmentVoAndUserVo(
				deptVoList, userVoList);

		return orgList;
	}

	@GET
	@Path("/{id}/children/for_dept_combobox_user_not_combobox")
	@Produces("application/json")
	public List<OrganizationNodeVo> getChildrenByIdForDeptComboboxUserNotCombobox(
			@Context HttpServletRequest request, @PathParam("id") String id,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();

		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		List<DepartmentVo> deptVoList = null;
		List<UserVo> userVoList = null;
		String type = request.getParameter("type");
		List<Department> deptList = deptMgr.getChildrenById(id);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		deptVoList = DepartmentService.fillPropertiesToVo(deptList, type);
		for (DepartmentVo deptVo : deptVoList) {
			deptVo.setIo("/rest/depts/" + deptVo.getDeptFullPath()
					+ "/children/for_dept_combobox_user_not_combobox?type=task");
		}
		userVoList = UserService.fillUserListPropertiesToVo(userMgr
				.findByDepartmentId(id));
		List<OrganizationNodeVo> orgList = this.mergeDepartmentVoAndUserVo(
				deptVoList, userVoList);

		return orgList;
	}

	@GET
	@Path("/{id}/children/for_user_combobox_dept_not_combobox")
	@Produces("application/json")
	public List<OrganizationNodeVo> getChildrenByIdForUserComboboxDeptNotCombobox(
			@PathParam("id") String id, @QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();

		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		List<DepartmentVo> deptVoList = null;
		List<UserVo> userVoList = null;
		List<Department> deptList = deptMgr.getChildrenById(id);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		deptVoList = DepartmentService.fillPropertiesToVo(deptList);
		userVoList = UserService.fillUserListPropertiesToVo(
				userMgr.findByDepartmentId(id), "task");
		List<OrganizationNodeVo> orgList = this.mergeDepartmentVoAndUserVo(
				deptVoList, userVoList);

		return orgList;
	}

	@GET
	@Path("/{id}/children/for_dept_combobox_user_not_combobox")
	@Produces("application/json")
	public List<OrganizationNodeVo> getChildrenByIdForDeptComboboxUserNotCombobox(
			@PathParam("id") String id, @QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();

		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		LdapUserManager userMgr = new LdapUserManager(ldapSvc);
		List<DepartmentVo> deptVoList = null;
		List<UserVo> userVoList = null;
		List<Department> deptList = deptMgr.getChildrenById(id);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		deptVoList = DepartmentService.fillPropertiesToVo(deptList, "task");
		userVoList = UserService.fillUserListPropertiesToVo(userMgr
				.findByDepartmentId(id));
		List<OrganizationNodeVo> orgList = this.mergeDepartmentVoAndUserVo(
				deptVoList, userVoList);

		return orgList;
	}

	@GET
	@Path("/dept_tree/{id}")
	@Produces("application/json")
	public List<OrganizationNodeVo> getDeptTree(@PathParam("id") String id,
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		List<DepartmentVo> deptVoList = null;
		List<Department> deptList = deptMgr.getChildrenById(id);
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		deptVoList = DepartmentService.fillPropertiesToVo(deptList);
		for (DepartmentVo deptVo : deptVoList) {
			deptVo.setIo("/rest/depts/dept_tree/" + deptVo.getDeptFullPath());
			deptVo.setType("check");
		}
		List<OrganizationNodeVo> orgList = this.mergeDepartmentVoAndUserVo(
				deptVoList, new ArrayList<UserVo>());
		return orgList;
	}

	private void filterDeptList(List<Department> deptList) {
		List<Department> removeDepts = new ArrayList<Department>();
		for (Department dept : deptList) {
			if (StringUtils.isNotBlank(dept.getDeptFullPath())) {
				if (dept.getDeptFullPath().contains("ou=四维度管理小组,o=广州局")) {
					removeDepts.add(dept);
				} else if (dept.getDeptFullPath().contains("ou=试题库管理小组,o=广州局")) {
					removeDepts.add(dept);
				} else if (dept.getDeptFullPath().contains("ou=表单化管理小组,o=广州局")) {
					removeDepts.add(dept);
				} else if (dept.getDeptFullPath().contains("ou=系统管理员,o=广州局")) {
					removeDepts.add(dept);
				}
			}
		}
		deptList.removeAll(removeDepts);
	}

	@GET
	@Path("root_and_second_level_dept_vo_with_task")
	@Produces("application/json")
	public List<DepartmentVo> getRootAndSecondLevelDepartmentVoWithTask(
			@QueryParam("isFilter") String isFilter)
			throws UserPersistException {
		List<Department> deptList = null;
		LdapService ldapSvc = this.getLdapService();
		LdapDepartmentManager deptMgr = new LdapDepartmentManager(ldapSvc);
		deptList = deptMgr.getRootAndSecondLevelDepartment();
		if ("true".equalsIgnoreCase(isFilter)) {
			this.filterDeptList(deptList);
		}
		List<DepartmentVo> deptVoList = DepartmentService
				.fillPropertiesToVoWithTask(deptList);
		for (DepartmentVo deptVo : deptVoList) {
			deptVo.setIo("");
		}
		return deptVoList;
	}

	public static List<DepartmentVo> fillPropertiesToVoWithTask(
			List<Department> deptList) {
		List<DepartmentVo> deptVoList = new ArrayList<DepartmentVo>(
				deptList.size());
		DepartmentVo deptVo = null;
		for (Department dept : deptList) {
			deptVo = DepartmentService.fillPropertiesToVoWithTask(dept);
			deptVoList.add(deptVo);
		}
		return deptVoList;
	}

	public static DepartmentVo fillPropertiesToVoWithTask(Department dept) {
		DepartmentVo deptVo = new DepartmentVo();

		deptVo.setId(dept.getId());
		deptVo.setType("task");
		deptVo.setLabel(dept.getName());
		deptVo.setCheckName(dept.getId());
		deptVo.setLeaf(false);
		if (StringUtils.isBlank(dept.getDeptFullPath())) {
			deptVo.setDeptFullPath("o=" + dept.getId());
		} else {
			deptVo.setDeptFullPath(dept.getDeptFullPath());
		}
		if (StringUtils.isBlank(deptVo.getDeptFullPath())) {
			deptVo.setIo("/rest/depts/root/children");
		} else {
			deptVo.setIo("/rest/depts/" + deptVo.getDeptFullPath()
					+ "/children");
		}
		deptVo.setKind("dept");
		return deptVo;
	}
}
