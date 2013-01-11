package com.zyeeda.coala.bpm;

import java.util.Collection;

import org.activiti.engine.delegate.DelegateExecution;

/**
 * @author guyong
 *
 */
public interface CandidateService {

    Collection<String> getUsersByRoleId(String roleId);
    
    Collection<String> getUsersByDepartmentId(String departmentId);
    
    Collection<String> getUsersByRoleIds(String... roleIds);
    
    Collection<String> getUsersByDepartmentIds(String... departmentIds);

    Collection<String> getUsersByRoleId(DelegateExecution execution, String roleId);
    
    Collection<String> getUsersByDepartmentId(DelegateExecution execution, String departmentId);
    
    Collection<String> getUsersByRoleIds(DelegateExecution execution, String... roleIds);
    
    Collection<String> getUsersByDepartmentIds(DelegateExecution execution, String... departmentIds);
    
}
