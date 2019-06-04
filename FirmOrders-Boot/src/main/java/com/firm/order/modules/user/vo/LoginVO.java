package com.firm.order.modules.user.vo;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
public class LoginVO implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String loginName;
	private String password;
	
	

}
