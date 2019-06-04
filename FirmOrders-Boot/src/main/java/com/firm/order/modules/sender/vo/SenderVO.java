package com.firm.order.modules.sender.vo;

import com.firm.order.modules.base.vo.SuperVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class SenderVO extends SuperVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String senderName;
	private String serderPhone;
	private String senderAddr;
	private int isEnabled;
	private int bizRange;
	

}
