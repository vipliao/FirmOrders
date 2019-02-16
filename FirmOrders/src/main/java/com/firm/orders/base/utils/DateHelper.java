package com.firm.orders.base.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHelper {

	/**
	 * 获取某月最后一天
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfMonth(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date); 
		cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DATE));
		return cal.getTime();
	}
	
	/**
	 * 获取某月最后一天
	 * @param date
	 * @return
	 */
	public static String getLastDayOfMonthStr(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date); 
		cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DATE));
		return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
	}
	
	/**
	 * 获取某月最后一天
	 * 月份是从0开始的，比如说如果输入5的话，实际上显示的是4月份的最后一天，所以月份减去1了  
	 * @param year
	 * @param month
	 * @return
	 */
	public static String getLastDayOfMonthStr(int year,int month){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year); 
		cal.set(Calendar.MONTH,month-1);
		cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DATE));
		return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
	}
	
	/**
	 * 获取某月第一天
	 * @param year
	 * @param month
	 * @return
	 */
	public static  Date getFirstDayOfMonth(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date); 
		cal.set(Calendar.DAY_OF_MONTH,cal.getActualMinimum(Calendar.DATE));
		return cal.getTime();
	}
	
	/**
	 * 获取某月第一天
	 * @param year
	 * @param month
	 * @return
	 */
	public static String getFirstDayOfMonthStr(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date); 
		cal.set(Calendar.DAY_OF_MONTH,cal.getActualMinimum(Calendar.DATE));
		return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
	}
	
	
	/**
	 * 获取某月第一天
	 * @param year
	 * @param month
	 * @return
	 */
	public static String getFirstDayOfMonthStr(int year,int month){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year); 
		cal.set(Calendar.MONTH,month-1);
		cal.set(Calendar.DAY_OF_MONTH,cal.getActualMinimum(Calendar.DATE));
		return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
	}
	
	
}
