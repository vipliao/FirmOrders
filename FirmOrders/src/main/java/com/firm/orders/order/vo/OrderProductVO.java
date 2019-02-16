package com.firm.orders.order.vo;

import java.math.BigDecimal;
import com.firm.orders.base.vo.SuperSubVO;

public class OrderProductVO extends SuperSubVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String productId;

	private String productName;
	
	private BigDecimal pnumber;
	
	private String productUnit;
	
	private String productBarCode;
	
	private String orderId;
	
	private BigDecimal productCostPrice;
	
	private int productWarehouse;

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

	public int getProductWarehouse() {
		return productWarehouse;
	}

	public void setProductWarehouse(int productWarehouse) {
		this.productWarehouse = productWarehouse;
	}

}
