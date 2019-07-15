package com.firm.order.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EncryptHelper {

    private static String SERVER_PRIVATE_KEY = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIJ+grty+95ZeDxMiqFzmhASKrCp55TD" +
            "ECZELmppaqFk2cJhkYriw6E2ZHUsdGwQuw9Qo8F8Ce6OqZHs3ncKKTfz3k8M+pVKnP96zGVhRnGrgFLELzzV8Bl6lgbtHQA/TUaOGEd6OJLR" +
            "HraM7pTFaVSoiFvq0bf0l0ZvFVIXdxElAgMBAAECgYA4Li0sdIF5Jb29ABsDDVj7qEaWzl80TZKLVvlAuOe+WkPiCs11JNGie19ejugEimNuYYJOz" +
            "48bFHJur7LLPyWlHf6tH/j1Xf/HQ4TtFAhAfeFqGcabicbP4ll71/EW6gOjAUL3Yt37Nx/+SWdm66rFrbaoucC3ToAdfhmsMkS+oQJBAOnF6BQ/t4tR/do" +
            "AYmTJj4vrK3PHjybDEp+gYBy9w2ZS+qFD5r31EO8xct7KBZcK6I1dnSzOW0bBdKz2Fw/CBakCQQCO5sVTrceSn9aZsN5LcJD0E9oLisnxm0mIX0Cw/QyIK" +
            "rqlajA/8tiRGdimOYiX5uFBNTGDqyST2tiR/w3rMyUdAkB9wnGvMvfSgzFE7aABCE2ov8KIbnqcJ6UFKomJ7KwJh+o8hwLCfeu2QavCHnwTXVLtecbsK7b+wWO7CEg" +
            "vufjBAkBuRmuUbZCK98nVhIWm1dEmaYNkUNASIHGYSVlUu5JGKiMqr01YrACvyLBFZjSgdEzz0ybIovNxHfruf1KwZCGJAkAzv9erOlEmounOK7grvoRsYPK" +
            "6hhV95adyJGHREFIQmz+JmNweTKFYSBf0Nd72p5fhKrZ5wmMNjSTneYtg1tP2";

    private static String CLIENT_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCl6Fs5ttmiCeGlS+8F0" +
            "V2K7r/swAUie7crUeQ4o3Y3sdSOohsi0SnW6aIq4gcaTUIjr8Zv7AapGPM+cK" +
            "9ac05ZgKKl7aXh7J6iHTs7w2Lo9s6ZFW0ewIweSDhjRUXHBD14W74Rq2CuIs6KxkJ6zWsVABf6Xr+P/J8g9U0yBkZ8rQIDAQAB";

    /**
     * RSA+AES加密
     *
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
     *
     * @param data
     * @return
     */
    public static String decrypt(String data) {
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
        boolean verify = RSAUtils.verify(signSrc, CLIENT_PUBLIC_KEY, sign);
        if (verify) {
            String deData = realData.substring(17, realData.length() - 8 - signEndPartLength);
            String enData = RSAUtils.decryptDataOnJava(deData, SERVER_PRIVATE_KEY);
            JSONObject enData2Obj = JSONObject.parseObject(enData);
            String enAESKey = enData2Obj.get("metadataKey").toString();
            String enAESIv = enData2Obj.get("metadataIv").toString();
            String enAESData = enData2Obj.get("metadata").toString();
            return AesEncryptUtils.decrypt(enAESData, enAESKey, enAESIv);
        }
        return null;

    }
}
