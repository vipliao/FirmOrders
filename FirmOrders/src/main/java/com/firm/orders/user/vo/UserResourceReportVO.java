package com.firm.orders.user.vo;

import java.io.Serializable;

public class UserResourceReportVO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int fristNum;
	private int beforeNum;
	private int currentNum;
	private int addNum;
	private int sumAddNum;
	private String region;
	public int getFristNum() {
		return fristNum;
	}
	public void setFristNum(int fristNum) {
		this.fristNum = fristNum;
	}
	public int getBeforeNum() {
		return beforeNum;
	}
	public void setBeforeNum(int beforeNum) {
		this.beforeNum = beforeNum;
	}
	public int getCurrentNum() {
		return currentNum;
	}
	public void setCurrentNum(int currentNum) {
		this.currentNum = currentNum;
	}
	public int getAddNum() {
		return addNum;
	}
	public void setAddNum(int addNum) {
		this.addNum = addNum;
	}
	public int getSumAddNum() {
		return sumAddNum;
	}
	public void setSumAddNum(int sumAddNum) {
		this.sumAddNum = sumAddNum;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	
	

}
