package com.firm.orders.order.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.orders.base.entity.SuperEntity;

@Entity
@Table(name="order_product")
public class OrderProductEntity extends SuperEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Column(name="product_id")
	private String productId;
	
	@Column(name="product_name")
	private String productName;
	
	private BigDecimal pnumber;
	
	@Column(name="product_unit")
	private String productUnit;
	
	@Column(name="product_bar_code")
	private String productBarCode;
	
	@Column(name="order_id")
	private String orderId;
	
	@Column(name="product_cost_price")
	private BigDecimal productCostPrice;
	
	@Column(name="product_warehouse")
	private String productWarehouse;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public BigDecimal getPnumber() {
		return pnumber;
	}

	public void setPnumber(BigDecimal pnumber) {
		this.pnumber = pnumber;
	}

	public String getProductUnit() {
		return productUnit;
	}

	public void setProductUnit(String productUnit) {
		this.productUnit = productUnit;
	}

	public String getProductBarCode() {
		return productBarCode;
	}

	public void setProductBarCode(String productBarCode) {
		this.productBarCode = productBarCode;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public BigDecimal getProductCostPrice() {
		return productCostPrice;
	}

	public void setProductCostPrice(BigDecimal productCostPrice) {
		this.productCostPrice = productCostPrice;
	}

	public String getProductWarehouse() {
		return productWarehouse;
	}

	public void setProductWarehouse(String productWarehouse) {
		this.productWarehouse = productWarehouse;
	}


}
