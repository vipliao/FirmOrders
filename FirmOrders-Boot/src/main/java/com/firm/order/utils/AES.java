package com.firm.order.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

/**
 * 对称加密解密AES算法
 *
 * @author zhaors
 */
public class AES {

	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final String ALGORITHM = "AES";
	private static final String CHARSET = "utf-8";
	/**
	 * 建议为16位或32位
	 */
	private static final String KEY = "A-16-Byte-keyVal";
	/**
	 * 必须16位
	 * 初始化向量IV不可以为32位，否则异常java.security.InvalidAlgorithmParameterException: Wrong IV length: must be 16 bytes long
	 */
	private static final String IV = "A-16-Byte-String";

	/**
	 * 加密
	 *
	 * @param context
	 * @return
	 */
	public static String encrypt(String context) {
		try {
			byte[] decode = context.getBytes(CHARSET);
			byte[] bytes = createKeyAndIv(decode, Cipher.ENCRYPT_MODE);
			return Base64.getEncoder().encodeToString(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解密
	 *
	 * @param context
	 * @return
	 */
	public static String decrypt(String context) {
		try {
			Base64.Decoder decoder = Base64.getDecoder();
			byte[] decode = decoder.decode(context);
			byte[] bytes = createKeyAndIv(decode, Cipher.DECRYPT_MODE);
			return new String(bytes, CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取key & iv
	 *
	 * @param context
	 * @param opmode
	 * @return
	 * @throws Exception
	 */
	public static byte[] createKeyAndIv(byte[] context, int opmode) throws Exception {
		byte[] key = KEY.getBytes(CHARSET);
		byte[] iv = IV.getBytes(CHARSET);
		return cipherFilter(context, opmode, key, iv);
	}

	/**
	 * 执行操作
	 *
	 * @param context
	 * @param opmode
	 * @param key
	 * @param iv
	 * @return
	 * @throws Exception
	 */
	public static byte[] cipherFilter(byte[] context, int opmode, byte[] key, byte[] iv) throws Exception {
		Key secretKeySpec = new SecretKeySpec(key, ALGORITHM);
		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(opmode, secretKeySpec, ivParameterSpec);
		return cipher.doFinal(context);
	}

	/**
	 * 主方法测试
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		String context = "zhaors";
		System.out.println("元数据" + context);
		String encrypt = encrypt(context);
		System.out.println("加密之后：" + encrypt);
		String decrypt = decrypt(encrypt);
		System.out.println("解密之后：" + decrypt);
	}

}
