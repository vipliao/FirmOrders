package com.firm.orders.warehouse.vo;

import com.firm.orders.base.vo.SuperSubVO;

public class WarehouseVO extends SuperSubVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String code;
	private String ordeCodePrefix;
	private int bizRange;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getOrdeCodePrefix() {
		return ordeCodePrefix;
	}
	public void setOrdeCodePrefix(String ordeCodePrefix) {
		this.ordeCodePrefix = ordeCodePrefix;
	}
	public int getBizRange() {
		return bizRange;
	}
	public void setBizRange(int bizRange) {
		this.bizRange = bizRange;
	}
	
}
