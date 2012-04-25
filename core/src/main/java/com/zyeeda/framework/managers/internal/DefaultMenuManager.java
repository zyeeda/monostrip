package com.zyeeda.framework.managers.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.zyeeda.framework.managers.MenuManager;
import com.zyeeda.framework.managers.PermissionManager;
import com.zyeeda.framework.utils.MenuListComparator;
import com.zyeeda.framework.viewmodels.MenuVo;
import com.zyeeda.framework.viewmodels.PermissionVo;

public class DefaultMenuManager implements MenuManager {
	
	private final static String PERMISSION_FILE = "permission.xml";

	 public static Document generateDocumentByInputStream(InputStream is) throws ParserConfigurationException, SAXException, IOException {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);
	        factory.setValidating(false);
	        DocumentBuilder builder = factory.newDocumentBuilder();

	        return builder.parse(is);
	  }
	    

	public List<MenuVo> getMenuListByPermissionAuth(List<String> authList)
			throws XPathExpressionException, IOException {
		InputStream is = null;
		XPathExpression exp = null;
		XPathFactory fac = XPathFactory.newInstance();
		XPath xpath = fac.newXPath();
		PermissionManager permissionMgr = new DefaultPermissionManager();
		List<MenuVo> listMenu = new ArrayList<MenuVo>();
		Map<String, MenuVo> menuMap = new LinkedHashMap<String, MenuVo>();
		is = this.getClass().getClassLoader().getResourceAsStream(
				PERMISSION_FILE);
		String	root = null;
		try {
			Document document = DefaultMenuManager.generateDocumentByInputStream(is);
			for (String auth : authList) { 
				exp = xpath.compile("//p[@value='" + auth + "']");
				Node node = (Node) exp.evaluate(document, XPathConstants.NODE);
				if(!auth.endsWith("*")){
					PermissionVo childPermission = permissionMgr.getPermissionByPath(node);
					MenuVo childMenu = null;
					if (childPermission != null) {
						childMenu = this.convertPermission2Menu(childPermission);
						if (!(menuMap.containsKey(childMenu.getAuth()))) {
							menuMap.put(childMenu.getAuth(), childMenu);
						}else{
							continue;
						}
					} 
					PermissionVo parentPermission = permissionMgr.getParentPermissionByPath(node);
					if (parentPermission == null) {
							listMenu.add(menuMap.get(childMenu.getAuth()));
							continue;
					}
					if (menuMap.containsKey(parentPermission.getValue())) {
						MenuVo menuKey = menuMap.get(parentPermission.getValue());
						menuKey.getPermissionSet().add(childMenu);
						continue;
					}
					MenuVo parentMenu = this.convertPermission2Menu(parentPermission);
					parentMenu.getPermissionSet().add(childMenu);
					menuMap.put(parentPermission.getValue(), parentMenu);
					while (parentPermission != null) {
						parentMenu = new MenuVo();
					    String authKey = parentPermission.getValue();
					    exp = xpath.compile("//p[@value='" + parentPermission.getValue() + "']");
//					is = this.getClass().getClassLoader().getResourceAsStream(
//							PERMISSION_FILE);
						 node = (Node) exp.evaluate(document, XPathConstants.NODE);
						parentPermission = permissionMgr.getParentPermissionByPath(node);
							if(parentPermission != null){
								parentMenu.setAuth(parentPermission.getValue());
								parentMenu.setId(parentPermission.getId());
								parentMenu.setName(parentPermission.getName());
								parentMenu.setOrderBy(parentPermission.getOrderBy());
									if (!(menuMap.containsKey(parentMenu.getAuth()))) {
										parentMenu.getPermissionSet().add(menuMap.get(authKey));
										menuMap.put(parentMenu.getAuth(), parentMenu);
									} else {
										MenuVo menuKey = menuMap.get(parentMenu.getAuth());
										menuKey.getPermissionSet().add(menuMap.get(authKey));
										break;
									}
							} else {
								root = authKey;
								listMenu.add(menuMap.get(root));
							} 
					}
				}
			}
			if(listMenu.size() > 0){
				MenuListComparator comparator = new MenuListComparator();
				Collections.sort(listMenu, comparator);
			}
		} catch (Exception e){
			throw new RuntimeException(e);
		} finally {
			if(is != null) {is.close();}
		}
		return listMenu;
	}
	
	private MenuVo convertPermission2Menu(PermissionVo permission) {
		MenuVo menu = new MenuVo();
		menu.setAuth(permission.getValue());
		menu.setId(permission.getId());
		menu.setName(permission.getName());
		menu.setOrderBy(permission.getOrderBy());
		return menu;
	}
	
}
