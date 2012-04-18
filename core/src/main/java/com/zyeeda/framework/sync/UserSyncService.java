package com.zyeeda.framework.sync;

import com.zyeeda.framework.entities.User;
import com.zyeeda.framework.service.Service;

public interface UserSyncService extends Service {
	
	public void persist(User user);
	
	public void update(User user);
	
	public void enable(String... ids);
	
	public void disable(String... ids);
	
	public void updateAssignPassword(String id, String newPassword);
	
	public void updateDeptName(String oldDeptPath, String newDeptPath);
	
	public void remove(String id);
	
//	public void setVisible(Boolean visible, String... ids);
}
