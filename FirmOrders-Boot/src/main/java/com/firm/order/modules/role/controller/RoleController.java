package com.firm.order.modules.role.controller;

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

import com.firm.order.modules.role.entity.RoleEntity;
import com.firm.order.modules.role.service.IRoleService;
import com.firm.order.modules.role.vo.RoleVO;
import com.firm.order.utils.JsonBackData;
@Controller
@RequestMapping(value="role")
public class RoleController {

	private static Logger logger = LoggerFactory.getLogger(RoleController.class);
	
	@Autowired
	private IRoleService service;

	@RequestMapping(value = "queryList")
	@ResponseBody
	public JsonBackData queryList(@RequestParam Map<String, Object> map) {
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
				Page<RoleVO> reVO = service.queryList(pageable,map);
				back.setBackData(reVO);
				back.setSuccess(true);
				back.setBackMsg("查询角色列表成功！");
			
		}catch (Exception e) {
			logger.error("查询角色信息列表方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询角色信息列表失败,"+e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "findbyid")
	@ResponseBody
	public JsonBackData findbyid(@RequestParam String id) {
		JsonBackData back = new JsonBackData();
		try {
				RoleVO reVO = service.findVOById(id, RoleVO.class);
				back.setBackData(reVO);
				back.setSuccess(true);
				back.setBackMsg("查询角色信息成功！");
			
		}catch (Exception e) {
			logger.error("查询角色信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询角色信息失败,"+e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "save", method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData save(@RequestBody RoleVO vo) {
		JsonBackData back = new JsonBackData();
		try {

			RoleVO reVO= service.save(vo, RoleEntity.class, RoleVO.class);
			back.setBackData(reVO);
			back.setSuccess(true);
			back.setBackMsg("用户角色信息保存成功!");

		} catch (Exception e) {
			logger.error("用户角色信息保存方法：", e);
			back.setSuccess(false);
			back.setBackMsg("用户角色信息保存失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "delete",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData delete(@RequestBody Map<String,Object> param) {
		JsonBackData back = new JsonBackData();
		try {	
			if(!param.containsKey("id") ){
				throw new Exception("id不能为空!");
			}
			
			String id = (String) param.get("id");
			service.delete(id);
			back.setSuccess(true);
			back.setBackMsg("删除角色信息成功！");
			
		}catch (Exception e) {
			logger.error("删除角色信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("删除角色信息失败,"+e.getMessage());
		}
		return back;
	}
	
}
