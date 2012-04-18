package com.zyeeda.framework.managers;

import java.util.List;
import java.util.Set;

import com.googlecode.genericdao.dao.jpa.GenericDAO;
import com.zyeeda.framework.entities.Role;
import com.zyeeda.framework.entities.TheThirdRoleAndUser;
import com.zyeeda.framework.viewmodels.RoleVo;
import com.zyeeda.framework.viewmodels.ShiftUserVo;
import com.zyeeda.framework.viewmodels.UserVo;

public interface RoleManager extends GenericDAO<Role, String> {

		public  List<Role> getRoleBySubject(String subject);
		
		public List<Role> getRoleDistinct(String hql);
		
		public List<RoleVo> roleToVo(List<Role> listRole, Boolean isLeaf);
		
		public List<UserVo> getUserVoByRole(Role role) throws UserPersistException;
		
		public List<RoleVo> deptToVo(List<Role> listRole);
		
		public Set<String> getListMenuAuth(List<Role> roles);
		
		public List<Role> findRoleBySubStationName(String subStationName);
		
		public Set<ShiftUserVo> getShiftUserVoByRole(Role role) throws UserPersistException;
		
		public List<ShiftUserVo> getAllShiftUserVoByRole(Role role)  throws UserPersistException;
		
		public String getUserVoByFlowState(String flowState);
		
		public String getUserVoByFlowState(String flowState, String deptment);
		
		public  List<String> getRoleNameBySubject(String subject);
		
		public Set<TheThirdRoleAndUser> getRoleByRoleName(String RoleName);
		
		public Set<TheThirdRoleAndUser> getRoleByLikeRoleName(String RoleName);
			
		public List<Object[]> getBySql(String sql, String[] params);
		
		public Role findRoleByName(String roleName);
		
		public Set<String> getSubjectByRoleName(String[] roles);
		
		public List<UserVo> findSubjectByLikeRoleName(String roleName) throws UserPersistException;
		
		public Set<String> findSubjectsByLikeRoleName(String roleName) throws UserPersistException;
		
		public String getTransDept(String subject);
		
		public String appendUserNameByRoleName(String[] roleName) throws UserPersistException;
		
		public String appendUserNameByRoleName(String roleName) throws UserPersistException;
		
		public Boolean isHadRoleName(String subject, String roleName);
		
		public Set<String> findSubjectsByLikeRoleName(String roleName, String deptName);
		
		public String userNameToUserCn (List<String> userNameSet) throws UserPersistException;
		
		public String userNameToUserCn (Set<String> userNameSet) throws UserPersistException;
		
}
