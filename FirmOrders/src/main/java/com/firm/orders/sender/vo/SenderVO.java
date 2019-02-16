package com.firm.orders.sender.vo;

import com.firm.orders.base.vo.SuperVO;

public class SenderVO extends SuperVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String senderName;
	private String serderPhone;
	private String senderAddr;
	private int isEnabled;
	
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public String getSerderPhone() {
		return serderPhone;
	}
	public void setSerderPhone(String serderPhone) {
		this.serderPhone = serderPhone;
	}
	public String getSenderAddr() {
		return senderAddr;
	}
	public void setSenderAddr(String senderAddr) {
		this.senderAddr = senderAddr;
	}
	public int getIsEnabled() {
		return isEnabled;
	}
	public void setIsEnabled(int isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	

}
