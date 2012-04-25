package com.zyeeda.framework.managers.internal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.genericdao.search.Search;
import com.zyeeda.framework.entities.Role;
import com.zyeeda.framework.entities.TheThirdRoleAndUser;
import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.managers.RoleManager;
import com.zyeeda.framework.managers.TheThirdUserAndRoleManager;
import com.zyeeda.framework.managers.UserManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.managers.base.DomainEntityManager;
import com.zyeeda.framework.persistence.PersistenceService;
import com.zyeeda.framework.viewmodels.RoleVo;
import com.zyeeda.framework.viewmodels.ShiftUserVo;
import com.zyeeda.framework.viewmodels.UserVo;

public class DefaultRoleManager extends DomainEntityManager<Role, String> implements RoleManager {
	private static final Logger logger = LoggerFactory.getLogger(DefaultRoleManager.class);

	public DefaultRoleManager(PersistenceService persistenceSvc) {
		super(persistenceSvc);
	}
	
	public Role findRoleByName(String roleName) {
		Search search = new Search();
		search.addFilterEqual("name", roleName);
		return (Role)this.search(search).get(0);
	}
	
	public Set<String> getSubjectByRoleName(String[] roles) {
		Search search = new Search();
		search.addFilterIn("name", CollectionUtils.asList(roles));
		List<Role> roleList = this.search(search);
		Set<String> roleSubject = new HashSet<String>();
		for(Role role : roleList) {
			roleSubject.addAll(role.getSubjects());
		}
		return roleSubject;
	}
	
	public Set<String> getListAuth(List<Role> roles) {
		Set<String> auths = new HashSet<String>();
		for(Role role : roles) {
			auths.addAll(role.getRoamPermissionList());
		}
		return auths;
	}

	public Set<String> getListMenuAuth(List<Role> roles) {
		Set<String> auths = new HashSet<String>();
		for(Role role : roles) {
			auths.addAll(role.getPermissionsList());
		}
		return auths;
	}
	
	public  List<Role> getRoleBySubject(String subject){
		logger.debug("the value of the dept subject is = {}  ", subject);
		//EntityManager session = (EntityManager)this.getPersistenceService().getCurrentSession();
		TypedQuery<Role> query = this.em().createNamedQuery("getRolesBySubject", Role.class);
		query.setParameter("subject", subject);
		List<Role> roleList = query.getResultList();
		return roleList;
	}
	
	@SuppressWarnings("unchecked")
    public  List<String> getRoleNameBySubject(String subject){
		logger.debug("the value of the dept subject is = {}  ", subject);
		String sql = " SELECT  F_NAME FROM SYS_ROLE r LEFT JOIN SYS_SUBJECT s ON s.F_ROLE_ID = r.F_ID  WHERE  s.F_SUBJECT = ?";
		//EntityManager session = (EntityManager)this.getPersistenceService().getCurrentSession();
		List<String> query =  this.em().createNativeQuery(sql).setParameter(1, subject).getResultList();
		return query;
	}
	
