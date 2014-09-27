/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.support;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.openid4java.association.AssociationException;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerException;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import com.zyeeda.cdeio.sso.openid.provider.OpenIdProvider;

/**
 * 默认 OpenID provider 实现类.
 *
 * @author $Author$
 *
 */
public class DefaultOpenIdProvider implements OpenIdProvider, ServletContextAware {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOpenIdProvider.class);

    /**
     * ServerManager 对象.
     */
    private ServerManager serverManager;

    /**
     * Servlet context 对象.
     */
    private ServletContext servletContext;

    /**
     * 名称.
     */
    private String name;

    /**
     * 服务器协议.
     */
    private String serverProtocol;

    /**
     * 服务器地址.
     */
    private String serverAddress;

    /**
     * 服务器端口.
     */
    private int serverPort;

    /**
     * 首页路径.
     */
    private String indexPath;

    /**
     * Base 路径.
     */
    private String basePath;

    /**
     * 初始化方法.
     */
    public void init() {
        this.serverManager = new ServerManager();
        this.serverManager.setOPEndpointUrl(this.getEndpointCompletePath());
        this.serverManager.setSharedAssociations(new InMemoryServerAssociationStore());
        this.serverManager.setPrivateAssociations(new InMemoryServerAssociationStore());
    }

    @Override
    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setServerProtocol(final String serverProtocol) {
        this.serverProtocol = serverProtocol;
    }

    @Override
    public void setServerAddress(final String serverAddress) {
        this.serverAddress = serverAddress;
    }

    @Override
    public void setServerPort(final int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void setIndexPath(final String indexPath) {
        this.indexPath = indexPath;
    }

    @Override
    public void setBasePath(final String basePath) {
        this.basePath = basePath;
    }

    @Override
    public String getIndexPath() {
        return this.indexPath;
    }

    @Override
    public String getIndexUrl() {
        return this.pathToUrl(this.indexPath);
    }

    @Override
    public String getSignInPath() {
        return this.basePath + "/signin";
    }

    @Override
    public String getSignInUrl() {
        return this.pathToUrl(this.getSignInPath());
    }

    @Override
    public String getSignOutPath() {
        return this.basePath + "/signout";
    }

    @Override
    public String getSignOutUrl() {
        return this.pathToUrl(this.getSignOutPath());
    }

    @Override
    public String getMainPath() {
        return this.basePath + "/main";
    }

    @Override
    public String getMainUrl() {
        return this.pathToUrl(this.getMainPath());
    }

    @Override
    public String getEndpointPath() {
        return this.basePath + "/endpoint";
    }

    @Override
    public String getEndpointUrl() {
        return this.pathToUrl(this.getEndpointPath());
    }

    @Override
    public String getEndpointCompletePath() {
        return this.basePath + "/endpoint/complete";
    }

    @Override
    public String getEndpointCompleteUrl() {
        return this.pathToUrl(this.getEndpointCompletePath());
    }

    @Override
    public String getCallbackPath() {
        return this.basePath + "/callback";
    }

    @Override
    public String getCallbackUrl() {
        return this.pathToUrl(this.getCallbackPath());
    }

    @Override
    public String getUserPath(final String userId) {
        return this.basePath + "/user?id=" + userId;
    }

    @Override
    public String getUserUrl(final String userId) {
        return this.pathToUrl(this.getUserPath(userId));
    }

    /**
     * 将 path 路径转换为对应的 URL 形式.
     *
     * @param path 路径
     * @return 对应的 URL 形式
     */
    private String pathToUrl(final String path) {
        try {
            URL url = new URL(this.serverProtocol, this.serverAddress, this.serverPort, this.servletContext.getContextPath() + path);
            return url.toString();
        } catch (MalformedURLException e) {
            LOGGER.error("Cannot build URL from path.", e);
        }
        return null;
    }

    @Override
    public Message associateRequest(final ParameterList params) {
        return this.serverManager.associationResponse(params);
    }

    @Override
    public Message verifyRequest(final ParameterList params) {
        return this.serverManager.verify(params);
    }

    @Override
    public Message authResponse(final ParameterList params, final String userSelectedId,
            final String userSelectedClaimedId, final boolean authenticatedAndApproved,
            final String opEndpointUrl) throws MessageException, ServerException,
            AssociationException {
        return this.serverManager.authResponse(params, userSelectedId,
                userSelectedClaimedId, authenticatedAndApproved, opEndpointUrl, true);
    }

}
