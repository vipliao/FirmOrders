package com.firm.order.modules.product.vo;

import java.math.BigDecimal;

import com.firm.order.modules.base.vo.SuperVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ProductVO extends SuperVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String wareHouse;
	private String wareHouseName;
	
	private String name;
	private BigDecimal costPrice;
	private String barCode;
	private String unit;


}
