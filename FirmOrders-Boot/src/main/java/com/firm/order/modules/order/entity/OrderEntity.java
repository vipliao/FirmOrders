package com.firm.order.modules.order.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.firm.order.modules.base.entity.SuperEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="order_info")
@Getter @Setter @ToString
public class OrderEntity extends SuperEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String region;
	
	@Column(name="user_id")
	private String userId;
	
	@Column(name="user_name")
	private String userName;
	
	@Column(name="order_code")
	private String orderCode;
	
	private int warehouse;
	
	@Column(name="order_nature")
	private String orderNature;
	
	@Column(name="serder_phone")
	private String serderPhone;
	
	@Column(name="sender_addr")
	private String senderAddr;
	
	@Column(name="order_wechat_code")
	private String orderWechatCode;
	
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@Column(name="order_date")
	private Date orderDate;
	
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@Column(name="deliver_date")
	private Date deliverDate;
	
	@Column(name="receiver_name")
	private String receiverName;
	
	@Column(name="receiver_phone")
	private String receiverPhone;
	
	@Column(name="receiver_addr")
	private String receiverAddr;
	
	@Column(name="advert_channel")
	private String advertChannel;
	
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@Column(name="incomline_time")
	private Date incomlineTime;
	
	@Column(name="deposit_amout")
	private BigDecimal depositAmout;
	
	@Column(name="collection_amout")
	private BigDecimal collectionAmout;
	
	@Column(name="total_amount")
	private BigDecimal totalAmount;
	
	@Column(name="is_foreign_express")
	private int isForeignExpress;
	
	@Column(name="order_state")
	private int orderState;
	
	@Column(name="is_over_cost")
	private int isOverCost;
	
	@Column(name="cost_amount")
	private BigDecimal costAmount;

	@Column(name="cost_ratio")
	private BigDecimal costRatio;
	
	@Column(name="express_code")
	private String expressCode; 
	
	@Column(name="express_state")
	private int expressState;
	
	@Column(name="express_company")
	private int expressCompany;

}
