package com.firm.orders.sender.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.orders.base.entity.SuperEntity;

@Entity
@Table(name="sender_info")
public class SenderEntity extends SuperEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name="sender_name")
	private String senderName;
	@Column(name="serder_phone")
	private String serderPhone;
	@Column(name="sender_addr")
	private String senderAddr;
	@Column(name="is_enabled")
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
