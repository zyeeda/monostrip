/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.support;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.cdeio.commons.crypto.BCrypt;

/**
 * 基于 BCrypt 算法的 PasswordService 实现.
 *
 * @author $Author$
 *
 */
public class BCryptPasswordService implements PasswordService {

    /**
     * 日志对象.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BCryptPasswordService.class);

    @Override
    public String encryptPassword(final Object plaintextPassword) {
        ByteSource source = ByteSource.Util.bytes(plaintextPassword);
        String password = CodecSupport.toString(source.getBytes());
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public boolean passwordsMatch(final Object submittedPlaintext, final String encrypted) {
        ByteSource source = ByteSource.Util.bytes(submittedPlaintext);
        String password = CodecSupport.toString(source.getBytes());
        try {
            return BCrypt.checkpw(password, encrypted);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

}
