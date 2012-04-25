package com.zyeeda.framework.managers;



import com.googlecode.genericdao.dao.jpa.GenericDAO;
import com.zyeeda.framework.entities.Dictionary;

public interface DictionaryManager extends GenericDAO<Dictionary, String> {

	public Dictionary getDictionaryByTypeAndName(String name);
}
