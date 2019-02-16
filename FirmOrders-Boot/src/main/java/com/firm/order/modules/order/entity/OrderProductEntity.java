package com.firm.order.modules.order.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.order.modules.base.entity.SuperEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="order_product")
@Getter @Setter @ToString
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
	private int productWarehouse;

	
}
