/* $Id$ */

package com.zyeeda.cdeio.sso.openid;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.cdeio.commons.authc.entity.Permission;
import com.zyeeda.cdeio.commons.authc.entity.Role;
import com.zyeeda.cdeio.commons.organization.entity.Account;

/**
 * 抽象的 ShiroRealml.
 *
 * @author $Author$
 *
 */
public abstract class ShiroRealm extends AuthorizingRealm {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroRealm.class);

    /**
     * EntityManager 对象.
     */
    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            final PrincipalCollection principals) {
        if (principals.isEmpty()) {
            return null;
        }

        String accountId = ((Account) principals.getPrimaryPrincipal()).getId();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        List<Role> roles = this.getRoles("sso.openid.realm.findRolesByAccountId", accountId);
        for (Role role : roles) {
            info.addRole(role.getName());
            for (Permission perm : role.getPermissions()) {
                info.addStringPermission(perm.getValue());
            }
        }
        return info;
    }

    /**
     * 根据提供的 named query，获取名称为 name 的账户.
     *
     * @param namedQuery 命名查询
     * @param name 账户名
     *
     * @return 根据该查询获取到的账户名为 name 的账户.
     */
    protected Account getAccount(final String namedQuery, final String name) {
        TypedQuery<Account> query = this.entityManager.createNamedQuery(namedQuery, Account.class);
        query.setParameter("name", name);
        Account account = null;
        try {
            account = query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.debug("Cannot find account named " + name + ".", e);
        }

        return account;
    }

    /**
     * 根据提供的 named query，获取 accountId 对应账户的全部角色.
     *
     * @param namedQuery 命名查询
     * @param accountId 账户 ID
     *
     * @return accountId 账户对应的全部角色.
     */
    protected List<Role> getRoles(final String namedQuery, final String accountId) {
        TypedQuery<Role> query = this.entityManager.createNamedQuery(namedQuery, Role.class);
        query.setParameter("id", accountId);
        return query.getResultList();
    }

}
