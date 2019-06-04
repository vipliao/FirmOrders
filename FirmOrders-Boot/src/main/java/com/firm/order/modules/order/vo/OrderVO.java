package com.firm.order.modules.order.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.firm.order.modules.base.vo.SuperVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class OrderVO extends SuperVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String region;
	private String userId;
	private String userName;
	private String orderCode;
	private String warehouse;
	
	private String warehouseName;
	
	private String orderNature;
	private String serderPhone;
	private String senderAddr;
	private String orderWechatCode;
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date orderDate;
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date deliverDate;
	private String receiverName;
	private String receiverPhone;
	private String receiverArea;
	private String receiverCity;
	private String receiverProvince;
	private String receiverAddr;
	private String advertChannel;
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date incomlineTime;
	private BigDecimal depositAmout;
	private BigDecimal collectionAmout;
	private BigDecimal totalAmount;
	private int isForeignExpress;
	private int orderState;
	private int isOverCost;
	private BigDecimal costAmount;
	private BigDecimal costRatio;
	private String expressCode;
	private int expressState;
	private int expressCompany;
	
	List<OrderProductVO> childrenDetail = new ArrayList<>();
	
	
}
