package com.firm.orders.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.orders.base.entity.SuperEntity;

@Entity
@Table(name="user_own_resource")
public class UserOwnResourceEntity extends SuperEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Column(name="user_id")
	private String userId;
	@Column(name="resource_wechat_code")
	private String resourceWechatCode;
	@Column(name="resource_phone")
	private String resourcePhone;
	@Column(name="min_fans")
	private int minFans;
	
	/*@ManyToOne
	@JoinColumn(name = "user_id", insertable=false ,updatable=false)
	private UserEntity parent;*/
	
	public String getResourceWechatCode() {
		return resourceWechatCode;
	}
	public void setResourceWechatCode(String resourceWechatCode) {
		this.resourceWechatCode = resourceWechatCode;
	}
	public String getResourcePhone() {
		return resourcePhone;
	}
	public void setResourcePhone(String resourcePhone) {
		this.resourcePhone = resourcePhone;
	}
	public int getMinFans() {
		return minFans;
	}
	public void setMinFans(int minFans) {
		this.minFans = minFans;
	}
	/*public UserEntity getParent() {
		return parent;
	}
	public void setParent(UserEntity parent) {
		this.parent = parent;
	}*/
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

}
