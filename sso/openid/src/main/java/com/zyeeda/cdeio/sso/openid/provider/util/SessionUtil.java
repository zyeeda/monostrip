/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.util;

import java.util.LinkedList;
import java.util.List;

import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.cdeio.sso.openid.provider.ActiveSite;

/**
 * Session 工具类.
 *
 * @author $Author$
 *
 */
public final class SessionUtil {

    /**
     * 日志组件.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionUtil.class);

    /**
     * Sites key.
     */
    private static final String KEY_SITES = "sites";

    /**
     * 默认私有构造方法.
     */
    private SessionUtil() {
    }

    /**
     * 存储活动站点信息.
     *
     * @param session session 对象
     * @param site 活动站点信息
     */
    public static void storeSite(final Session session, final ActiveSite site) {
        List<ActiveSite> list = retrieveSites(session);
        list.add(site);
    }

    /**
     * 存储活动站点信息.
     *
     * @param session session 对象
     * @param sites 所有活动站点信息
     */
    public static void storeSites(final Session session, final List<ActiveSite> sites) {
        List<ActiveSite> list = retrieveSites(session);
        list.addAll(sites);
    }

    /**
     * 获取站点信息.
     *
     * @param session session 对象
     *
     * @return 所有站点信息
     */
    @SuppressWarnings("unchecked")
    public static List<ActiveSite> retrieveSites(final Session session) {
        LOGGER.debug("Retrieve active sites.");
        List<ActiveSite> sites = (List<ActiveSite>) session.getAttribute(KEY_SITES);
        if (sites == null) {
            LOGGER.debug("Active site is null, create one.");
            sites = new LinkedList<ActiveSite>();
            session.setAttribute(KEY_SITES, sites);
        }
        return sites;
    }
}
