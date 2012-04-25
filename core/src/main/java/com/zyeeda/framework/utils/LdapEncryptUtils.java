package com.zyeeda.framework.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class LdapEncryptUtils {
	
	public static String md5Encode(String standardMd5) throws UnsupportedEncodingException {
//		String standardMd5 = DigestUtils.md5Hex(password);
		byte[] ba = new byte[standardMd5.length() / 2];
		for (int i = 0; i < standardMd5.length(); i = i + 2) {
			ba[i == 0 ? 0 : i / 2] = (byte) (0xff & Integer.parseInt(
					standardMd5.substring(i, i + 2), 16));
		}
		Base64 base64 = new Base64();
		return new String(base64.encode(ba), "UTF-8").trim();
	}
	
	public static boolean verifySHA(String ldapPw, String inputPw)
			throws NoSuchAlgorithmException {

		// MessageDigest 提供了消息摘要算法，如 MD5 或 SHA，的功能，这里LDAP使用的是SHA-1
		MessageDigest md = MessageDigest.getInstance("SHA-1");

		// 取出加密字符
		if (ldapPw.startsWith("{SSHA}")) {
			ldapPw = ldapPw.substring(6);
		} else if (ldapPw.startsWith("{SHA}")) {
			ldapPw = ldapPw.substring(5);
		}

		// 解码BASE64
//		byte[] ldapPwByte = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(ldapPw);
		byte[] ldapPwByte = Base64.decodeBase64(ldapPw);
		byte[] shaCode;
		byte[] salt;

		// 前20位是SHA-1加密段，20位后是最初加密时的随机明文
		if (ldapPwByte.length <= 20) {
			shaCode = ldapPwByte;
			salt = new byte[0];
		} else {
			shaCode = new byte[20];
			salt = new byte[ldapPwByte.length - 20];
			System.arraycopy(ldapPwByte, 0, shaCode, 0, 20);
			System.arraycopy(ldapPwByte, 20, salt, 0, salt.length);
		}

		// 把用户输入的密码添加到摘要计算信息
		md.update(inputPw.getBytes());
		// 把随机明文添加到摘要计算信息
		md.update(salt);

		// 按SSHA把当前用户密码进行计算
		byte[] inputPwByte = md.digest();

		// 返回校验结果
		return MessageDigest.isEqual(shaCode, inputPwByte);
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		String str = "e10adc3949ba59abbe56e057f20f883e";
		System.out.println(md5Encode(str));
		
		//e10adc3949ba59abbe56e057f20f883e
		System.out.println(DigestUtils.md5Hex("admin"));
		System.out.println(verifySHA("{SSHA}RphV6QRjgL1WMfCQOkpzwhHrXSUbpfAKnshKjA==", DigestUtils.md5Hex("111111")));
	}
}
