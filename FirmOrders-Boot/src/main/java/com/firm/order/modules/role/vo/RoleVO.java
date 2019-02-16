package com.firm.order.modules.role.vo;

import com.firm.order.modules.base.vo.SuperVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter @Setter @ToString
public class RoleVO extends SuperVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String roleCode;
	private String roleName;
	
}
