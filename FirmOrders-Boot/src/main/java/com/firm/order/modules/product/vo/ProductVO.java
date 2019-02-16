package com.firm.order.modules.product.vo;

import java.math.BigDecimal;

import com.firm.order.modules.base.vo.SuperVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
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
}
