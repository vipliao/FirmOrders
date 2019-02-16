package com.firm.orders.user.vo;

import com.firm.orders.base.vo.SuperSubVO;

public class UserOwnResourceVO extends SuperSubVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String userId;
	private String resourceWechatCode;
	private String resourcePhone;
	private int minFans;
	
	private UserVO parent;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getResourceWechatCode() {
		return resourceWechatCode;
	}
	public void setResourceWechatCode(String resourceWechatCode) {
		this.resourceWechatCode = resourceWechatCode;
	}
	public String getResourcePhone() {
		return resourcePhone;
	}
	public void setResourcePhone(String resourcePhone) {
		this.resourcePhone = resourcePhone;
	}
	public int getMinFans() {
		return minFans;
	}
	public void setMinFans(int minFans) {
		this.minFans = minFans;
	}
	public UserVO getParent() {
		return parent;
	}
	public void setParent(UserVO parent) {
		this.parent = parent;
	}

}
