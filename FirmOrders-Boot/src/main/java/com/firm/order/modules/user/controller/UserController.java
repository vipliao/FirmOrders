package com.firm.order.modules.user.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.firm.order.modules.user.entity.UserEntity;
import com.firm.order.modules.user.service.IUserService;
import com.firm.order.modules.user.vo.LoginVO;
import com.firm.order.modules.user.vo.LoginedUserVO;
import com.firm.order.modules.user.vo.UserOwnResourceVO;
import com.firm.order.modules.user.vo.UserVO;
import com.firm.order.utils.JsonBackData;

@Controller
@RequestMapping(value = "user")
public class UserController {
	private static Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private IUserService service;

	@RequestMapping(value = "save", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData save(@RequestBody UserVO vo) {
		JsonBackData back = new JsonBackData();
		try {

			UserVO userVO = service.save(vo, UserEntity.class, UserVO.class);
			back.setBackData(userVO);
			back.setSuccess(true);
			StringBuilder reMsg = new StringBuilder();
			if(vo.getId()==null || vo.getId().equals("")){
				reMsg.append("用户信息保存成功,用户编码为"+userVO.getUserCode());
				if(vo.getPassword() == null || vo.getPassword().equals("")){
					reMsg.append(",密码为000000");
				}
			}
			if(reMsg==null || reMsg.toString().equals("")){
				reMsg.append("用户信息保存成功!");
			}
			back.setBackMsg(reMsg.toString());

		} catch (Exception e) {
			logger.error("用户信息保存方法：", e);
			back.setSuccess(false);
			back.setBackMsg("用户信息保存失败," + e.getMessage());
		}
		return back;
	}

	@RequestMapping(value = "qureyOneUser")
	@ResponseBody
	public JsonBackData qureyOneUser(String id) {
		JsonBackData back = new JsonBackData();
		try {
			UserVO userVO = service.findVOById(id, UserVO.class);
			back.setBackData(userVO);
			back.setSuccess(true);
			back.setBackMsg("查询用户信息成功！");

		} catch (Exception e) {
			logger.error("查询用户信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询用户信息失败," + e.getMessage());
		}
		return back;
	}

	@RequestMapping(value = "userList")
	@ResponseBody
	public JsonBackData userList(@RequestParam Map<String, Object> map) {
		JsonBackData back = new JsonBackData();
		try {
			Pageable pageable = null;
			String pageNumber = (String) map.get("pageNumber");
			String pageSize = (String) map.get("pageSize");
			String sortField = (String) map.get("sortField");
			String sortType = (String) map.get("sortType");
			if (!StringUtils.isEmpty(pageNumber) && !StringUtils.isEmpty(pageSize)) {
				int iPageNumber = Integer.parseInt(pageNumber);
				if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortType)) {
					pageable = new PageRequest(iPageNumber <= 0 ? 0 : (iPageNumber - 1), Integer.parseInt(pageSize),
							Direction.fromString(sortType), sortField);
				} else {
					pageable = new PageRequest(iPageNumber <= 0 ? 0 : (iPageNumber - 1), Integer.parseInt(pageSize));
				}
			}
			Page<UserVO> userVO = service.queryUserList(pageable, map);
			back.setBackData(userVO);
			back.setSuccess(true);
			back.setBackMsg("查询用户信息列表成功！");

		} catch (Exception e) {
			logger.error("查询用户信息列表方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询用户信息列表失败," + e.getMessage());
		}
		return back;
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData delete(@RequestBody Map<String, Object> param) {
		JsonBackData back = new JsonBackData();
		try {
			if (param.containsKey("phone")) {
				String phone = (String) param.get("phone");
				service.deleteByPhone(phone);
				back.setSuccess(true);
				back.setBackMsg("删除用户信息成功！");
			} else if (param.containsKey("id") && !param.containsKey("phone")) {
				String id = (String) param.get("id");
				service.delete(id);
				back.setSuccess(true);
				back.setBackMsg("删除用户信息成功！");
			} else {
				back.setSuccess(false);
				back.setBackMsg("删除用户信息失败,缺少参数");
			}

		} catch (Exception e) {
			logger.error("删除用户信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("删除用户信息失败," + e.getMessage());
		}
		return back;
	}

	@RequestMapping(value = "queryUser")
	@ResponseBody
	public JsonBackData queryUser(@RequestParam String phone) {
		JsonBackData back = new JsonBackData();
		try {
			UserVO userVO = service.queryUserByPhoneOrCode(phone);
			back.setBackData(userVO);
			back.setSuccess(true);
			back.setBackMsg("根据用户名查询用户信息成功！");

		} catch (Exception e) {
			logger.error("根据用户名查询用户信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("根据用户名查询用户信息失败," + e.getMessage());
		}
		return back;
	}

	@RequestMapping(value = "login", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData login(@RequestBody LoginVO loginVO) {
		JsonBackData back = new JsonBackData();
		try {
			LoginedUserVO loginUserVO = service.login(loginVO.getLoginName(), loginVO.getPassword());
			back.setBackData(loginUserVO);
			back.setSuccess(true);
			back.setBackMsg("登录成功！");

		} catch (Exception e) {
			logger.error("用户登录方法：", e);
			back.setSuccess(false);
			back.setBackMsg("登录失败," + e.getCause().getMessage());
		}
		return back;
	}

	@RequestMapping(value = "frozenUser", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData frozenUser(@RequestBody Map<String, Object> map) {
		JsonBackData back = new JsonBackData();
		try {
			if (!map.containsKey("id")) {
				throw new Exception("用户id不能为空");
			}
			int isFrozen = 0;
			if (map.containsKey("isFrozen")) {
				isFrozen = (int) map.get("isFrozen");
			}

			service.frozenUser((String) map.get("id"), isFrozen);
			back.setSuccess(true);
			back.setBackMsg("冻结/解冻用户成功！");

		} catch (Exception e) {
			logger.error("冻结/解冻用户方法：", e);
			back.setSuccess(false);
			back.setBackMsg("冻结/解冻用户失败," + e.getMessage());
		}
		return back;
	}

	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData resetPassword(@RequestBody UserVO user) {
		JsonBackData back = new JsonBackData();
		try {

			String data = service.reSetPassWord(user.getPhone());
			back.setSuccess(true);
			back.setBackMsg("重置密码成功，默认密码为:" + data);

		} catch (Exception e) {
			logger.error("重置密码方法：", e);
			back.setSuccess(false);
			back.setBackMsg("重置密码失败," + e.getMessage());
		}
		return back;
	}

	@RequestMapping(value = "updatePassword", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData updatePassword(@RequestBody UserVO user) {
		JsonBackData back = new JsonBackData();
		try {
			service.updatePassword(user.getPhone(), user.getPassword());
			back.setSuccess(true);
			back.setBackMsg("修改密码成功!");

		} catch (Exception e) {
			logger.error("修改密码方法：", e);
			back.setSuccess(false);
			back.setBackMsg("修改密码失败," + e.getMessage());
		}
		return back;
	}

	@RequestMapping(value = "changeUserPic", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData changeUserPic(@RequestBody Map<String, Object> map) {
		JsonBackData back = new JsonBackData();
		try {
			if (!map.containsKey("phone")) {
				throw new Exception("手机号不能为空");
			}
			if (!map.containsKey("assessoryId")) {
				throw new Exception("附件id不能为空");
			}
			int assessoryBusinessType = 0;
			String phone = (String) map.get("phone");
			String assessoryId = (String) map.get("assessoryId");
			service.changeUserPic(phone, assessoryId, assessoryBusinessType);
			back.setSuccess(true);
			back.setBackMsg("修改头像成功!");

		} catch (Exception e) {
			logger.error("修改头像方法：", e);
			back.setSuccess(false);
			back.setBackMsg("修改头像失败," + e.getMessage());
		}
		return back;
	}

	@RequestMapping(value = "saveUserOwnResources", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData saveUserOwnResources(@RequestBody List<UserOwnResourceVO> list) {
		JsonBackData back = new JsonBackData();
		try {
			UserVO revo = service.saveUserOwnResources1(list.get(0).getUserId(), list);
			back.setBackData(revo);
			back.setSuccess(true);
			back.setBackMsg("保存用户拥有资源成功!");

		} catch (Exception e) {
			logger.error("保存用户拥有资源方法：", e);
			back.setSuccess(false);
			back.setBackMsg("保存用户拥有资源失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "saveUserOwnResourcesOnly", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData saveUserOwnResourcesOnly(@RequestBody List<UserOwnResourceVO> list) {
		JsonBackData back = new JsonBackData();
		try {

			List<UserOwnResourceVO> revo = service.saveUserOwnResources(list.get(0).getUserId(), list);
			back.setBackData(revo);
			back.setSuccess(true);
			back.setBackMsg("保存用户拥有资源成功!");

		} catch (Exception e) {
			logger.error("保存用户拥有资源(only)方法：", e);
			back.setSuccess(false);
			back.setBackMsg("保存用户拥有资源失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "saveOneUserOwnResources", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData saveOneUserOwnResources(@RequestBody UserOwnResourceVO vo) {
		JsonBackData back = new JsonBackData();
		try {

			List<UserOwnResourceVO> list =new ArrayList<>();
			list.add(vo);
			List<UserOwnResourceVO> revo = service.saveUserOwnResources(vo.getUserId(), list);
			back.setBackData(revo.get(0));
			back.setSuccess(true);
			back.setBackMsg("保存用户拥有资源成功!");

		} catch (Exception e) {
			logger.error("保存一条用户拥有资源方法：", e);
			back.setSuccess(false);
			back.setBackMsg("保存用户拥有资源失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "qureyUserORByUserId", method = RequestMethod.GET)
	@ResponseBody
	public JsonBackData qureyUserORByUserId(@RequestParam String userId) {
		JsonBackData back = new JsonBackData();
		try {

			List<UserOwnResourceVO> revo = service.queryOwnResources(userId);
			back.setBackData(revo);
			back.setSuccess(true);
			back.setBackMsg("查询用户拥有资源成功!");

		} catch (Exception e) {
			logger.error("查询用户拥有资源方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询用户拥有资源失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "qureyOneUserORById", method = RequestMethod.GET)
	@ResponseBody
	public JsonBackData qureyOneUserORById(@RequestParam String id) {
		JsonBackData back = new JsonBackData();
		try {

			UserOwnResourceVO revo = service.qureyOneUserORById(id);
			back.setBackData(revo);
			back.setSuccess(true);
			back.setBackMsg("查询用户拥有资源成功!");

		} catch (Exception e) {
			logger.error("查询一个用户拥有资源方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询用户拥有资源失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "deleteUserOR", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData deleteUserOR(@RequestBody Map<String, Object> param) {
		JsonBackData back = new JsonBackData();
		try {
			 if (param.containsKey("id")) {
				String id = (String) param.get("id");
				service.deleteUserOR(id);
				back.setSuccess(true);
				back.setBackMsg("删除用户拥有资源信息成功！");
			} else {
				back.setSuccess(false);
				back.setBackMsg("删除用户拥有资源信息失败,缺少参数");
			}

		} catch (Exception e) {
			logger.error("删除用户拥有资源信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("删除用户拥有资源信息失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "userORList")
	@ResponseBody
	public JsonBackData userORList(@RequestParam Map<String, Object> map) {
		JsonBackData back = new JsonBackData();
		try {
			Pageable pageable = null;
			String pageNumber = (String) map.get("pageNumber");
			String pageSize = (String) map.get("pageSize");
			String sortField = (String) map.get("sortField");
			String sortType = (String) map.get("sortType");
			if (!StringUtils.isEmpty(pageNumber) && !StringUtils.isEmpty(pageSize)) {
				int iPageNumber = Integer.parseInt(pageNumber);
				if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortType)) {
					pageable = new PageRequest(iPageNumber <= 0 ? 0 : (iPageNumber - 1), Integer.parseInt(pageSize),
							Direction.fromString(sortType), sortField);
				} else {
					pageable = new PageRequest(iPageNumber <= 0 ? 0 : (iPageNumber - 1), Integer.parseInt(pageSize));
				}
			}
			Page<UserOwnResourceVO> reVO = service.queryUserORList(pageable, map);
			back.setBackData(reVO);
			back.setSuccess(true);
			back.setBackMsg("查询用户拥有资源信息列表成功！");

		} catch (Exception e) {
			logger.error("查询用户拥有资源信息列表方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询用户拥有资源信息列表失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "countRegionOResources")
	@ResponseBody
	public JsonBackData countRegionUserOwnResources(@RequestBody(required=false) Map<String,Object> map) {
		JsonBackData back = new JsonBackData();
		try {
			Object re = service.countRegionUserOwnResources(map);	
			back.setBackData(re);
			back.setSuccess(true);
			back.setBackMsg("各区资源统计成功！");

		} catch (Exception e) {
			logger.error("各区资源统计方法:", e);
			back.setSuccess(false);
			back.setBackMsg("各区资源统计失败," + e.getMessage());
		}
		return back;
	}

}
