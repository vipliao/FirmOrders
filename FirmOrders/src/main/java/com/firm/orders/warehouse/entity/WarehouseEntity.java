package com.firm.orders.warehouse.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.orders.base.entity.SuperEntity;

@Entity
@Table(name="warehouse_info")
public class WarehouseEntity extends SuperEntity{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String code;
	@Column(name="orde_code_prefix")
	private String ordeCodePrefix;
	@Column(name="biz_range")
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
