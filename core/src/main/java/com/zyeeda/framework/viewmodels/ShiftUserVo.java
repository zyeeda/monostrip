package com.zyeeda.framework.viewmodels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "shiftUser")
public class ShiftUserVo implements Comparable<Object> {

	private String userId;
	
	private String username;
	
	private String roleId;
	
	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	private String roleName;
	
	private boolean checked = false;
	
	@Override
	public boolean equals(Object obj) {
		ShiftUserVo cUser = (ShiftUserVo) obj;
		if(cUser.getUserId().equals(this.getUserId())) {
			return true;
		}
		return false;
	}

	@Override
	public int compareTo(Object obj) {
		return this.getUserId().compareTo(((ShiftUserVo)obj).getUserId());
	}

	
	@Override
	public int hashCode() {
		return userId.hashCode();
	}
	
	public static void main(String[] args) {
		Set<ShiftUserVo> set = new HashSet<ShiftUserVo>();
		
		List<ShiftUserVo> list = new ArrayList<ShiftUserVo>();
		
		ShiftUserVo vo1 = new ShiftUserVo();
		vo1.setUserId("yelin");
		vo1.setUsername("叶林");
		vo1.setRoleName("当班值-值班员");
		
		ShiftUserVo vo2 = new ShiftUserVo();
		vo2.setUserId("yelin");
		vo2.setUsername("叶林");
		vo2.setRoleName("当班值-值班长");
		
		list.add(vo1);
		list.add(vo2);
		
		set.addAll(list);
		System.out.println(set.size());
		System.out.println(vo1.hashCode());
		System.out.println(vo2.hashCode());
	}
	
}
