package com.zyeeda.cdeio.web;

import javax.servlet.ServletRequest;

import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

public class EntityManagerControlFilter extends OpenEntityManagerInViewFilter {

    private static final String ENTITY_MANAGER_CONTROL_KEY = "entityManagerControl";
    
    protected void setEntityManagerControlFlag(final ServletRequest request) {
        request.setAttribute(ENTITY_MANAGER_CONTROL_KEY, new Object());
    }
    
    protected boolean hasEntityManagerControlFlag(final ServletRequest request) {
        return request.getAttribute(ENTITY_MANAGER_CONTROL_KEY) != null;
    }

}
