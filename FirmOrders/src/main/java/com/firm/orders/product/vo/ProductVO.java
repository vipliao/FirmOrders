package com.firm.orders.product.vo;

import java.math.BigDecimal;

import com.firm.orders.base.vo.SuperVO;

public class ProductVO extends SuperVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int wareHouse;
	private String name;
	private BigDecimal costPrice;
	private String barCode;
	private String unit;

	
	public int getWareHouse() {
		return wareHouse;
	}
	public void setWareHouse(int wareHouse) {
		this.wareHouse = wareHouse;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigDecimal getCostPrice() {
		return costPrice;
	}
	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}
	public String getBarCode() {
		return barCode;
	}
	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
}
