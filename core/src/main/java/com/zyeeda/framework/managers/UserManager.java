package com.zyeeda.framework.managers;

import java.util.List;

import javax.naming.directory.SearchControls;

import com.googlecode.genericdao.dao.jpa.GenericDAO;
import com.zyeeda.framework.entities.User;

public interface UserManager extends GenericDAO<User, String>{

	public void persist(User user) throws UserPersistException;
	
	public void remove(String id) throws UserPersistException;
	
	public void update(User user) throws UserPersistException;
	
	public User findById(String id) throws UserPersistException;
	
	public List<User> findByDepartmentId(String id) throws UserPersistException;
	
	public List<User> findByName(String name) throws UserPersistException;
	
	public List<User> findByName(String name, SearchControls sc) throws UserPersistException;
	
	public List<User> search(String condition) throws UserPersistException;
	
	public void updatePassword(String id, String password) throws UserPersistException;
	
	public void updateAssignPassword(String id, String password) throws UserPersistException;
	
	public void enable(String... ids) throws UserPersistException;
	
	public void disable(String... ids) throws UserPersistException;

	public List<User> findByDepartmentId(String id, SearchControls sc) throws UserPersistException;
	
	public String findStationDivisionByCreator(String creator) throws UserPersistException;
	
	public String getChinaName(String userId) throws UserPersistException;

}
