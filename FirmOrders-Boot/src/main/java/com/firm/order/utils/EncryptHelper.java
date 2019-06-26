package com.firm.order.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class EncryptHelper {

    private static String SERVER_PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJ9d8zOCDiblFEuDa9ytgEU3iwGETVW1bWwGRfELnC0JFo5m/SS52T0IhB9Tpz54RppkpSfKDEdZ1Kp3BREITQgqAtyiVcX5Eea7+uX3vsV+NGD2kCQGDnPTk98cOpaqQiypfSJFpI/fUFRk6C3Bp9uIG/Z0G9EEx2rTe3Lirw5BAgMBAAECgYBtt2Ga4XvavTWWs1jL6cr4XD/gAHS5gBlgGIWIaMTRoIMd8Ltw0F5GDZngc1gdwvJgks+9L/X6HVczMJOVYVCeKwuBo6JcgK64lhh58wcOlMVcUSydIN4QXbUcWmPnqTsZ1xErTQSdF6ybbNbP02Kf9VL0c/2SEnFx+hlQv4ZoPQJBAM/HxXGisWmA4WMU6WSw5zN78ms85v9nAUb16eHTxEA+Gopn/0kZVdZG7DOudcC9B3h8Q+pDlZ+aY3JL0246K/cCQQDEWfD4W5xmJsr7Jf/G/i00iqMsEoaosBN8EbuyoTqGAauazqdRREv906y8Z43m+UcdyUyyvev+qya2ohDkz1mHAkBhJ5QXEm/KWU0KO1j2kBFLbYVox01r1wot2AbIZXC6aU+XsBEaaRJN41PGxqigusKnf9Nx0rA/tL0TUIlIXUL5AkEAkbima8uhkPZtYmLbJtXwurPBUa4WHg8/Bq8qe/HIOzf2IZgI+PkU5LY51kGMQMT0EJkawPTOtlxTCOvSWd3A9QJBALYU/fjwJsgA9z6/+yV60JX0TmqrS6HBP4MZ72ICDmgfrL7Fy/q+mmMWn952wxA05eqssl7mnXX0PHekdmrd9JU=";

    private static String CLIENT_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCfXfMzgg4m5RRLg2vcrYBFN4sBhE1VtW1sBkXxC5wtCRaOZv" +
            "0kudk9CIQfU6c+eEaaZKUnygxHWdSqdwURCE0IKgLcolXF+RHmu/rl977FfjRg9pAkBg5z05PfHDqWqkIsq" +
            "X0iRaSP31BUZOgtwafbiBv2dBvRBMdq03ty4q8OQQIDAQAB";

    /**
     * RSA+AES加密
     * @param data
     * @return
     */
    public static String encrypt(Object data) {
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

        return RSAUtils.encryptedDataOnJava(enObj.toJSONString(), CLIENT_PUBLIC_KEY);
    }

    /**
     * RSA+AES解密
     * @param data
     * @return
     */
    public static String decrypt(String data) {
        String enData = RSAUtils.decryptDataOnJava(data, SERVER_PRIVATE_KEY);
        JSONObject enData2Obj = JSONObject.parseObject(enData);
        String enAESKey = enData2Obj.get("metadataKey").toString();
        String enAESIv = enData2Obj.get("metadataIv").toString();
        String enAESData = enData2Obj.get("metadata").toString();
        return AesEncryptUtils.decrypt(enAESData, enAESKey, enAESIv);

    }
}
