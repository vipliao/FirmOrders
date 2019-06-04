package com.firm.order.modules.user.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.firm.order.modules.base.vo.SuperVO;

import lombok.Builder;
import lombok.Data;

@Data 
public class LoginedUserVO extends SuperVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private UserVO loginedUser;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date logintime; 
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date outofServicetime; 
	private String token;
	

}
