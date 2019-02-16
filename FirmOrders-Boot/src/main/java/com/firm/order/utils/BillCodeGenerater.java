package com.firm.order.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BillCodeGenerater {

	private static String ZEROFILL;
	private static BillCodeGenerater billCodeGenerater = null;

	/**
	 * BillCodeGenerater的单例实现
	 *
	 * @return
	 */
	public static BillCodeGenerater getInstance() {
		if (billCodeGenerater == null) {
			synchronized (BillCodeGenerater.class) {
				if (billCodeGenerater == null) {
					billCodeGenerater = new BillCodeGenerater();
				}
			}
		}
		return billCodeGenerater;
	}

	/**
	 * 获取编码 日期+随机数流水号
	 * 
	 * @param dateFormat
	 * @throws Exception
	 */
	public String generaterBillCode(String dateFormat) {
		String id = "";
		try {
			if (StringUtils.isNotBlank(dateFormat)) {
				throw new Exception("日期格式不能为空!");
			}
			SimpleDateFormat sf = new SimpleDateFormat(dateFormat);
			String temp = sf.format(new Date());
			int random = (int) (Math.random() * 10000);
			id = temp + random;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return id;
	}

	/**
	 * 
	 * 获取编码 prefix + 年月 + ...0 + 1,每月从1开始计数
	 * @param prefix
	 *            编码前缀
	 * @param dateFormat
	 *            日期格式
	 * @param previous
	 *            上一个最近编码
	 * @return
	 * @throws Exception
	 */
	/**
	 * 获取编码 prefix + 年月 + ...0 + 1,每月从1开始计数
	 * @param prefix   编码前缀
	 * @param dateFormat 日期格式
	 * @param digits 流水号位数，如参数previous不为空，则按dateFormat格式取previous中流水位数
	 * @param previous 上一个最近编码,可为空
	 * @return
	 */
	public static synchronized String generaterBillCode(String prefix,Date date, String dateFormat,int digits,String previous) {
		String code = null;
		try {
			if (StringUtils.isBlank(dateFormat)) {
				throw new Exception("日期格式不能为空!");
			}
			if(digits <= 0){
				throw new Exception("流水号位只能大于0!");
			}
			//Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
			if (StringUtils.isBlank(previous)) {
				//上一个最近编码为空
				if (digits > 1) {
					code = prefix + formatter.format(date) + String.format( "%0" + digits + "d", 1);
				} else if (digits == 1) {
					code = prefix + formatter.format(date) + 1;
				}
			}else{
				int subBeginIndex = prefix.length() + dateFormat.length();
				String previousDatePart = previous.substring(prefix.length(), subBeginIndex);
				int serialDigits = previous.length() - subBeginIndex;
				if (serialDigits > 1) {
					ZEROFILL = "%0" + serialDigits + "d";
				} else if (serialDigits == 1) {
					ZEROFILL = null;
				}
				// 上一个最近编码是不是当月的
				if (!formatter.format(date).equals(previousDatePart)) {
					previousDatePart = formatter.format(date);
					//上一个最近编码不是当月,则取当月日期+(serialDigits-1)个0+1,如:S7001180921001
					if (ZEROFILL != null) {
						code = prefix + previousDatePart + String.format(ZEROFILL, 1);
					} else {
						code = prefix + previousDatePart + 1;
					}

				} else {
					if (ZEROFILL != null) {
						// 上一个最近编码当月，则累加
						code = prefix + formatter.format(date)
								+ String.format(ZEROFILL, 1 + Integer.parseInt(previous.substring(subBeginIndex)));
					} else {
						code = prefix + previousDatePart + (1 + Integer.parseInt(previous.substring(subBeginIndex)));
					}

				}
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return code;
	}

	public static void main(String[] args) {

		String prefix = "S7001";
		String previous = "S70011809218";
		Date date = new Date();
		try {
			System.out.println(generaterBillCode(prefix, date,"yyMMdd",3, previous));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
