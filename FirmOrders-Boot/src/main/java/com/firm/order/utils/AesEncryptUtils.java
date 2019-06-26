package com.firm.order.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.Map;

/**
 * 前后端数据传输加密工具类
 * @author pibigstar
 *
 */
public class AesEncryptUtils {
    //可配置到Constant中，并读取配置文件注入
    private static final String KEY = "abcdef0123456789";

    //参数分别代表 算法名称/加密模式/数据填充方式
    private static final String ALGORITHMSTR = "AES/CBC/PKCS5Padding";

    /**
     * 必须16位
     * 初始化向量IV不可以为32位，否则异常java.security.InvalidAlgorithmParameterException: Wrong IV length: must be 16 bytes long
     */
    private static final String IV = "A-16-Byte-String";

    /**
     * 加密
     * @param content 加密的字符串
     * @param encryptKey key值
     * @return
     * @throws Exception
     */
    public static String encrypt(String content, String encryptKey,String encryptIv)  {
        try{
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(encryptIv.getBytes());
            SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec,ivParameterSpec);
            byte[] b = cipher.doFinal(content.getBytes("utf-8"));
            // 采用base64算法进行转码,避免出现中文乱码
            return Base64.encodeBase64String(b);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 解密
     * @param encryptStr 解密的字符串
     * @param decryptKey 解密的key值
     * @return
     * @throws Exception
     */
    public static String decrypt(String encryptStr, String decryptKey,String decryptIv){
        try{
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(decryptIv.getBytes("utf-8"));
            SecretKeySpec secretKeySpec = new SecretKeySpec(decryptKey.getBytes("utf-8"), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec,ivParameterSpec);
            // 采用base64算法进行转码,避免出现中文乱码
            byte[] encryptBytes = Base64.decodeBase64(encryptStr);
            byte[] decryptBytes = cipher.doFinal(encryptBytes);
            return new String(decryptBytes);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(String content) {
        return encrypt(content, KEY,IV);
    }
    public static String decrypt(String encryptStr){
        return decrypt(encryptStr, KEY,IV);
    }

    public static String keyGenerator(){
        try{
            // 1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            // 2.根据ecnodeRules规则初始化密钥生成器
            // 生成一个128位的随机源,根据传入的字节数组
            keygen.init(128, new SecureRandom());
            // 3.产生原始对称密钥
            SecretKey original_key = keygen.generateKey();
            // 4.获得原始对称密钥的字节数组
            byte[] raw = original_key.getEncoded();
            return Base64.encodeBase64String(raw);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String ivGenerator(){
        try{
            Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv2 = params.getParameterSpec(IvParameterSpec.class).getIV();
            return Base64.encodeBase64String(iv2).substring(0,16);
        }catch (Exception e){
            e.printStackTrace();
        }
       return null;
    }

    /**
     * byte数组转化为16进制字符串
     * @param bytes
     * @return
     */
    public static String byteToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String strHex=Integer.toHexString(bytes[i]);
            if(strHex.length() > 3) {
                sb.append(strHex.substring(6));
            } else {
                if(strHex.length() < 2) {
                    sb.append("0" + strHex);
                } else {
                    sb.append(strHex);
                }
            }
        }
        return sb.toString();
    }


    public static void main(String[] args) throws Exception {
        Map map=new HashMap<String,String>();
        map.put("key","value");
        map.put("中文","汉字");
        String content = JSONObject.toJSONString(map);
        System.out.println("加密前：" + content);

        String encrypt = encrypt(content, KEY,IV);
        System.out.println("加密后：" + encrypt);

        String decrypt = decrypt(encrypt, KEY,IV);
        System.out.println("解密后：" + decrypt);
    }
}