	public List<Role> findRoleBySubStationName(String subStationName) {
		TypedQuery<Role> query = this.em().createNamedQuery("FindSquadLeader", Role.class);
		query.setParameter("subStationName", subStationName);
		return query.getResultList();
	}


	
	@SuppressWarnings("unchecked")
	public List<Role> getRoleDistinct(String hql) {
		List<Role> list = new ArrayList<Role>();
		TypedQuery<Role> createNativeQuery = (TypedQuery<Role>) this.em().createNativeQuery(hql);
		TypedQuery<Role> query = createNativeQuery;
		 list = query.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> getBySql(String sql, String[] params) {
		Query query = this.em().createNativeQuery(sql);
		for (int i = 0; i < params.length; i++) {
			query.setParameter(1+i, params[i]);
		}
		List<Object[]> list = query.getResultList();
		return list;
	}
	
//	public List<RoleVo> roleToVo(List<Role> listRole) {
//		List<RoleVo> listRoleVo = new ArrayList<RoleVo>();
//		for(Role role : listRole) {
//			RoleVo roleVo = new RoleVo();
//			if(role.getDeptepment() == null && role.getDeptepmentId() == null){
//				roleVo.setCheckName(role.getName());
//				roleVo.setLabel(role.getName());
//				roleVo.setId(role.getId());
//				roleVo.setLeaf(true);
//				roleVo.setType("tesk");
//				roleVo.setKind("user");
//			} else {
//				roleVo.setId(role.getDeptepmentId());
//				roleVo.setCheckName(role.getDeptepment());
//				roleVo.setLabel(role.getDeptepment());
//				roleVo.setIo("/rest/line_location/"+role.getDeptepmentId());
//				roleVo.setLeaf(false);
//				roleVo.setType("io");
//			}
//			listRoleVo.add(roleVo);
//		}
//		return listRoleVo;
//	}

	
//	@SuppressWarnings("unchecked")
//	public List<Role> getRoleDistinct(String hql){
//		List<Role> list = new ArrayList<Role>();
//		TypedQuery<Role> createNativeQuery = (TypedQuery<Role>) this.em().createNativeQuery(hql);
//		TypedQuery<Role> query = createNativeQuery;
//		 list = query.getResultList();
//		return list;
//	}
	
	public List<RoleVo> deptToVo(List<Role> listRole) {
		List<RoleVo> listRoleVo = new ArrayList<RoleVo>();
		for(Role role : listRole) {
			RoleVo roleVo = new RoleVo();
				roleVo.setId(role.getDeptepmentId());
				roleVo.setCheckName(role.getDeptepment());
				roleVo.setLabel(role.getDeptepment());
				roleVo.setIo("/rest/dept/"+role.getDeptepmentId());
				roleVo.setLeaf(false);
				roleVo.setKind("role");
				roleVo.setType("io");
				listRoleVo.add(roleVo);
		}
		return listRoleVo;
	}

	public List<RoleVo> roleToVo(List<Role> listRole, Boolean isLeaf) {
		List<RoleVo> listRoleVo = new ArrayList<RoleVo>();
		for(Role role : listRole) {
			RoleVo roleVo = new RoleVo();
				roleVo.setCheckName(role.getName());
				roleVo.setLabel(role.getName());
				roleVo.setId(role.getId());
				roleVo.setLeaf(false);
				roleVo.setType("io");
				roleVo.setKind("role");
				roleVo.setIo("/rest/roles/depts/" + role.getId());
				if(isLeaf != null && isLeaf) {
					roleVo.setLeaf(true);
					roleVo.setIo("");
					roleVo.setType("task");
				}
				listRoleVo.add(roleVo);
		}
		return listRoleVo;
	}
	
	public List<UserVo> getUserVoByRole(Role role) throws UserPersistException {
		Set<String> user = role.getSubjects();
		return this.subjectToVo(user);
	}
	
	public List<UserVo> subjectToVo(Set<String> userSbuject) throws UserPersistException {
		List<UserVo>  list = new ArrayList<UserVo>();
		for(String userName : userSbuject) {
			UserManager userMgr = new DefaultUserManager(this.getPersistenceService());
			logger.info("*******this user is :*********" + userName);
			User userPojo = userMgr.findById(userName);
			if(userPojo != null) {
				UserVo userVo = new UserVo();
				userVo.setCheckName(userPojo.getUsername());
				userVo.setLabel(userPojo.getUsername());
				userVo.setType("node");
				userVo.setId(userName);
				userVo.setKind("user");
				userVo.setLeaf(true);
				list.add(userVo);
			}
		}
		return list;
	}
	
	public Set<ShiftUserVo> getShiftUserVoByRole(Role role) throws UserPersistException  {
		Set<String> user = role.getSubjects();
		Set<ShiftUserVo>  set = new HashSet<ShiftUserVo>();
		for(String userName : user) {
			UserManager userMgr = new DefaultUserManager(this.getPersistenceService());
			User userPojo = userMgr.findById(userName);
			if(userPojo != null) {
				ShiftUserVo shiftUserVo = new ShiftUserVo();
				shiftUserVo.setUsername(userPojo.getUsername());
				shiftUserVo.setUserId(userName);
				shiftUserVo.setRoleName(role.getName());
				shiftUserVo.setRoleId(role.getId());
				set.add(shiftUserVo);
			}
		}
		return set;
	}
	
	public List<ShiftUserVo> getAllShiftUserVoByRole(Role role) throws UserPersistException  {
		Set<String> user = role.getSubjects();
		List<ShiftUserVo> list = new ArrayList<ShiftUserVo>();
		for(String userName : user) {
			UserManager userMgr = new DefaultUserManager(this.getPersistenceService());
			User userPojo = userMgr.findById(userName);
			if(userPojo != null) {
				ShiftUserVo shiftUserVo = new ShiftUserVo();
				shiftUserVo.setUsername(userPojo.getUsername());
				shiftUserVo.setUserId(userName);
				shiftUserVo.setRoleName(role.getName());
				shiftUserVo.setRoleId(role.getId());
				list.add(shiftUserVo);
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
    public String getUserVoByFlowState(String flowState) {
		String sql = "select F_USERNAME from ZDA_SYS_USER where F_ID in (select F_SUBJECT from SYS_SUBJECT where F_ROLE_ID in (" +
				"select F_ID from SYS_ROLE where F_PERMISSIONS like ? ))";
		List<String> rs = this.em().createNativeQuery(sql).setParameter(1, "%" + flowState + "%").getResultList();
		try {
			return this.userNameToUserCn(rs);
		} catch (UserPersistException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
    public String getUserVoByFlowState(String flowState, String deptment) {
		String sql = "select F_USERNAME from ZDA_SYS_USER where F_ID in (select F_SUBJECT from SYS_SUBJECT where F_ROLE_ID in (" +
				"select F_ID from SYS_ROLE where F_PERMISSIONS like ? ) and F_DEPTFULLPATH like ? )";
		List<String> rs = this.em().createNativeQuery(sql).setParameter(1, "%" + flowState + "%").setParameter(2, "%" + deptment + "%").getResultList();
		try {
			return this.userNameToUserCn(rs);
		} catch (UserPersistException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Set<TheThirdRoleAndUser> getRoleByRoleName(String roleName) {
		TheThirdUserAndRoleManager roleAndUserMgr = new DefaultTheThirdTableManager(this.getPersistenceService());
		Search search = new Search(Role.class);
		search.addFilterEqual("name", roleName);
		Set<TheThirdRoleAndUser> thirdList = new HashSet<TheThirdRoleAndUser>();
		List<Role> roleList = this.search(search);
		List<String> roleIdList = new ArrayList<String>();
		for(Role role : roleList) {
			roleIdList.add(role.getId());
		}
		Search searchThe = new Search(TheThirdRoleAndUser.class);
		searchThe.addFilterIn("roleId", roleIdList);
		searchThe.addFilterEqual("isPass", true);
		List<TheThirdRoleAndUser> list = roleAndUserMgr.search(searchThe);
		thirdList.addAll(list);
		return thirdList;
	
	}

	@Override
	public Set<TheThirdRoleAndUser> getRoleByLikeRoleName(
			String roleName) {
		TheThirdUserAndRoleManager roleAndUserMgr = new DefaultTheThirdTableManager(this.getPersistenceService());
		Search search = new Search(Role.class);
		search.addFilterLike("name", roleName);
		Set<TheThirdRoleAndUser> thirdList = new HashSet<TheThirdRoleAndUser>();
		List<Role> roleList = this.search(search);
		List<String> roleIdList = new ArrayList<String>();
		for(Role role : roleList) {
			roleIdList.add(role.getId());
		}
		Search searchThe = new Search(TheThirdRoleAndUser.class);
		searchThe.addFilterIn("roleId", roleIdList);
		searchThe.addFilterEqual("isPass", true);
		List<TheThirdRoleAndUser> list = roleAndUserMgr.search(searchThe);
		thirdList.addAll(list);
		return thirdList;
	}
	
	public List<UserVo> findSubjectByLikeRoleName(String roleName) throws UserPersistException {
		if(roleName == null) {
			roleName = null;
		}
		Search search = new Search(Role.class);
		search.addFilterLike("name", "%" + roleName + "%");
		List<Role> roleList = this.search(search);
		return this.subjectToVo(this.getRoleSubject(roleList));
	}
	public Set<String> getRoleSubject(List<Role> roleList) {
		Set<String> subjectSet = new HashSet<String>();
		for(Role role : roleList) {
			subjectSet.addAll(role.getSubjects());
		}
		return subjectSet;
	}
	
	public Set<String> findSubjectsByLikeRoleName(String roleName) throws UserPersistException {
		return this.findSubjectsByLikeRoleName(roleName, null);
	}
	
	public Set<String> findSubjectsByLikeRoleName(String roleName, String deptName) {
		if (roleName == null) {
			roleName = "";
		}
		Search search = new Search(Role.class);
		if (deptName != null) {
			search.addFilterEqual("deptepment", deptName);
		}
		search.addFilterLike("name", "%" + roleName + "%");
		List<Role> roleList = this.search(search);
		return this.getRoleSubject(roleList);
	}
	
	public Set<String> findSubjectsByRoleName(String roleName) throws UserPersistException {
		if(roleName == null) {
			roleName = "";
		}
		Search search = new Search(Role.class);
		search.addFilterLike("name", "%" + roleName + "%");
		List<Role> roleList = this.search(search);
		return this.getRoleSubject(roleList);
	}
	
	public String appendUserNameByRoleName(String[] roleName) throws UserPersistException {
		Set<String> userNameSet = this.getSubjectByRoleName(roleName);
		return this.userNameToUserCn(userNameSet);
	}
	
	public String appendUserNameByRoleName(String roleName) throws UserPersistException {
		Set<String> userNameSet = this.findSubjectsByRoleName(roleName);
		return this.userNameToUserCn(userNameSet);
	}
	
	public Boolean isHadRoleName(String subject, String roleName) {
		List<Role> roleList = this.getRoleBySubject(subject);
		if(roleList.contains(roleName)) {
			return true;
		}
		return false;
	}
	public String userNameToUserCn (List<String> userNameSet) throws UserPersistException {
		String userNames = userNameSet.toString();
		userNames = userNames.replace("[", "");
		userNames = userNames.replace("]", "");
		return userNames;
	}
	
	public String userNameToUserCn (Set<String> userNameSet) throws UserPersistException {
		String userNames = userNameSet.toString();
		userNames = userNames.replace("[", "");
		userNames = userNames.replace("]", "");
		return userNames;
	}
	
	public String getTransDept(String subject) {
		List<Role> roleList = this.getRoleBySubject(subject);
		for (Role role : roleList) {
			String roleName = role.getName();
			if(roleName.contains("输电一班")) {
				return "输电一班";
			} else if(roleName.contains("输电二班")) {
				return "输电二班";
			} else if(roleName.contains("输电三班")) {
				return "输电三班";
			} else if(roleName.contains("输电四班")) {
				return "输电四班";
			} else if(roleName.contains("输电五班")) {
				return "输电五班";
			} else if(roleName.contains("输电六班")) {
				return "输电六班";
			} else if(roleName.contains("输电七班")) {
				return "输电七班";
			} else if(roleName.contains("输电八班")) {
				return "输电八班";
			} 
		}
		return null;
	}
}
