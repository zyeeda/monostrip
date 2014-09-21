/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider;

import org.openid4java.association.AssociationException;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerException;

/**
 * OpenID provider.
 *
 * @author $Author$
 *
 */
public interface OpenIdProvider {

    /**
     * 设置名称.
     *
     * @param name 名称
     */
    void setName(String name);

    /**
     * 获取名称.
     *
     * @return 名称.
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
     * @param serverAddress 服务器地址
     */
    void setServerAddress(String serverAddress);

    /**
     * 设置服务器端口.
     *
     * @param serverPort 服务器端口
     */
    void setServerPort(int serverPort);

    /**
     * 设置首页路径.
     *
     * @param indexPath 首页路径
     */
    void setIndexPath(String indexPath);

    /**
     * 设置 base 路径.
     *
     * @param basePath base 路径
     */
    void setBasePath(String basePath);

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
     * 获取登录路径.
     * <p>默认值为 ${basePath}/signin.</p>
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
     * 获取登出路径.
     * <p>默认值为 ${basePath}/signout.</p>
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
     * 获取主页路径.
     * <p>默认值为 ${basePath}/main.</p>
     *
     * @return 主页路径
     */
    String getMainPath();

    /**
     * 获取主页 URL.
     *
     * @return 主页 URL
     */
    String getMainUrl();

    /**
     * 获取 endpoint 路径.
     * <p>默认值为 ${basePath}/endpoint.</p>
     *
     * @return endpoint 路径
     */
    String getEndpointPath();

    /**
     * 获取 endpoint URL.
     *
     * @return endpoint URL
     */
    String getEndpointUrl();

    /**
     * 获取 endpoint complete 路径.
     * <p>默认值为 ${basePath}/endpoint/complete.</p>
     *
     * @return endpoint complete 路径
     */
    String getEndpointCompletePath();

    /**
     * 获取 endpoint complete URL.
     *
     * @return endpoint complete URL
     */
    String getEndpointCompleteUrl();

    /**
     * 获取上下文登录回调页面路径.
     * <p>默认值为 ${basePath}/callback.</p>
     *
     * @return 上下文登录回调页面路径
     */
    String getCallbackPath();

    /**
     * 获取上下文登录回调页面 URL.
     *
     * @return 上下文登录回调页面 URL
     */
    String getCallbackUrl();

    /**
     * 获取用户路径.
     * <p>默认值为 ${basePath}/user?id=${userId}.</p>
     *
     * @param userId 用户 ID
     *
     * @return 用户路径
     */
    String getUserPath(String userId);

    /**
     * 获取用户 URL.
     *
     * @param userId 用户 ID
     *
     * @return 用户 URL
     */
    String getUserUrl(String userId);

    /**
     * 关联请求.
     *
     * @param params 参数
     *
     * @return 返回的信息
     */
    Message associateRequest(ParameterList params);

    /**
     * 验证请求.
     *
     * @param params 参数
     *
     * @return 返回的信息
     */
    Message verifyRequest(ParameterList params);

    /**
     * 认证响应.
     *
     * @param params 参数
     * @param userSelectedId 用户选择的 ID
     * @param userSelectedClaimedId 用户提取的 ID
     * @param authenticatedAndApproved 是否认证且通过
     * @param opEndpointUrl OP 的 endpoint URL
     *
     * @return 返回的信息
     *
     * @throws MessageException 异常
     * @throws ServerException 异常
     * @throws AssociationException 异常
     */
    Message authResponse(ParameterList params, String userSelectedId,
            String userSelectedClaimedId, boolean authenticatedAndApproved, String opEndpointUrl) throws MessageException, ServerException, AssociationException;

}
