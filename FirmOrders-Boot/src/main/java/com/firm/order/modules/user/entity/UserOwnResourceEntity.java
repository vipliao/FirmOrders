package com.firm.order.modules.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.order.modules.base.entity.SuperEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="user_own_resource")
@Getter @Setter @ToString
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
}
