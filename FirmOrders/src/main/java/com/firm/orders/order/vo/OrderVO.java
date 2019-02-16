package com.firm.orders.order.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.firm.orders.base.vo.SuperVO;

public class OrderVO extends SuperVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String region;
	private String userId;
	private String userName;
	private String orderCode;
	private int warehouse;
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

	public int getWarehouse() {
		return warehouse;
	}

	public void setWarehouse(int warehouse) {
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

	public void setIsOverCost(int is_over_cost) {
		this.isOverCost = is_over_cost;
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

	public List<OrderProductVO> getChildrenDetail() {
		return childrenDetail;
	}

	public void setChildrenDetail(List<OrderProductVO> childrenDetail) {
		this.childrenDetail = childrenDetail;
	}

	public int getExpressCompany() {
		return expressCompany;
	}

	public void setExpressCompany(int expressCompany) {
		this.expressCompany = expressCompany;
	}


}
