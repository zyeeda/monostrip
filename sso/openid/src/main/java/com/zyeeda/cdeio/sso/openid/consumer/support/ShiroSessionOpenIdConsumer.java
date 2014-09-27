/* $Id$ */

package com.zyeeda.cdeio.sso.openid.consumer.support;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.springframework.web.context.ServletContextAware;

import com.zyeeda.cdeio.sso.openid.consumer.support.AbstractOpenIdConsumer;

/**
 * 使用 Shiro 会话作为后端存储的 OpenID consumer 实现类.
 *
 * @author $Author$
 *
 */
public class ShiroSessionOpenIdConsumer extends AbstractOpenIdConsumer implements ServletContextAware {

    /**
     * 默认构造方法.
     *
     * @throws ConsumerException 构造失败时抛出
     */
    public ShiroSessionOpenIdConsumer() throws ConsumerException {
        super();
    }

    @Override
    protected void storeDiscoveryInfo(final HttpServletRequest httpReq, final DiscoveryInformation discovered) {
        SecurityUtils.getSubject().getSession().setAttribute(OPENID_DISCOVERED_KEY, discovered);
    }

    @Override
    protected DiscoveryInformation retrieveDiscoveryInfo(final HttpServletRequest httpReq) {
        return (DiscoveryInformation) SecurityUtils.getSubject().getSession().getAttribute(OPENID_DISCOVERED_KEY);
    }

    @Override
    protected void storeIdentifier(final HttpServletRequest httpReq, final Identifier identifier) {
        SecurityUtils.getSubject().getSession().setAttribute(OPENID_IDENTIFIER_KEY, identifier);
    }

    @Override
    public Identifier retrieveIdentifier(final HttpServletRequest httpReq) {
        return (Identifier) SecurityUtils.getSubject().getSession().getAttribute(OPENID_IDENTIFIER_KEY);
    }

}
