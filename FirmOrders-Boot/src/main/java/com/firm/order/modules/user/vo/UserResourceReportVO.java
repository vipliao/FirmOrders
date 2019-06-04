package com.firm.order.modules.user.vo;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data 
public class UserResourceReportVO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int fristNum;
	private int beforeNum;
	private int currentNum;
	private int addNum;
	private int sumAddNum;
	private String region;
}
