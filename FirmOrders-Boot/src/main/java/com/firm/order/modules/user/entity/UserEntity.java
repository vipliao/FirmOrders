package com.firm.order.modules.user.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.firm.order.modules.base.entity.SuperEntity;

import lombok.Builder;
import lombok.Data;

@Entity
@Table(name="user_info")
@Data
public class UserEntity extends SuperEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name="user_name")
	private String userName;
	@Column(name="user_code")
	private String userCode;
	private String phone;
	private String password;
	private String region;
	@Column(name="role_id")
	private String roleId;
	@Column(name="is_frozen")
	private int isFrozen;
	
/*	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "parent")
	private List<UserOwnResourceEntity> childrenDetail = new ArrayList<UserOwnResourceEntity>(0);*/
}
