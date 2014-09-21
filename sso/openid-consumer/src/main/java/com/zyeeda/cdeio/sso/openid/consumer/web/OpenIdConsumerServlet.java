/* $Id: OpenIdConsumerServlet.java,v 49501679b751 2013/09/02 16:18:32 tangrui $ */

package com.zyeeda.cdeio.sso.openid.consumer.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.cdeio.sso.openid.consumer.OpenIdConsumer;
import com.zyeeda.cdeio.sso.openid.consumer.support.HttpSessionOpenIdConsumer;

/**
 * OpenID consumer servlet.
 *
 * @author $Author: tangrui $
 *
 */
public abstract class OpenIdConsumerServlet extends HttpServlet {

    /**
     * 序列化版本.
     */
    private static final long serialVersionUID = -7675054655776592660L;

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdConsumerServlet.class);

    /**
     * redirectUrl 键.
     */
    private static final String REDIRECT_URL_KEY = "redirectUrl";

    /**
     * serverProtocol 键.
     */
    private static final String SERVER_PROTOCOL_KEY = "serverProtocol";

    /**
     * serverAddress 键.
     */
    private static final String SERVER_ADDRESS_KEY = "serverAddress";

    /**
     * serverPort 键.
     */
    private static final String SERVER_PORT_KEY = "serverPort";

    /**
     * indexPath 键.
     */
    private static final String INDEX_PATH_KEY = "indexPath";

    /**
     * basePath 键.
     */
    private static final String BASE_PATH_KEY = "basePath";

    /**
     * providerServerProtocol 键.
     */
    private static final String PROVIDER_SERVER_PROTOCOL_KEY = "providerServerProtocol";

    /**
     * providerServerAddress 键.
     */
    private static final String PROVIDER_SERVER_ADDRESS_KEY = "providerServerAddress";

    /**
     * providerServerPort 键.
     */
    private static final String PROVIDER_SERVER_PORT_KEY = "providerServerPort";

    /**
     * providerBasePath 键.
     */
    private static final String PROVIDER_BASE_PATH_KEY = "providerBasePath";

    /**
     * openidConsumer 键.
     */
    private static final String OPENID_CONSUMER_KEY = "openidConsumer";

    /**
     * 返回地址.
     */
    private String redirectUrl;

    /**
     * OpenID consumer 对象.
     */
    private OpenIdConsumer openIdConsumer;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        this.openIdConsumer = (OpenIdConsumer) this.getServletContext().getAttribute(OPENID_CONSUMER_KEY);
        if (this.openIdConsumer == null) {
            try {
                this.redirectUrl = this.getParameter(config, REDIRECT_URL_KEY);

                this.openIdConsumer = new HttpSessionOpenIdConsumer(this.getServletContext());

                String serverProtocol = config.getInitParameter(SERVER_PROTOCOL_KEY);
                if (this.isBlank(serverProtocol)) {
                    serverProtocol = "http";
                }
                this.openIdConsumer.setServerProtocol(serverProtocol);
                this.openIdConsumer.setServerAddress(this.getParameter(config, SERVER_ADDRESS_KEY));

                String serverPortString = config.getInitParameter(SERVER_PORT_KEY);
                if (this.isBlank(serverPortString)) {
                    serverPortString = "80";
                }
                int serverPort = Integer.parseInt(serverPortString);
                this.openIdConsumer.setServerPort(serverPort);
                this.openIdConsumer.setIndexPath(this.getParameter(config, INDEX_PATH_KEY));
                this.openIdConsumer.setBasePath(this.getParameter(config, BASE_PATH_KEY));

                String providerServerProtocol = config.getInitParameter(PROVIDER_SERVER_PROTOCOL_KEY);
                if (this.isBlank(providerServerProtocol)) {
                    providerServerProtocol = "http";
                }
                this.openIdConsumer.setProviderServerProtocol(providerServerProtocol);
                this.openIdConsumer.setProviderServerAddress(this.getParameter(config, PROVIDER_SERVER_ADDRESS_KEY));

                String providerServerPortString = config.getInitParameter(PROVIDER_SERVER_PORT_KEY);
                if (this.isBlank(providerServerPortString)) {
                    providerServerPortString = "9100";
                }
                int providerServerPort = Integer.parseInt(providerServerPortString);
                this.openIdConsumer.setProviderServerPort(providerServerPort);
                this.openIdConsumer.setProviderBasePath(this.getParameter(config, PROVIDER_BASE_PATH_KEY));

                this.getServletContext().setAttribute(OPENID_CONSUMER_KEY, this.openIdConsumer);
            } catch (ConsumerException e) {
                throw new ServletException(e);
            }
        }
        LOGGER.info("Initialized OpenID Consumer Servlet.");
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        LOGGER.debug("servlet path info = {}", request.getPathInfo());

        if ("/verify".equals(request.getPathInfo())) {
            LOGGER.info("Verify OpenID auth response.");

            try {
                Identifier id = this.openIdConsumer.verifyResponse(request);
                if (id == null) {
                    LOGGER.info("Access denied");
                    this.onAccessDenied(request, response);
                    return;
                }

                LOGGER.info("OpenID identifier = {}", id.getIdentifier());
                this.onAccessGranted(request, response);
                return;
            } catch (OpenIDException e) {
                throw new ServletException(e);
            }
        }

        try {
            LOGGER.info("Send OpenID auth request.");
            AuthRequest authReq = this.openIdConsumer.authRequest(this.openIdConsumer.getProviderXrdsUrl(), request, response);
            request.setAttribute("message", authReq);
            request.getRequestDispatcher(this.redirectUrl).forward(request, response);
        } catch (OpenIDException e) {
            throw new ServletException(e);
        }
    }

    /**
     * 获取 OpenID consumer 对象.
     *
     * @return OpenID consumer 对象
     */
    protected OpenIdConsumer getOpenIdConsumer() {
        return this.openIdConsumer;
    }

    /**
     * 当访问通过时调用此方法.
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     */
    protected abstract void onAccessGranted(HttpServletRequest request, HttpServletResponse response);

    /**
     * 当放被拒绝时调用此方法.
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     */
    protected abstract void onAccessDenied(HttpServletRequest request, HttpServletResponse response);

    /**
     * 判断传入的字符串是否为空.
     *
     * @param str 待判断的字符串
     * @return 为空则返回 true，否则返回 false
     */
    private boolean isBlank(final String str) {
        if (str == null || "".equals(str)) {
            return true;
        }

        return false;
    }

    /**
     * 检查初始化参数是否为空.
     *
     * @param config ServetContext 对象
     * @param parameterName 初始化参数名称
     * @return 当初始化参数为空时抛出异常，否则返回该参数值.
     */
    private String getParameter(final ServletConfig config, final String parameterName) {
        String value = config.getInitParameter(parameterName);
        if (this.isBlank(value)) {
            throw new IllegalArgumentException("Init parameter " + parameterName + " cannot be empty.");
        }
        return value;
    }

}
