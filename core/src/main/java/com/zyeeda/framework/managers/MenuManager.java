package com.zyeeda.framework.managers;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import com.zyeeda.framework.viewmodels.MenuVo;

public interface MenuManager {
	
	public List<MenuVo> getMenuListByPermissionAuth(List<String> authList) throws XPathExpressionException, IOException;

	

}