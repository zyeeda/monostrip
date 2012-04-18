package com.zyeeda.framework.viewmodels;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.zyeeda.framework.entities.Role;
@XmlRootElement(name = "roleVoAndQualification")
public class RoleVoAndQualification {
	
	private List<DepartmentVo> departmentVo = new ArrayList<DepartmentVo>();
	
	public List<DepartmentVo> getDepartmentVo() {
		return departmentVo;
	}

	public void setDepartmentVo(List<DepartmentVo> departmentVo) {
		this.departmentVo = departmentVo;
	}

	private List<Role> role = new ArrayList<Role>();

	

	public List<Role> getRole() {
		return role;
	}

	public void setRole(List<Role> role) {
		this.role = role;
	}

}
