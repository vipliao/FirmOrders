package com.firm.order.modules.product.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.order.modules.base.entity.SuperEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="product_info")
@Data
@EqualsAndHashCode(callSuper=true)
public class ProductEntity extends SuperEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name="warehouse")
	private String wareHouse;
	private String name;
	@Column(name="cost_price")
	private BigDecimal costPrice;
	@Column(name="bar_code")
	private String barCode;
	private String unit;

}
