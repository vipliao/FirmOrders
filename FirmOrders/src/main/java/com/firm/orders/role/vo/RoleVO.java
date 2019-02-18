package com.firm.orders.role.vo;

import com.firm.orders.base.vo.SuperVO;

public class RoleVO extends SuperVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String roleCode;
	private String roleName;
	private int bizRange;
	public String getRoleCode() {
		return roleCode;
	}
	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public int getBizRange() {
		return bizRange;
	}
	public void setBizRange(int bizRange) {
		this.bizRange = bizRange;
	}
	
}
