/* $Id$ */

package com.zyeeda.cdeio.sso.openid.consumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;

/**
 * OpenID consumer.
 *
 * @author $Author$
 *
 */
public interface OpenIdConsumer {

    /**
     * 设置名称.
     *
     * @param name 名称
     */
    void setName(String name);

    /**
     * 获取名称.
     *
     * @return 名称
     */
    String getName();

    /**
     * 设置服务器协议.
     *
     * @param serverProtocol 服务器协议
     */
    void setServerProtocol(String serverProtocol);

    /**
     * 设置服务器地址.
     *
     * @param serverAddress 服务器地址.
     */
    void setServerAddress(String serverAddress);

    /**
     * 设置服务器端口.
     *
     * @param serverPort 服务器端口.
     */
    void setServerPort(int serverPort);

    /**
     * 设置首页路径.
     *
     * @param indexPath 首页路径
     */
    void setIndexPath(String indexPath);

    /**
     * 获取首页路径.
     *
     * @return 首页路径
     */
    String getIndexPath();

    /**
     * 获取首页 URL.
     *
     * @return 首页 URL
     */
    String getIndexUrl();

    /**
     * 设置登录路径.
     *
     * @param signInPath 登录路径
     */
    void setSignInPath(String signInPath);

    /**
     * 获取登录路径.
     *
     * @return 登录路径
     */
    String getSignInPath();

    /**
     * 获取登录 URL.
     *
     * @return 登录 URL
     */
    String getSignInUrl();

    /**
     * 设置登出路径.
     *
     * @param signOutPath 登出路径
     */
    void setSignOutPath(String signOutPath);

    /**
     * 获取登出路径.
     *
     * @return 登出路径
     */
    String getSignOutPath();

    /**
     * 获取登出 URL.
     *
     * @return 登出 URL
     */
    String getSignOutUrl();

    /**
     * 设置返回路径.
     *
     * @param returnToPath 返回路径
     */
    void setReturnToPath(String returnToPath);

    /**
     * 获取 OP 端认证后的返回路径.
     *
     * @return OP 端认证后的返回路径
     */
    String getReturnToPath();

    /**
     * 获取 OP 端认证后的返回 URL.
     *
     * @return OP 端认证后的返回 URL
     */
    String getReturnToUrl();

    /**
     * 设置回调路径.
     *
     * @param callbackPath 回调路径
     */
    void setCallbackPath(String callbackPath);

    /**
     * 获取上下文登录后的回调路径.
     *
     * @return 上下文登录后的回调路径
     */
    String getCallbackPath();

    /**
     * 获取上下文登录后的回调 URL.
     *
     * @return 上下文登录后的回调 URL
     */
    String getCallbackUrl();

    /**
     * 设置 OP 端服务器协议.
     *
     * @param providerServerProtocol OP 端服务器协议
     */
    void setProviderServerProtocol(String providerServerProtocol);

    /**
     * 设置 OP 端服务器地址.
     *
     * @param providerServerAddress OP 端服务器地址
     */
    void setProviderServerAddress(String providerServerAddress);

    /**
     * 设置 OP 端服务器端口.
     *
     * @param providerServerPort OP 端服务器端口
     */
    void setProviderServerPort(int providerServerPort);

    /**
     * 设置 OP 端 XRDS 路径.
     *
     * @param providerXrdsPath OP 端 XRDS 路径
     */
    void setProviderXrdsPath(String providerXrdsPath);

    /**
     * 获取 OP 端 XRDS 路径.
     *
     * @return OP 端 XRDS 路径
     */
    String getProviderXrdsPath();

    /**
     * 获取 OP 端 XRDS URL.
     *
     * @return OP 端 XRDS URL
     */
    String getProviderXrdsUrl();

    /**
     * 设置 OP 端登录路径.
     *
     * @param providerSignInPath OP 端登录路径
     */
    void setProviderSignInPath(String providerSignInPath);

    /**
     * 获取 OP 端登录路径.
     *
     * @return OP 端登录路径
     */
    String getProviderSignInPath();

    /**
     * 获取 OP 端登录 URL.
     *
     * @return OP 端登录 URL
     */
    String getProviderSignInUrl();

    /**
     * 设置 OP 端登出路径.
     *
     * @param providerSignOutPath OP 端登出路径
     */
    void setProviderSignOutPath(String providerSignOutPath);

    /**
     * 获取 OP 端登出路径.
     *
     * @return OP 端登出路径
     */
    String getProviderSignOutPath();

    /**
     * 获取 OP 端登出 URL.
     *
     * @return OP 端登出 URL
     */
    String getProviderSignOutUrl();

    /**
     * 生成 {@link #AuthRequest} 认证请求对象.
     *
     * @param userSuppliedId 用户提供的 ID
     * @param httpReq HTTP 请求
     * @param httpRes HTTP 响应
     *
     * @return 认证请求对象
     *
     * @throws OpenIDException 认证请求生成失败时抛出
     */
    AuthRequest authRequest(String userSuppliedId, HttpServletRequest httpReq,
            HttpServletResponse httpRes) throws OpenIDException;

    /**
     * 验证 OP 端返回的请求, 并提取用户 ID.
     *
     * @param httpReq HTTP 请求
     *
     * @return 成功验证后提取的用户 ID
     *
     * @throws OpenIDException 验证失败时抛出
     */
    Identifier verifyResponse(HttpServletRequest httpReq)
            throws OpenIDException;

    /**
     * 提取身份信息.
     *
     * @param httpReq HTTP 请求
     *
     * @return 身份信息
     */
    Identifier retrieveIdentifier(HttpServletRequest httpReq);

}
