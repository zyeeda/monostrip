package com.zyeeda.framework.managers;

import java.util.Date;

import com.googlecode.genericdao.dao.jpa.GenericDAO;
import com.zyeeda.framework.entities.TheThirdRoleAndUser;

public interface TheThirdUserAndRoleManager extends GenericDAO<TheThirdRoleAndUser, String>{

	
	public void editeTheThirdTableDate(String[] TheThirdTableIds, Date changeTime);
	
	public void changeTheThirdTableDate();
}
