package com.firm.order.modules.warehouse.vo;

import com.firm.order.modules.base.vo.SuperSubVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter @Setter @ToString
public class WarehouseVO extends SuperSubVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String code;
	private String ordeCodePrefix;
}
