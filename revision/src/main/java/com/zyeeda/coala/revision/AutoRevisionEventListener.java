/* $Id$ */

package com.zyeeda.coala.revision;

import java.util.Date;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.coala.commons.base.entity.RevisionDomainEntity;
import com.zyeeda.coala.commons.organization.entity.Account;

/**
 * JPA event listener.
 * 当 RevisionDomainEntity 保存的时候，自动更新相关字段.
 *
 * @author $Author$
 *
 */
public class AutoRevisionEventListener {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoRevisionEventListener.class);

    /**
     * 存储之前执行的回调方法.
     * 自动填入创建人、创建时间、最后修改人和最后修改时间等信息.
     *
     * @param obj 将要存储的实体
     */
    public void onPrePersist(final Object obj) {
        LOGGER.debug("On pre-persist callback.");
        if (obj instanceof RevisionDomainEntity) {
            LOGGER.debug("Object is of type RevisionDomainEntity");
            RevisionDomainEntity rev = (RevisionDomainEntity) obj;

            Date now = new Date();
            LOGGER.debug("current time = {}", now);
            rev.setCreatedTime(now);
            rev.setLastModifiedTime(now);

            Subject subject = SecurityUtils.getSubject();
            if (subject.isAuthenticated()) {
                Account account = (Account) subject.getPrincipal();
                LOGGER.debug("Authenticated subject name = {}", account.getAccountName());
                rev.setCreator(account.getId());
                rev.setCreatorName(account.getRealName());
                rev.setLastModifier(account.getId());
                rev.setLastModifierName(account.getRealName());
            } else {
                LOGGER.debug("Subject is not authenticated.");
            }
        }
    }

    /**
     * 更细之前执行的回调方法.
     * 自动填入最后修改人和最后修改时间等字段.
     *
     * @param obj 将要更新的实体
     */
    public void onPreUpdate(final Object obj) {
        LOGGER.debug("On pre-update callback.");
        if (obj instanceof RevisionDomainEntity) {
            LOGGER.debug("Object is of type RevisionDomainEntity");
            RevisionDomainEntity rev = (RevisionDomainEntity) obj;

            rev.setLastModifiedTime(new Date());
            Subject subject = SecurityUtils.getSubject();
            if (subject.isAuthenticated()) {
                Account account = (Account) subject.getPrincipal();
                LOGGER.debug("Authenticated subject name = {}", account.getAccountName());
                rev.setLastModifier(account.getId());
                rev.setLastModifierName(account.getRealName());
            }
        }
    }

}
