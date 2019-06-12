package com.firm.order.config.jasypt;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;

public class FrimEncryptablePropertyDetector implements EncryptablePropertyDetector {

    public static final String ENCODED_PASSWORD_HINT = "dJICJBmLxRDD4e7/1Zekhw";

    
    
    // 如果属性的字符开头为"{cipher}"，返回true，表明该属性是加密过的
    @Override
    public boolean isEncrypted(String s) {
        if (null != s) {
            return s.startsWith(ENCODED_PASSWORD_HINT);
        }
        return false;
    }
    // 该方法告诉工具，如何将自定义前缀去除
    @Override
    public String unwrapEncryptedValue(String s) {
        return s.substring(ENCODED_PASSWORD_HINT.length());
    }
}
