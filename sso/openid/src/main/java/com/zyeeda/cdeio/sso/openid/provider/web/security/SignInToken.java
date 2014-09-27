package com.zyeeda.cdeio.sso.openid.provider.web.security;

import java.io.Serializable;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;

/**
 * 安全记录.
 *
 * @author $Author$
 *
 */
public class SignInToken implements Serializable {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = 208441193191324619L;

    /**
     * Nonce.
     */
    private String nonce = new SecureRandomNumberGenerator().nextBytes().toHex();

    /**
     * 时间戳.
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 登录尝试次数.
     */
    private int attempts = 0;

    /**
     * 是否需要验证码.
     */
    private boolean captchaRequired = false;

    public String getNonce() {
        return this.nonce;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * 更新时间戳.
     */
    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public int getAttempts() {
        return this.attempts;
    }

    /**
     * 登录尝试次数加一.
     */
    public void increaseAttempts() {
        ++this.attempts;
    }

    public boolean isCaptchaRequired() {
        return this.captchaRequired;
    }

    public void setCaptchaRequired(final boolean captchaRequired) {
        this.captchaRequired = captchaRequired;
    }

}
