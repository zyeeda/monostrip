package com.zyeeda.framework.managers.internal;

import java.util.Date;
import java.util.List;

import com.googlecode.genericdao.search.Search;
import com.zyeeda.framework.entities.TheThirdRoleAndUser;
import com.zyeeda.framework.managers.TheThirdUserAndRoleManager;
import com.zyeeda.framework.managers.base.DomainEntityManager;
import com.zyeeda.framework.persistence.PersistenceService;

public class DefaultTheThirdTableManager extends DomainEntityManager<TheThirdRoleAndUser, String> implements TheThirdUserAndRoleManager {

	public DefaultTheThirdTableManager(PersistenceService persistenceSvc) {
		super(persistenceSvc);
	}
	
	public void editeTheThirdTableDate(String[] TheThirdTableIds, Date changeTime) {
		Date date = new Date();
		if(TheThirdTableIds != null){
			for(String id : TheThirdTableIds) {
				TheThirdRoleAndUser theThirdRoleAndUser = this.find(id);
				theThirdRoleAndUser.setPassTime(changeTime);
				if(changeTime.after(date)) {
					theThirdRoleAndUser.setIsPass(true);
				}
				if(date.after(changeTime)) {
					theThirdRoleAndUser.setIsPass(false);
				}
			}
		}
		this.getPersistenceService().getCurrentSession().flush();
	}
	
	public void changeTheThirdTableDate() {
		Search search = new Search();
		Date date = new Date();
		search.addFilterLessThan("passTime", date);
		search.addFilterEqual("isPass", true);
		List<TheThirdRoleAndUser> theThirdRoleAndUserList = this.search(search);
		for(TheThirdRoleAndUser theThirdRoleAndUser : theThirdRoleAndUserList) {
			theThirdRoleAndUser.setIsPass(false);
		}
		search.clear();
		search.addFilterGreaterThan("passTime", date);
		search.addFilterEqual("isPass", false);
		List<TheThirdRoleAndUser> theThirdRoleAndUserLists = this.search(search);
		for(TheThirdRoleAndUser theThirdRoleAndUser : theThirdRoleAndUserLists) {
			theThirdRoleAndUser.setIsPass(true);
		}
		this.getPersistenceService().getCurrentSession().flush();
	}

}
