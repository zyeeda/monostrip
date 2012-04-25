package com.zyeeda.framework.persistence;

import java.util.Date;

import org.apache.shiro.SecurityUtils;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;

import com.zyeeda.framework.entities.base.RevisionDomainEntity;

public class AutoRevisionEventListener implements PreInsertEventListener, PreUpdateEventListener {

	private static final long serialVersionUID = 3017978089669707604L;
	
	private String getCurrentUser() {
	    Object o = SecurityUtils.getSubject().getPrincipal();
	    return o == null ? null : o.toString();
	}
	
	@Override
	public boolean onPreInsert(PreInsertEvent event) {
		Object e = event.getEntity();
		if (e instanceof RevisionDomainEntity) {
			RevisionDomainEntity rev = (RevisionDomainEntity) e;
			Date now = new Date();
			rev.setCreator(getCurrentUser());
			rev.setCreatedTime(now);
			rev.setLastModifier(getCurrentUser());
			rev.setLastModifiedTime(now);
		}
		return false;
	}
	
	@Override
	public boolean onPreUpdate(PreUpdateEvent event) {
		Object e = event.getEntity();
		if (e instanceof RevisionDomainEntity) {
			RevisionDomainEntity rev = (RevisionDomainEntity) e;
			rev.setLastModifier(getCurrentUser());
			rev.setLastModifiedTime(new Date());
		}
		return false;
	}

}
