package com.firm.orders.order.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.firm.orders.base.entity.SuperEntity;

@Entity
@Table(name="order_info")
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
	
	private String warehouse;
	
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

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getWarehouse() {
		return warehouse;
	}

	public void setWarehouse(String warehouse) {
		this.warehouse = warehouse;
	}

	public String getOrderNature() {
		return orderNature;
	}

	public void setOrderNature(String orderNature) {
		this.orderNature = orderNature;
	}

	public String getSerderPhone() {
		return serderPhone;
	}

	public void setSerderPhone(String serderPhone) {
		this.serderPhone = serderPhone;
	}

	public String getSenderAddr() {
		return senderAddr;
	}

	public void setSenderAddr(String senderAddr) {
		this.senderAddr = senderAddr;
	}

	public String getOrderWechatCode() {
		return orderWechatCode;
	}

	public void setOrderWechatCode(String orderWechatCode) {
		this.orderWechatCode = orderWechatCode;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public Date getDeliverDate() {
		return deliverDate;
	}

	public void setDeliverDate(Date deliverDate) {
		this.deliverDate = deliverDate;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getReceiverPhone() {
		return receiverPhone;
	}

	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}

	public String getReceiverAddr() {
		return receiverAddr;
	}

	public void setReceiverAddr(String receiverAddr) {
		this.receiverAddr = receiverAddr;
	}

	public String getAdvertChannel() {
		return advertChannel;
	}

	public void setAdvertChannel(String advertChannel) {
		this.advertChannel = advertChannel;
	}

	public Date getIncomlineTime() {
		return incomlineTime;
	}

	public void setIncomlineTime(Date incomlineTime) {
		this.incomlineTime = incomlineTime;
	}

	public BigDecimal getDepositAmout() {
		return depositAmout;
	}

	public void setDepositAmout(BigDecimal depositAmout) {
		this.depositAmout = depositAmout;
	}

	public BigDecimal getCollectionAmout() {
		return collectionAmout;
	}

	public void setCollectionAmout(BigDecimal collectionAmout) {
		this.collectionAmout = collectionAmout;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public int getIsForeignExpress() {
		return isForeignExpress;
	}

	public void setIsForeignExpress(int isForeignExpress) {
		this.isForeignExpress = isForeignExpress;
	}

	public int getOrderState() {
		return orderState;
	}

	public void setOrderState(int orderState) {
		this.orderState = orderState;
	}

	public int getIsOverCost() {
		return isOverCost;
	}

	public void setIsOverCost(int isOverCost) {
		this.isOverCost = isOverCost;
	}

	public BigDecimal getCostAmount() {
		return costAmount;
	}

	public void setCostAmount(BigDecimal costAmount) {
		this.costAmount = costAmount;
	}

	public BigDecimal getCostRatio() {
		return costRatio;
	}

	public void setCostRatio(BigDecimal costRatio) {
		this.costRatio = costRatio;
	}

	public String getExpressCode() {
		return expressCode;
	}

	public void setExpressCode(String expressCode) {
		this.expressCode = expressCode;
	}

	public int getExpressState() {
		return expressState;
	}

	public void setExpressState(int expressState) {
		this.expressState = expressState;
	}

	public int getExpressCompany() {
		return expressCompany;
	}

	public void setExpressCompany(int expressCompany) {
		this.expressCompany = expressCompany;
	}

}
