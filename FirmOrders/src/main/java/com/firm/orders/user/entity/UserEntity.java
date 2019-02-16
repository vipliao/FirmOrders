package com.firm.orders.user.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import com.firm.orders.base.entity.SuperEntity;

@Entity
@Table(name="user_info")
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
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserCode() {
		return userCode;
	}
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public int getIsFrozen() {
		return isFrozen;
	}
	public void setIsFrozen(int isFrozen) {
		this.isFrozen = isFrozen;
	}
	/*public List<UserOwnResourceEntity> getChildrenDetail() {
		return childrenDetail;
	}
	public void setChildrenDetail(List<UserOwnResourceEntity> childrenDetail) {
		this.childrenDetail = childrenDetail;
	}*/
	
	
}
