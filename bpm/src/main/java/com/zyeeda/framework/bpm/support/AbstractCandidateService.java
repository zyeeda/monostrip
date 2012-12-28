package com.zyeeda.framework.bpm.support;

import java.util.ArrayList;
import java.util.Collection;

import org.activiti.engine.delegate.DelegateExecution;

import com.zyeeda.framework.bpm.CandidateService;

/**
 * @author guyong
 *
 */
public abstract class AbstractCandidateService implements CandidateService {

    @Override
    public Collection<String> getUsersByRoleId(String roleId) {
        return new ArrayList<String>();
    }

    @Override
    public Collection<String> getUsersByDepartmentId(String departmentId) {
        return new ArrayList<String>();
    }

    @Override
    public Collection<String> getUsersByRoleIds(String... roleIds) {
        return new ArrayList<String>();
    }

    @Override
    public Collection<String> getUsersByDepartmentIds(String... departmentIds) {
        return new ArrayList<String>();
    }

    @Override
    public Collection<String> getUsersByRoleId(DelegateExecution execution, String roleId) {
        return new ArrayList<String>();
    }

    @Override
    public Collection<String> getUsersByDepartmentId(DelegateExecution execution, String departmentId) {
        return new ArrayList<String>();
    }

    @Override
    public Collection<String> getUsersByRoleIds(DelegateExecution execution, String... roleIds) {
        return new ArrayList<String>();
    }

    @Override
    public Collection<String> getUsersByDepartmentIds(DelegateExecution execution, String... departmentIds) {
        return new ArrayList<String>();
    }

}
