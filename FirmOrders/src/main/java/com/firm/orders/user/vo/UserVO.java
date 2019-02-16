package com.firm.orders.user.vo;

import java.util.ArrayList;
import java.util.List;

import com.firm.orders.assessory.vo.AssessoryVO;
import com.firm.orders.base.vo.SuperVO;
public class UserVO extends SuperVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private String userName;
	private String userCode;
	private String phone;
	private String password;
	private String region;
	private String roleId;
	private String roleName;
	private String roleCode;
	private int isFrozen;
	private List<UserOwnResourceVO> childrenDetail = new ArrayList<UserOwnResourceVO>(0);
	
	
	private List<AssessoryVO> assessorys;
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
	public List<AssessoryVO> getAssessorys() {
		return assessorys;
	}
	public void setAssessorys(List<AssessoryVO> assessorys) {
		this.assessorys = assessorys;
	}
	public List<UserOwnResourceVO> getChildrenDetail() {
		return childrenDetail;
	}
	public void setChildrenDetail(List<UserOwnResourceVO> childrenDetail) {
		this.childrenDetail = childrenDetail;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getRoleCode() {
		return roleCode;
	}
	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}
	
	
	
}
