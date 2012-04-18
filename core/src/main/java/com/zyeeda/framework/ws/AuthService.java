package com.zyeeda.framework.ws;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.zyeeda.framework.entities.Role;
import com.zyeeda.framework.managers.PermissionManager;
import com.zyeeda.framework.managers.RoleManager;
import com.zyeeda.framework.managers.internal.DefaultPermissionManager;
import com.zyeeda.framework.managers.internal.DefaultRoleManager;
import com.zyeeda.framework.viewmodels.AuthVO;
import com.zyeeda.framework.viewmodels.PermissionVo;
import com.zyeeda.framework.ws.base.ResourceService;

@Path("/auth")
public class AuthService extends ResourceService {

	private final static String ROAM_PERMISSION_FILE = "roamPermission.xml";
	
	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
	
	private final static String PERMISSION_FILE = "permission.xml";
	
	@GET
	@Path("/{id}/{role_id}")
	@Produces("application/json")
	public List<AuthVO> getPermissionById(@PathParam("id") String id,
			@PathParam("role_id") String roleId)
			throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		PermissionManager permissionMgr = new DefaultPermissionManager();
		List<PermissionVo> list = permissionMgr.findSubPermissionById(id, PERMISSION_FILE);
		RoleManager roleMgr = new DefaultRoleManager(this
				.getPersistenceService());
		Role role = roleMgr.find(roleId);
		List<AuthVO> authVO = getAuthList(list, roleId, role
				.getPermissionList());
		return authVO;
	}

	public List<AuthVO> getAuthList(List<PermissionVo> list, String roleId,
			List<String> auth) {
		List<AuthVO> authList = new ArrayList<AuthVO>();
		
			for (PermissionVo permission : list) {
				//PermissionVo permission = (PermissionVo) list.get(i);
				AuthVO authVO = new AuthVO(); 
				authVO.setId(permission.getId());
				authVO.setLabel("<a>" + permission.getName() + "</a>");
				authVO.setType("task");
				authVO.setTag(permission.getValue());
				for (String roleAuth : auth) {
					if (roleAuth.trim().equals(permission.getValue().trim())) {
						authVO.setChecked(true);
						break;
					} else {
						authVO.setChecked(false);
					}
				}
				if ("false".equals(permission.getIsHaveIO().toString())) {
					authVO.setIo("/rest/auth/" + permission.getId() + "/"
							+ roleId);
					authVO.setLeaf(false);
				} else {
					authVO.setLeaf(true);
				}
				authList.add(authVO);
//				if(permission.getPermissionList().size() > 0) {
//					this.getAuthList(list, roleId, auth);
//				}
			}
		return authList;
	}
	
	@GET
	@Path("/{id}/raom_permission/{role_id}")
	@Produces("application/json")
	public List<AuthVO> getRPermissionById(@PathParam("id") String id,
			@PathParam("role_id") String roleId)
			throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		RoleManager roleMgr = new DefaultRoleManager(this
				.getPersistenceService());
		PermissionManager permissionMgr = new DefaultPermissionManager();
		List<PermissionVo> list = permissionMgr.findSubPermissionById(id, ROAM_PERMISSION_FILE);
		Role role = roleMgr.find(roleId);
		List<AuthVO> authVO = getRaomAuthList(list, roleId, role.getRoamPermissionList());
		return authVO;
	}
	

	
	public List<AuthVO> getRaomAuthList(List<PermissionVo> list, String roleId,
			List<String> auth) {
		List<AuthVO> authList = new ArrayList<AuthVO>();
		
			for (PermissionVo permission : list) {
				//PermissionVo permission = (PermissionVo) list.get(i);
				AuthVO authVO = new AuthVO(); 
				authVO.setId(permission.getId());
				authVO.setLabel("<a>" + permission.getName() + "</a>");
				authVO.setType("task");
				authVO.setTag(permission.getValue());
				for (String roleAuth : auth) {
					if (roleAuth.trim().equals(permission.getValue().trim())) {
						authVO.setChecked(true);
						break;
					} else {
						authVO.setChecked(false);
					}
				}
				if ("false".equals(permission.getIsHaveIO().toString())) {
					authVO.setIo("/rest/auth/" + permission.getId() + "/raom_permission/" + roleId);
					authVO.setLeaf(false);
				} else {
					authVO.setLeaf(true);
				}
				authList.add(authVO);
//				if(permission.getPermissionList().size() > 0) {
//					this.getAuthList(list, roleId, auth);
//				}
			}
		return authList;
	}
	
	@GET
	@Path("/{id}/get_auth_to_tree/{roleId}")
	@Produces("application/json")
	public List<AuthVO> getRoamPermissionById(@PathParam("id") String id, @PathParam("roleId") String roleId)
			throws XPathExpressionException, IOException {
		PermissionManager permissionMgr = new DefaultPermissionManager();
		RoleManager roleMgr = new DefaultRoleManager(this
				.getPersistenceService());
		List<String> authList = new ArrayList<String>();
		Role role = roleMgr.find(roleId);
		if(role != null) {
			authList = role.getRoamPermissionList();
			logger.debug("this RoamPermissionList auth value is :", authList.size());
		}
		List<AuthVO> list = permissionMgr.getPermissionToTree(id, ROAM_PERMISSION_FILE, authList);
		return list;
	}
	
	
	@GET
	@Path("/{id}/get_permission_tree/{roleId}")
	@Produces("application/json")
	public List<AuthVO> getPermissionsById(@PathParam("id") String id, @PathParam("roleId") String roleId)
			throws XPathExpressionException, IOException {
		PermissionManager permissionMgr = new DefaultPermissionManager();
		RoleManager roleMgr = new DefaultRoleManager(this
				.getPersistenceService());
		List<String> authList = new ArrayList<String>();
		Role role = roleMgr.find(roleId);
		if(role != null) {
			 authList = role.getPermissionsList();
			 logger.debug("this permission auth value is :", authList.size());
		}
		List<AuthVO> list = permissionMgr.getPermissionToTree(id, PERMISSION_FILE, authList);
		return list;
	}
	

}
