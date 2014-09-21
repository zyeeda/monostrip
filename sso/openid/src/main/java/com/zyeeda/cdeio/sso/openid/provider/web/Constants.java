/* $Id */

package com.zyeeda.cdeio.sso.openid.provider.web;

/**
 * Filter 里面使用到的常量.
 *
 * @author $Author$
 *
 */
public final class Constants {

    /**
     * cookie 名称.
     */
    public static final String COOKIE_NAME = "SIGNINTOKEN";

    /**
     * 向 request 里写入的 SignInToken 属性的名称.
     */
    public static final String SIGN_IN_TOKEN_ATTRIBUTE_NAME = "signInToken";

    /**
     * 私有构造方法.
     */
    private Constants() {
    }

}
