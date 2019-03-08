package com.firm.order.config.jasypt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.firm.order.utils.SymmetricEncoder;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;

public class FirmEncryptablePropertyResolver implements EncryptablePropertyResolver {

	@Value("${encrypt.encodeRules}")
	private String encodeRules;

	

	// 解密方法
	@Override
	public String resolvePropertyValue(String arg) {
		if (null != arg && arg.startsWith(FrimEncryptablePropertyDetector.ENCODED_PASSWORD_HINT)) {
			return SymmetricEncoder.AESDncode(encodeRules,
					arg.substring(FrimEncryptablePropertyDetector.ENCODED_PASSWORD_HINT.length()));
		}
		return arg;
	}

}
