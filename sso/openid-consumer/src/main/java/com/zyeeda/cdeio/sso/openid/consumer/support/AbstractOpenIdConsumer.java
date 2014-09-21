/* $Id: AbstractOpenIdConsumer.java,v 49501679b751 2013/09/02 16:18:32 tangrui $ */

package com.zyeeda.cdeio.sso.openid.consumer.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.cdeio.sso.openid.consumer.OpenIdConsumer;

/**
 * OpenID consumer 抽象类.
 *
 * @author $Author: tangrui $
 *
 */
public abstract class AbstractOpenIdConsumer implements OpenIdConsumer {

    /**
     * 将发现信息存储在会话 里时使用的键值.
     */
    protected static final String OPENID_DISCOVERED_KEY = "openid.discovered";

    /**
     * 将 ID 信息存储在会话里时使用的键值.
     */
    protected static final String OPENID_IDENTIFIER_KEY = "openid.identifier";

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOpenIdConsumer.class);

    /**
     * 默认连接超时时间.
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 300000;

    /**
     * 默认 socket 超时时间.
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 300000;

    /**
     * 默认 nonce 最大年龄.
     */
    private static final int DEFAULT_NONCE_MAX_AGE = 60000;

    /**
     * 默认服务器端口.
     */
    private static final int DEFAULT_SERVER_PORT = 80;

    /**
     * 默认 OP 端服务器端口.
     */
    private static final int DEFAULT_PROVIDER_SERVER_PORT = 9100;

    /**
     * ConsumerManager 对象.
     */
    private ConsumerManager manager;

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
     * OP 端服务器协议.
     */
    private String providerServerProtocol;

    /**
     * 服务器地址.
     */
    private String serverAddress;

    /**
     * OP 端服务器地址.
     */
    private String providerServerAddress;

    /**
     * 服务器端口.
     */
    private int serverPort = DEFAULT_SERVER_PORT;

    /**
     * OP 端服务器端口.
     */
    private int providerServerPort = DEFAULT_PROVIDER_SERVER_PORT;

    /**
     * 首页路径.
     */
    private String indexPath;

    /**
     * Base 路径.
     */
    private String basePath;

    /**
     * OP 端 base 路径.
     */
    private String providerBasePath;

    /**
     * 默认构造方法.
     *
     * @throws ConsumerException 构造失败时抛出
     */
    public AbstractOpenIdConsumer() throws ConsumerException {
        this.manager = new ConsumerManager();
        this.manager.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        this.manager.setSocketTimeout(DEFAULT_SOCKET_TIMEOUT);
        this.manager.setAssociations(new InMemoryConsumerAssociationStore());
        this.manager.setNonceVerifier(new InMemoryNonceVerifier(DEFAULT_NONCE_MAX_AGE));
    }

    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setName(final String name) {
        LOGGER.debug("application name = {}", name);
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setServerProtocol(final String serverProtocol) {
        LOGGER.debug("server protocol = {}", serverProtocol);
        this.serverProtocol = serverProtocol;
    }

    @Override
    public void setProviderServerProtocol(final String providerServerProtocol) {
        LOGGER.debug("provider server protocol = {}", providerServerProtocol);
        this.providerServerProtocol = providerServerProtocol;
    }

    @Override
    public void setServerAddress(final String serverAddress) {
        LOGGER.debug("server address = {}", serverAddress);
        this.serverAddress = serverAddress;
    }

    @Override
    public void setProviderServerAddress(final String providerServerAddress) {
        LOGGER.debug("provider server address = {}", providerServerAddress);
        this.providerServerAddress = providerServerAddress;
    }

    @Override
    public void setServerPort(final int serverPort) {
        LOGGER.debug("server port = {}", serverPort);
        this.serverPort = serverPort;
    }

    @Override
    public void setProviderServerPort(final int providerServerPort) {
        LOGGER.debug("provider server port = {}", providerServerPort);
        this.providerServerPort = providerServerPort;
    }

    @Override
    public void setIndexPath(final String indexPath) {
        LOGGER.debug("index path = {}", indexPath);
        this.indexPath = indexPath;
    }

    @Override
    public void setBasePath(final String basePath) {
        LOGGER.debug("base path = {}", basePath);
        this.basePath = basePath;
    }

    @Override
    public void setProviderBasePath(final String providerBasePath) {
        LOGGER.debug("provider base path = {}", providerBasePath);
        this.providerBasePath = providerBasePath;
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
    public String getReturnToPath() {
        return this.basePath + "/verify";
    }

    @Override
    public String getReturnToUrl() {
        return this.pathToUrl(this.getReturnToPath());
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
    public String getProviderXrdsPath() {
        return this.providerBasePath + "/xrds";
    }

    @Override
    public String getProviderXrdsUrl() {
        return this.providerPathToUrl(this.getProviderXrdsPath());
    }

    @Override
    public String getProviderSignInPath() {
        return this.providerBasePath + "/signin";
    }

    @Override
    public String getProviderSignInUrl() {
        return this.providerPathToUrl(this.getProviderSignInPath());
    }

    @Override
    public String getProviderSignOutPath() {
        return this.providerBasePath + "/signout";
    }

    @Override
    public String getProviderSignOutUrl() {
        return this.providerPathToUrl(this.getProviderSignOutPath());
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

    /**
     * 将 OP 端的 path 路径转换为对应的 URL 形式.
     *
     * @param path 路径
     * @return OP 端对应的 URL 形式
     */
    private String providerPathToUrl(final String path) {
        try {
            URL url = new URL(this.providerServerProtocol, this.providerServerAddress, this.providerServerPort, path);
            return url.toString();
        } catch (MalformedURLException e) {
            LOGGER.error("Cannot build provider URL from path.", e);
        }
        return null;
    }


    @Override
    public AuthRequest authRequest(final String userSuppliedId,
            final HttpServletRequest httpReq, final HttpServletResponse httpRes)
            throws OpenIDException {
        LOGGER.info("user supplied id = {}", userSuppliedId);

        List<?> discos = this.manager.discover(userSuppliedId);
        DiscoveryInformation discovered = this.manager.associate(discos);
        this.storeDiscoveryInfo(httpReq, discovered);
        AuthRequest authReq = this.manager.authenticate(discovered, this.getReturnToUrl());

        return authReq;
    }

    @Override
    public Identifier verifyResponse(final HttpServletRequest httpReq)
            throws OpenIDException {
        ParameterList response = new ParameterList(httpReq.getParameterMap());

        DiscoveryInformation discovered = this.retrieveDiscoveryInfo(httpReq);

        StringBuilder receivingURL = new StringBuilder(this.getReturnToUrl());
        String queryString = httpReq.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            receivingURL.append("?").append(httpReq.getQueryString());
        }

        VerificationResult verification = this.manager.verify(receivingURL.toString(), response, discovered);

        Identifier verified = verification.getVerifiedId();
        if (verified == null) {
            throw new OpenIDException("Cannot verify OpenID auth response.");
        }

        this.storeIdentifier(httpReq, verified);
        return verified;
    }

    /**
     * 存储发现信息.
     *
     * @param httpReq HTTP 请求
     * @param discovered 发现信息
     */
    protected abstract void storeDiscoveryInfo(HttpServletRequest httpReq, DiscoveryInformation discovered);

    /**
     * 提取发现信息.
     *
     * @param httpReq HTTP 请求
     *
     * @return 发现信息
     */
    protected abstract DiscoveryInformation retrieveDiscoveryInfo(HttpServletRequest httpReq);

    /**
     * 存储身份信息.
     *
     * @param httpReq HTTP 请求
     * @param identifier 身份信息
     */
    protected abstract void storeIdentifier(HttpServletRequest httpReq, Identifier identifier);

    /**
     * 获取身份信息.
     *
     * @param httpReq HTTP 请求
     * @return 身份信息
     */
    public abstract Identifier retrieveIdentifier(HttpServletRequest httpReq);

}
