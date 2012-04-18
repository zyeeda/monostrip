package com.zyeeda.framework.managers.internal;

import java.util.List;

import javax.naming.directory.SearchControls;

import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.managers.UserManager;
import com.zyeeda.framework.managers.UserPersistException;
import com.zyeeda.framework.managers.base.DomainEntityManager;
import com.zyeeda.framework.persistence.PersistenceService;

public class DefaultUserManager extends DomainEntityManager<User, String>
		implements UserManager {

	public DefaultUserManager(PersistenceService persistenceSvc) {
		super(persistenceSvc);
	}

	@Override
	public List<User> findByDepartmentId(String id) {
		return null;
	}

	@Override
	public User findById(String id) throws UserPersistException {
		return super.find(id);
	}

	@Override
	public List<User> findByName(String name) throws UserPersistException {
		return null;
	}

	@Override
	public void persist(User user) throws UserPersistException {
		super.persist(user);

	}

	@Override
	public void remove(String id) throws UserPersistException {
		super.removeById(id);
	}

	
	@Override
	public void disable(String... ids) throws UserPersistException {
		this.setVisible(false, ids);
	}

	@Override
	public void enable(String... ids) throws UserPersistException {
		this.setVisible(true, ids);
	}

	@Override
	public void update(User user) throws UserPersistException {
		super.merge(user);
	}

	@Override
	public void updatePassword(String id, String password) throws UserPersistException {
		User user = findById(id);
		user.setPassword(password);

		super.merge(user);
	}
	
	@Override
	public void updateAssignPassword(String id, String password) throws UserPersistException {
		User user = findById(id);
		user.setAssignPassword(password);

		super.merge(user);
	}

	private void setVisible(Boolean visible, String... ids) {
		StringBuffer buffer = new StringBuffer();
		for (String id : ids) {
			buffer.append(id).append(",");
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.lastIndexOf(","));
		}
		super.em().createQuery("update com.zyeeda.framework.entities.User o set o.status = ?1 where o.id in(?2)")
				                     .setParameter(1, visible).setParameter(2, buffer.toString()).executeUpdate();
	}

	@Override
	public List<User> search(String condition) throws UserPersistException {
		return null;
	}

	@Override
	public List<User> findByDepartmentId(String id, SearchControls sc)
			throws UserPersistException {
		return null;
	}

	@Override
	public List<User> findByName(String name, SearchControls sc)
			throws UserPersistException {
		return null;
	}

	@Override
	public String findStationDivisionByCreator(String creator)
			throws UserPersistException {
		return null;
	}
	
	public String updateDeptName(String oldDept, String newDept) {
		
		return null;
	}
	
	public String getChinaName(String userId) throws UserPersistException {
		User user = this.findById(userId);
		if(user == null) {
			return "";
		}
		String userName = user.getUsername();
		return userName;
	}

}
