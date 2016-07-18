/* $Id */

package com.zyeeda.cdeio.sso.openid.provider.web.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

import com.zyeeda.cdeio.sso.openid.provider.web.Constants;

/**
 * 防止机器人登录的过滤器.
 *
 * @author $Author$
 *
 */
public class AntiRobotFilter extends FormAuthenticationFilter {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AntiRobotFilter.class);

    /**
     * 默认 cookie 最大年龄，单位为秒.
     */
    private static final int DEFAULT_COOKIE_MAX_AGE = 300;

    /**
     * 默认登录请求的冷却时间，单位为毫秒.
     */
    private static final int DEFAULT_COOLDOWN_TIME = 500;

    /**
     * 默认验证码参数名.
     */
    private static final String DEFAULT_CAPTCHA_PARAM_NAME = "captcha";

    /**
     * Cookie 最大年龄.
     */
    private int cookieMaxAge = DEFAULT_COOKIE_MAX_AGE;

    /**
     * 登录请求冷却时间.
     */
    private int cooldownTime = DEFAULT_COOLDOWN_TIME;

    /**
     * Captcha 参数名称.
     */
    private String captchaParamName = DEFAULT_CAPTCHA_PARAM_NAME;

    /**
     * 缓存对象.
     */
    private Cache cache;

    public void setCookieMaxAge(final int cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }

    public void setCooldownTime(final int cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public void setCaptchaParamName(final String captchaParamName) {
        this.captchaParamName = captchaParamName;
    }

    public void setCache(final Cache cache) {
        this.cache = cache;
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        // 请求登录页面
        if (this.isLoginRequest(request, response)) {
            LOGGER.info("Request sign-in page.");

            HttpServletRequest httpReq = (HttpServletRequest) request;
            HttpServletResponse httpRes = (HttpServletResponse) response;

            Cookie cookie = WebUtils.getCookie(httpReq, Constants.COOKIE_NAME);
            if (cookie == null || StringUtils.isBlank(cookie.getValue())) { // cookie 不存在
                LOGGER.info("Cookie {} does not exist.", Constants.COOKIE_NAME);

                if (this.isLoginSubmission(request, response)) {
                    LOGGER.info("Submit to sign-in page.");

                    // 如果请求中携带验证码，且验证码正确匹配，则直接通过
                    if (this.captchaProvided(httpReq) && this.captchaMatches(httpReq)) {
                        LOGGER.info("Captcha required and matches.");
                        return true;
                    }

                    request.setAttribute(this.getFailureKeyAttribute(), "INVALID_SIGN_IN_TOKEN");
                    return true;
                }

                this.generateSignInToken(httpReq, httpRes);
            } else { // cookie 存在
                LOGGER.info("Cookie {} exists, value = {}.", Constants.COOKIE_NAME, cookie.getValue());

                if (this.isLoginSubmission(request, response)) {
                    LOGGER.info("Submit to sign-in page.");

                    Element e = this.cache.get(cookie.getValue());
                    if (e == null) { // token 不存在或已过期
                        LOGGER.info("Sign-in token does not exist or is expired.");
                        request.setAttribute(this.getFailureKeyAttribute(), "INVALID_SIGN_IN_TOKEN");
                        return true;
                    } else { // token 存在
                        LOGGER.info("Sign-in token exists.");
                        SignInToken token = (SignInToken) e.getObjectValue();
                        httpReq.setAttribute(Constants.SIGN_IN_TOKEN_ATTRIBUTE_NAME, token);

                        if (this.captchaRequired(httpReq)) {
                            LOGGER.info("Captcha required!");
                            if (!this.captchaProvided(httpReq) || !this.captchaMatches(httpReq)) {
                                LOGGER.info("Captcha not matches.");
                                request.setAttribute(this.getFailureKeyAttribute(), "INVALID_CAPTCHA");
                                return true;
                            }
                        }

                        // 登录请求间隔小于冷却时间，疑似机器人
                        if (System.currentTimeMillis() - token.getTimestamp() <= this.cooldownTime) {
                            LOGGER.info("Request too fast, maybe a robot.");
                            token.setCaptchaRequired(true);
                            request.setAttribute(this.getFailureKeyAttribute(), "REQUEST_TOO_FAST");
                            return true;
                        }
                    }
                }
            }
        }

        // 其他情况，直接通过
        return true;
    }

    /**
     * 判断 Http 请求中是否携带验证码.
     *
     * @param httpReq Http 请求
     * @return 请求中携带验证码则返回 true, 否则返回 false
     */
    private boolean captchaProvided(final HttpServletRequest httpReq) {
        String captcha = httpReq.getParameter(this.captchaParamName);
        return StringUtils.isNotBlank(captcha);
    }

    /**
     * 判断验证码是否匹配.
     *
     * @param httpReq Http 请求
     * @return 验证码匹配则返回 true, 否则返回 false
     */
    private boolean captchaMatches(final HttpServletRequest httpReq) {
        String expected = (String) httpReq.getSession().getAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
        String present = httpReq.getParameter(this.captchaParamName);
        HttpSession session = httpReq.getSession();
        session.removeAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
        session.removeAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_DATE);
        return StringUtils.equalsIgnoreCase(expected, present);
    }

    /**
     * 是否要求请求中携带验证码.
     *
     * @param httpReq Http 请求
     * @return 要求携带验证码则返回 true，否则返回 false
     */
    private boolean captchaRequired(final HttpServletRequest httpReq) {
        SignInToken token = (SignInToken) httpReq.getAttribute(Constants.SIGN_IN_TOKEN_ATTRIBUTE_NAME);
        return token.isCaptchaRequired();
    }

    /**
     * 生成 SignInToken cookie 和缓存对象.
     * @param httpReq HttpServletRequest 对象
     * @param httpRes HttpServletResponse 对象
     * @return 生成的 SignInToken 对象
     */
    private SignInToken generateSignInToken(final HttpServletRequest httpReq, final HttpServletResponse httpRes) {
        LOGGER.info("Generate the sign-in token and add cookie to response.");

        SignInToken token = new SignInToken();
        Element e = new Element(token.getNonce(), token);
        this.cache.put(e);
        httpReq.setAttribute(Constants.SIGN_IN_TOKEN_ATTRIBUTE_NAME, token);

        CookieGenerator cg = new CookieGenerator();
        cg.setCookieName(Constants.COOKIE_NAME);
        cg.setCookieMaxAge(this.cookieMaxAge);
        cg.addCookie(httpRes, token.getNonce());

        return token;
    }

}
