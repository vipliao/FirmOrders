package com.firm.order.modules.warehouse.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.order.modules.base.entity.SuperEntity;

import lombok.Data;

@Entity
@Table(name="warehouse_info")
@Data
public class WarehouseEntity extends SuperEntity{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String code;
	@Column(name="orde_code_prefix")
	private String ordeCodePrefix;
	@Column(name="biz_range")
	private int bizRange;
	
	
}
