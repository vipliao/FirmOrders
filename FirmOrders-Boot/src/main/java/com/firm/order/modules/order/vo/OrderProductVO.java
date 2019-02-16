package com.firm.order.modules.order.vo;

import java.math.BigDecimal;

import com.firm.order.modules.base.vo.SuperSubVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
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

	

}
