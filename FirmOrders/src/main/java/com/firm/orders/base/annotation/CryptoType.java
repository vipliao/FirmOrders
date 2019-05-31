package com.firm.orders.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 秘密类型
 * @author ldy
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CryptoType {

	Type value() default Type.NONE;
	
	enum Type {
		/**
		 * 无
		 */
		NONE,
		/**
		 * 签名
		 */
		SIGN, 
		/**
		 * 加密
		 */
		CRYPTO
	}
}
