package com.zyeeda.framework.managers;

import java.io.IOException;

import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.zyeeda.framework.viewmodels.AuthVO;
import com.zyeeda.framework.viewmodels.PermissionVo;

public interface PermissionManager {
		public List<PermissionVo> findSubPermissionById(String id, String authXml) throws XPathExpressionException, IOException,ParserConfigurationException, SAXException;
	
	public  PermissionVo getPermissionByPath(Node node) throws XPathExpressionException,
	IOException;
	
	public PermissionVo getParentPermissionByPath(Node node) throws XPathExpressionException,
	IOException;
	
	public String getParentPermissionListAuthByList(List<String> authList, String authXml) throws XPathExpressionException, IOException;
	
	
	public List<AuthVO> getPermissionToTree(String id, String authXml, List<String> authList)
	throws XPathExpressionException, IOException ;

	public void getPermissionByPathToRomPermission(ServletContext ctx)
	throws XPathExpressionException, IOException, ParserConfigurationException, SAXException;
}
