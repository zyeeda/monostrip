package com.zyeeda.framework.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.zyeeda.framework.entities.Role;
import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.managers.MenuManager;
import com.zyeeda.framework.managers.UserManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.managers.internal.DefaultMenuManager;
import com.zyeeda.framework.managers.internal.DefaultRoleManager;
import com.zyeeda.framework.managers.internal.DefaultUserManager;
import com.zyeeda.framework.viewmodels.MenuAndPermission;
import com.zyeeda.framework.viewmodels.MenuVo;
import com.zyeeda.framework.viewmodels.PermissionVo;
import com.zyeeda.framework.ws.base.ResourceService;

@Path("/menu")
public class MenuService extends ResourceService {
	
	private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

	@SuppressWarnings("unchecked")
	@GET
	@Path("/")
	@Produces("application/json")
	public MenuAndPermission getMenu(@Context ServletContext ctx)throws XPathExpressionException, IOException, UserPersistException, ParserConfigurationException, SAXException {
		String user = this.getSecurityService().getCurrentUser();
		Session session = SecurityUtils.getSubject().getSession();
		User userInfo = null;
		if(session.getAttribute("userInfo") == null) {
			UserManager userMgr = new DefaultUserManager(this.getPersistenceService());
			userInfo = userMgr.findById(user);
			 session.setAttribute("userInfo", userInfo);
		} else {
			userInfo = (User) session.getAttribute("userInfo");
		}
		
		MenuManager menuMgr = new DefaultMenuManager();
		DefaultRoleManager roleMgr = new DefaultRoleManager(this
				.getPersistenceService());
		MenuAndPermission roleWithUserVo = new MenuAndPermission();
		if(userInfo != null) {
			roleWithUserVo.setUserName(userInfo.getUsername());
			roleWithUserVo.setDeptName(this.getDeptByUser(user));
			if("输电管理所".equals(this.getDeptByUser(user))) {
				roleWithUserVo.setParentDeptName(roleMgr.getTransDept(user));
			}
		}
		List<MenuVo> listMenu = new ArrayList<MenuVo>();
		List<Role> roles = new ArrayList<Role>();
		roles = roleMgr.getRoleBySubject(user);
		Set<String> authList = roleMgr.getListAuth(roles);
		Map<String, PermissionVo> mapPermission = null;
		if(ctx.getAttribute("authPermission") != null) {
			mapPermission = (Map<String, PermissionVo>) ctx.getAttribute("authPermission");
			for(String auth : authList) {
				if(mapPermission.get(auth) != null) {
					roleWithUserVo.getListPermission().add(mapPermission.get(auth));
				}
			}
		}
		if(session.getAttribute("auth") == null) {
			session.setAttribute("auth", authList);
		}
		if(roles.size() == 1) {
			logger.debug("the value of the dept subject is = {}  ", roles.get(0).getPermissionsList());
			listMenu = menuMgr.getMenuListByPermissionAuth(roles.get(0).getPermissionsList());
			roleWithUserVo.getListMenu().addAll(listMenu);
			return roleWithUserVo;
		}
		Set<String> authMenuSet = roleMgr.getListMenuAuth(roles);
		List<String> menuList = new ArrayList<String>();
		menuList.addAll(authMenuSet);
		listMenu = menuMgr.getMenuListByPermissionAuth(menuList);
		roleWithUserVo.getListMenu().addAll(listMenu);
		return roleWithUserVo;
	}
	
	
	@GET
	@Path("/get_auth_to_session")
	@Produces("application/json")
	public void getAuthToSession() throws UserPersistException {
		String user = this.getSecurityService().getCurrentUser();
		DefaultRoleManager roleMgr = new DefaultRoleManager(this
				.getPersistenceService());
		MenuAndPermission roleWithUserVo = new MenuAndPermission();
		Session session = SecurityUtils.getSubject().getSession();
		User userInfo = null;
		if(session.getAttribute("userInfo") == null) {
			UserManager userMgr = new DefaultUserManager(this.getPersistenceService());
			userInfo = userMgr.findById(user);
			 session.setAttribute("userInfo", userInfo);
		} else {
			userInfo = (User) session.getAttribute("userInfo");
		}
		if(userInfo != null) {
			roleWithUserVo.setUserName(userInfo.getUsername());
		}
		List<Role> roles = new ArrayList<Role>();
		roles = roleMgr.getRoleBySubject(user);	
		Set<String> authList = roleMgr.getListAuth(roles);
		if(session.getAttribute("auth") == null) {
			session.setAttribute("auth", authList);
		}
	}
	
	@GET
	@Path("/xml")
	@Produces("application/xml")
	public List<MenuVo> getRoleXMLMenu(@Context ServletContext ctx)throws XPathExpressionException, IOException, UserPersistException {
		String user = this.getSecurityService().getCurrentUser();
		MenuManager menuMgr = new DefaultMenuManager();
		DefaultRoleManager roleMgr = new DefaultRoleManager(this
				.getPersistenceService());

		List<MenuVo> listMenu = new ArrayList<MenuVo>();
		List<Role> roles = new ArrayList<Role>();
		roles = roleMgr.getRoleBySubject(user);	
		Set<String> authList = roleMgr.getListAuth(roles);
		Session session = SecurityUtils.getSubject().getSession();
		
		if(session.getAttribute("auth") == null) {
			session.setAttribute("auth", authList);
		}
		if(roles.size() == 1) {
			listMenu = menuMgr.getMenuListByPermissionAuth(roles.get(0).getPermissionsList());
			return listMenu;
		}
		Set<String> authMenuSet = roleMgr.getListMenuAuth(roles);
		List<String> menuList = new ArrayList<String>();
		menuList.addAll(authMenuSet);
		listMenu = menuMgr.getMenuListByPermissionAuth(menuList);
		return listMenu;
	}
}
