package com.zyeeda.framework.ws;

import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.genericdao.search.Search;
import com.zyeeda.framework.entities.Role;
import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.managers.RoleManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.managers.internal.DefaultRoleManager;
import com.zyeeda.framework.managers.internal.DefaultUserManager;
import com.zyeeda.framework.ws.base.ResourceService;

@Path("/sync")
public class DefaultUserService extends ResourceService {

	private Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@POST
	@Path("/persist")
	@Produces("application/json")
	public User createUser(@FormParam("") User user) throws UserPersistException {
		DefaultUserManager userMgr = new DefaultUserManager(this.getPersistenceService());
		if (userMgr.findById(user.getId()) != null) {
			throw new RuntimeException("账号不能重复");
		} else {
			userMgr.persist(user);
			return userMgr.findById(user.getId());
		}
	}

	@PUT
	@Path("/update")
	@Produces("application/json")
	public User editUser(@FormParam("") User user) throws UserPersistException {
		DefaultUserManager userMg = new DefaultUserManager(this
				.getPersistenceService());

		User u = userMg.find(user.getId());
		logger.info("this user id is {}", user.getId());
		if (u != null) {
			u.setUsername(user.getUsername());
			u.setEmail(user.getEmail());
			u.setBirthday(user.getBirthday());
			u.setDateOfWork(user.getDateOfWork());
			u.setDegree(user.getDegree());
			u.setDepartmentName(user.getDepartmentName());
			u.setDeptFullPath(user.getDeptFullPath());
			u.setGender(user.getGender());
//			u.setPassword(user.getPassword());
			u.setMobile(user.getMobile());
			u.setPosition(user.getPosition());
			u.setPostStatus(user.getPostStatus());
			u.setStatus(user.getStatus());
			userMg.update(u);
			return u;
		} else {
			userMg.persist(user);
			return user;
		}
	}

	@PUT
	@Path("/editPassword")
	public User editPassword(@QueryParam("id") String id,
			@QueryParam("formerlyPassword") String formerlyPassword,
			@QueryParam("nowPasswrd") String nowPasswrd)
	 			throws UserPersistException  {
		DefaultUserManager userMg = new DefaultUserManager(this
				.getPersistenceService());
		User user = userMg.findById(id);
		if (user.getPassword().equals(formerlyPassword)) {
			if (!nowPasswrd.equals(user.getPassword())) {
				userMg.updatePassword(id, nowPasswrd);
			}
		} else {
			throw new RuntimeException("旧密码输入错误");
		}
		return null;
	}
	
	@PUT
	@Path("/editAssignPassword")
	@Produces("application/json")
	public User editAssignPassword(@FormParam("id") String id,
			@FormParam("assignPassword") String assignPassword)
	 			throws UserPersistException  {
		DefaultUserManager userMg = new DefaultUserManager(this
				.getPersistenceService());
		userMg.updateAssignPassword(id, assignPassword);
		return userMg.findById(id);
	}

	@PUT
	@Path("/enable")
	@Produces("application/json")
	public User enable(@PathParam("id") String id) throws UserPersistException {
		DefaultUserManager userMg = new DefaultUserManager(this.getPersistenceService());
		userMg.enable(id);
		return userMg.findById(id);
	}

	@PUT
	@Path("/{id}/disable")
	@Produces("application/json")
	public User disable(@PathParam("id") String id) throws UserPersistException {
		DefaultUserManager userMg = new DefaultUserManager(this.getPersistenceService());
		userMg.enable(id);
		return userMg.findById(id);
	}

	
	@PUT
	@Path("/updateDeptPath")
	@Produces("application/json")
	public void updateDeptPath(@FormParam("oldDeptPath") String oldDeptPath, @FormParam("newDeptPath") String newDeptPath) {
		logger.debug("oldDeptPath ：{} ; newDeptPath : {}", oldDeptPath, newDeptPath);
		String tmp = null;
		RoleManager roleMgr = new DefaultRoleManager(this.getPersistenceService());
		Search roleSearch = new Search(Role.class);
		roleSearch.addFilterLike("deptepmentId", "%" + oldDeptPath);
		List<Role> roles = roleMgr.search(roleSearch);
		String sDept = newDeptPath.substring(3, newDeptPath.indexOf(","));
		logger.debug("新部门名称:{}", tmp);
		if(roles != null && roles.size() > 0) {
			for(Role r : roles) {
				if(oldDeptPath.indexOf(r.getDeptepment()) != -1) {
					r.setDeptepment(sDept);
				}
				tmp = r.getDeptepmentId().substring(0, r.getDeptepmentId().indexOf(oldDeptPath)) + newDeptPath;
				r.setDeptepmentId(tmp);
				roleMgr.merge(r);
			}
		}
		
		DefaultUserManager userMgr = new DefaultUserManager(this.getPersistenceService());
		Search userSearch = new Search(User.class);
		userSearch.addFilterLike("deptFullPath", "%" + oldDeptPath);
		List<User> users = userMgr.search(userSearch);
		if(users != null && users.size() > 0) {
			for(User u : users) {
				tmp = u.getDeptFullPath().substring(0, u.getDeptFullPath().indexOf(oldDeptPath)) + newDeptPath;
				u.setDeptFullPath(tmp);
				u.setDepartmentName(tmp);
				userMgr.merge(u);
			}
		}
		this.getPersistenceService().getCurrentSession().flush();
	}
	
	@PUT
	@Path("/remove")
	@Produces("application/json")
	public void removeUserById(@FormParam("id") String id)
	 			throws UserPersistException  {
		DefaultUserManager userMg = new DefaultUserManager(this
				.getPersistenceService());
		userMg.remove(id);
	}
	
	
	public static void main(String[] args) {
		String newPath = "ou=222,ou=±500kV肇庆换流站,o=广州局";
		String oldPath = "ou=400,ou=±500kV肇庆换流站,o=广州局";
		String uPath = "uid=lihaowei,ou=400,ou=±500kV肇庆换流站,o=广州局";
		String tmp = uPath.substring(0, uPath.indexOf(oldPath)) + newPath;
		System.out.println(tmp);
	}
}
