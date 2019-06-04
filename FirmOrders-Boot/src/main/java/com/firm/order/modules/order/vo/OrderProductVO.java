package com.firm.order.modules.order.vo;

import java.math.BigDecimal;

import com.firm.order.modules.base.vo.SuperSubVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
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
	
	private String productWarehouse;
	
	private String productWarehouseName;

	

}
