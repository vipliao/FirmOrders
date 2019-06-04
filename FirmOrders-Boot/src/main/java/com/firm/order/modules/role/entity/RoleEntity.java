package com.firm.order.modules.role.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.order.modules.base.entity.SuperEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="role_info")
@Data 
@EqualsAndHashCode(callSuper=true)
public class RoleEntity extends SuperEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Column(name="role_code")
	private String roleCode;
	@Column(name="role_name")
	private String roleName;
	@Column(name="biz_range")
	private int bizRange;
	private int level;
	
	
}
