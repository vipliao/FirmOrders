package com.firm.orders.product.entity;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.orders.base.entity.SuperEntity;

@Entity
@Table(name="product_info")
public class ProductEntity extends SuperEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name="warehouse")
	private int wareHouse;
	private String name;
	@Column(name="cost_price")
	private BigDecimal costPrice;
	@Column(name="bar_code")
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
