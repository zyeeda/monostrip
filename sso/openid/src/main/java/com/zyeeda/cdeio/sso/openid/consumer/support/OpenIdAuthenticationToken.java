/* $Id$ */

package com.zyeeda.cdeio.sso.openid.consumer.support;

import org.apache.shiro.authc.AuthenticationToken;
import org.openid4java.discovery.Identifier;

/**
 * OpenId 认证令牌.
 *
 * @author $Author$
 *
 */
public class OpenIdAuthenticationToken implements AuthenticationToken {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = -1651819514779654758L;

    /**
     * 用户标识.
     */
    private String principal;

    /**
     * 构造方法.
     *
     * @param identifier 用户标识
     */
    public OpenIdAuthenticationToken(final Identifier identifier) {
        super();

        String id = identifier.getIdentifier();
        int index = id.indexOf("id=") + "id=".length();
        id = id.substring(index);
        this.principal = id;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

}
