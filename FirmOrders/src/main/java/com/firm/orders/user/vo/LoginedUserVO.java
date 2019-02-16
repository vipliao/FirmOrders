package com.firm.orders.user.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.firm.orders.base.vo.SuperVO;

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
	public UserVO getLoginedUser() {
		return loginedUser;
	}
	public void setLoginedUser(UserVO loginedUser) {
		this.loginedUser = loginedUser;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Date getOutofServicetime() {
		return outofServicetime;
	}
	public void setOutofServicetime(Date outofServicetime) {
		this.outofServicetime = outofServicetime;
	}
	public Date getLogintime() {
		return logintime;
	}
	public void setLogintime(Date logintime) {
		this.logintime = logintime;
	}
	

}
