package com.firm.order.modules.user.vo;

import com.firm.order.modules.base.vo.SuperSubVO;

import lombok.Builder;
import lombok.Data;

@Data
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
	
	

}
