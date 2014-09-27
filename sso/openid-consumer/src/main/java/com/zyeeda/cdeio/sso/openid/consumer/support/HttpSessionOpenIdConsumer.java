/* $Id: HttpSessionOpenIdConsumer.java,v 49501679b751 2013/09/02 16:18:32 tangrui $ */

package com.zyeeda.cdeio.sso.openid.consumer.support;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.openid4java.consumer.ConsumerException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;

/**
 * 使用 HTTP 会话作为后端存储的 OpenID consumer 实现类.
 *
 * @author $Author: tangrui $
 *
 */
public class HttpSessionOpenIdConsumer extends AbstractOpenIdConsumer {

    /**
     * 默认构造方法.
     *
     * @param servletContext Servlet context 对象
     *
     * @throws ConsumerException 构造失败时抛出
     */
    public HttpSessionOpenIdConsumer(final ServletContext servletContext) throws ConsumerException {
        super();
        this.setServletContext(servletContext);
    }

    @Override
    protected void storeDiscoveryInfo(final HttpServletRequest httpReq, final DiscoveryInformation discovered) {
        httpReq.getSession().setAttribute(OPENID_DISCOVERED_KEY, discovered);
    }

    @Override
    protected DiscoveryInformation retrieveDiscoveryInfo(final HttpServletRequest httpReq) {
        DiscoveryInformation discovered = (DiscoveryInformation) httpReq.getSession().getAttribute(OPENID_DISCOVERED_KEY);
        httpReq.getSession().removeAttribute(OPENID_DISCOVERED_KEY);
        return discovered;
    }

    @Override
    protected void storeIdentifier(final HttpServletRequest httpReq, final Identifier identifier) {
        httpReq.getSession().setAttribute(OPENID_IDENTIFIER_KEY, identifier);
    }

    @Override
    public Identifier retrieveIdentifier(final HttpServletRequest httpReq) {
        return (Identifier) httpReq.getSession().getAttribute(OPENID_IDENTIFIER_KEY);
    }

}
