package com.firm.order.modules.warehouse.vo;

import com.firm.order.modules.base.vo.SuperSubVO;

import lombok.Data;

@Data
public class WarehouseVO extends SuperSubVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String code;
	private String ordeCodePrefix;
	private int bizRange;

	
}
