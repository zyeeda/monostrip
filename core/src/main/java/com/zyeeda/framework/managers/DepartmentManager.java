package com.zyeeda.framework.managers;

import java.util.List;

import com.zyeeda.framework.entities.Department;

public interface DepartmentManager {

	public void persist(Department dept) throws UserPersistException;
	
	public Department findById(String id) throws UserPersistException;
	
	public void remove(String id) throws UserPersistException;
	
	public void update(Department dept) throws UserPersistException;
	
	public List<Department> getChildrenById(String id) throws UserPersistException;
	
	public List<Department> getRootAndSecondLevelDepartment() throws UserPersistException;
	
	public List<Department> getDepartmentListByUserId(String userId) throws UserPersistException;
	
	public List<Department> search(String condition) throws UserPersistException;
	
	public List<Department> findByName(String name) throws UserPersistException;
}
