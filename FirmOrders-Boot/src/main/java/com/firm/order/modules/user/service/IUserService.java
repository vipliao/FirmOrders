package com.firm.order.modules.user.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.firm.order.modules.base.service.IBaseService;
import com.firm.order.modules.user.entity.UserEntity;
import com.firm.order.modules.user.vo.LoginedUserVO;
import com.firm.order.modules.user.vo.UserOwnResourceVO;
import com.firm.order.modules.user.vo.UserVO;

public interface IUserService extends IBaseService<UserEntity,UserVO>{

	LoginedUserVO login(String loginName,String password) throws Exception;
	
	public UserVO anth(String loginName,String password) throws Exception;

	void deleteByPhone(String phone) throws Exception;

	UserVO queryUserByPhoneOrCode(String code) throws Exception;
	
	Page<UserVO> queryUserList(Pageable pageable, Map<String, Object> map) throws Exception;

	void frozenUser(String id,int type) throws Exception;
	
	String reSetPassWord(String phone) throws Exception;
	
	void updatePassword(String phone,String password) throws Exception;
	
	void changeUserPic(String phone,String assessoryId,int assessoryBusinessType) throws Exception; 
	
	List<UserOwnResourceVO> saveUserOwnResources(String userId,List<UserOwnResourceVO> list) throws Exception;
	
	UserVO saveUserOwnResources1(String userId,List<UserOwnResourceVO> list) throws Exception;
	
	List<UserOwnResourceVO> queryOwnResources(String userId) throws Exception;
	
	void deleteUserOR(String id) throws Exception;

	UserOwnResourceVO qureyOneUserORById(String id) throws Exception;

	Page<UserOwnResourceVO> queryUserORList(Pageable pageable, Map<String, Object> map) throws Exception;

	Object countRegionUserOwnResources(Map<String, Object> map) throws Exception;

}
