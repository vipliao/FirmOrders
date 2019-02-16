package com.firm.orders.base.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 汉字转化为拼音的工具类
 * 
 * @author LIAO
 *
 */
public class ChineseToPinyinHelper {

	public static enum Type {
		UPPERCASE, // 全部大写
		LOWERCASE, // 全部小写
		FIRSTUPPER // 首字母大写
	}

	/**
	 * 将str转换成大写拼音，如果不是汉字或者没有对应的拼音，则不作转换
	 * 
	 * @param str：要转化的汉字
	 * @return
	 * @throws BadHanyuPinyinOutputFormatCombination
	 */
	public static String toUpperPinYin(String str) throws BadHanyuPinyinOutputFormatCombination {
		return toPinYin(str, "", Type.UPPERCASE);
	}

	/**
	 * 将str转换成首字母大写拼音，如果不是汉字或者没有对应的拼音，则不作转换
	 * 
	 * @param str：要转化的汉字
	 * @return
	 * @throws BadHanyuPinyinOutputFormatCombination
	 */
	public static String toFirstUppPinYin(String str) throws BadHanyuPinyinOutputFormatCombination {
		return toPinYin(str, "", Type.FIRSTUPPER);
	}

	/**
	 * 将str转换成小写拼音，如果不是汉字或者没有对应的拼音，则不作转换
	 * 
	 * @param str：要转化的汉字
	 * @return
	 * @throws BadHanyuPinyinOutputFormatCombination
	 */
	public static String toLowerPinYin(String str) throws BadHanyuPinyinOutputFormatCombination {
		return toPinYin(str, "", Type.LOWERCASE);
	}

	/**
	 * 将str转换成拼音，如果不是汉字或者没有对应的拼音，则不作转换
	 * 
	 * @param str：要转化的汉字
	 * @param type:类型
	 * @return
	 * @throws BadHanyuPinyinOutputFormatCombination
	 */
	public static String toPinYin(String str, Type type) throws BadHanyuPinyinOutputFormatCombination {
		return toPinYin(str, "", type);
	}

	/**
	 * 将str转换成拼音，如果不是汉字或者没有对应的拼音，则不作转换
	 * 
	 * @param str：要转化的汉字
	 * @param spera：转化结果的分割符
	 * @return
	 * @throws BadHanyuPinyinOutputFormatCombination
	 */
	public static String toPinYin(String str, String spera) throws BadHanyuPinyinOutputFormatCombination {
		return toPinYin(str, spera, Type.UPPERCASE);
	}

	/**
	 * 将str转换成拼音，如果不是汉字或者没有对应的拼音，则不作转换
	 * 
	 * @param str：要转化的汉字
	 * @param spera：转化结果的分割符
	 * @param type：大小写类型
	 * @return
	 * @throws BadHanyuPinyinOutputFormatCombination
	 */
	public static String toPinYin(String str, String spera, Type type) throws BadHanyuPinyinOutputFormatCombination {
		if (str == null || str.trim().length() == 0)
			return "";
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		if (type == Type.UPPERCASE)
			format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		else
			format.setCaseType(HanyuPinyinCaseType.LOWERCASE);

		String py = "";
		String temp = "";
		String[] t;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if ((int) c <= 128)
				py += c;
			else {
				t = PinyinHelper.toHanyuPinyinStringArray(c, format);
				if (t == null)
					py += c;
				else {
					temp = t[0];
					if (type == Type.FIRSTUPPER)
						temp = t[0].toUpperCase().charAt(0) + temp.substring(1);
					py += temp + (i == str.length() - 1 ? "" : spera);
				}
			}
		}
		return py.trim();
	}
}
