package com.firm.order.modules.base.encrypt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.firm.order.config.context.ApplicationProperties;
import com.firm.order.utils.AesEncryptUtils;
import com.firm.order.utils.RSAUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EncryptHelper {

    private static ApplicationProperties applicationProperties;

    @Autowired
    public void init(ApplicationProperties applicationProperties) {
        EncryptHelper.applicationProperties = applicationProperties;
    }

    /**
     * RSA+AES加密
     *
     * @param data
     */
    public static String encrypt(Object data) {
        return encrypt(data, applicationProperties.getClientPublickey());
    }

    /**
     * RSA+AES加密
     *
     * @param data
     * @param clientPublicKey
     * @return
     */
    public static String encrypt(Object data, String clientPublicKey) {
        String metadataKey = AesEncryptUtils.keyGenerator();
        String metadataIv = AesEncryptUtils.ivGenerator();
        String enBody = null;
        if (data != null) {
            enBody = AesEncryptUtils.encrypt(
                    JSON.toJSONString(data, SerializerFeature.PrettyFormat,
                            SerializerFeature.WriteMapNullValue,
                            SerializerFeature.WriteNullStringAsEmpty,
                            SerializerFeature.DisableCircularReferenceDetect,
                            SerializerFeature.WriteNullListAsEmpty,
                            SerializerFeature.WriteDateUseDateFormat),
                    metadataKey,
                    metadataIv
            );
        }

        JSONObject enObj = new JSONObject();
        enObj.put("metadataKey", metadataKey);
        enObj.put("metadataIv", metadataIv);
        enObj.put("metadata", enBody);

        return RSAUtils.encryptedDataOnJava(enObj.toJSONString(), clientPublicKey);
    }


    /**
     * RSA+AES解密
     *
     * @param data
     * @return
     */
    public static String decrypt(String data) {
        return decrypt(data, applicationProperties.getClientPublickey(), applicationProperties.getServerPrivatekey());
    }

    /**
     * RSA+AES解密
     *
     * @param data
     * @param clientPublicKey
     * @param serverPrivateKey
     * @return
     */
    public static String decrypt(String data, String clientPublicKey, String serverPrivateKey) {
        String realData = StringUtils.substringBeforeLast(data, "/");
        String signBiginPart = realData.substring(0, 9);
        String signSrcBeginPart = realData.substring(9, 17);
        String signSrcEndPart = realData.substring(realData.length() - 8);
        String interStr = StringUtils.substringAfterLast(data, "/");
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(interStr);
        int signEndPartLength = Integer.parseInt(m.replaceAll("").trim());
        String signEndPart = realData.substring(realData.length() - 8 - signEndPartLength, realData.length() - 8);
        String sign = signBiginPart + signEndPart;
        String signSrc = signSrcBeginPart + signSrcEndPart;
        boolean verify = RSAUtils.verify(signSrc, clientPublicKey, sign);
        if (verify) {
            String deData = realData.substring(17, realData.length() - 8 - signEndPartLength);
            String enData = RSAUtils.decryptDataOnJava(deData, serverPrivateKey);
            JSONObject enData2Obj = JSONObject.parseObject(enData);
            String enAESKey = enData2Obj.get("metadataKey").toString();
            String enAESIv = enData2Obj.get("metadataIv").toString();
            String enAESData = enData2Obj.get("metadata").toString();
            return AesEncryptUtils.decrypt(enAESData, enAESKey, enAESIv);
        }
        return null;

    }
}
