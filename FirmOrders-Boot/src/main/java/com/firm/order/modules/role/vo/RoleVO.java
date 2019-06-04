package com.firm.order.modules.role.vo;

import com.firm.order.modules.base.vo.SuperVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data 
@EqualsAndHashCode(callSuper=true)
public class RoleVO extends SuperVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String roleCode;
	private String roleName;
	private int bizRange;
	private int level;
	
	
}
