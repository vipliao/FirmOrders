package com.firm.order.modules.user.vo;

import java.util.ArrayList;
import java.util.List;

import com.firm.order.modules.assessory.vo.AssessoryVO;
import com.firm.order.modules.base.vo.SuperVO;

import lombok.Data;


@Data 
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
	private int roleLevel;
	private int roleBizRange;
	private int isFrozen;
	private List<UserOwnResourceVO> childrenDetail = new ArrayList<UserOwnResourceVO>(0);
	private List<AssessoryVO> assessorys;
	
}
