package com.firm.order.modules.sender.controller;

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

import com.firm.order.modules.sender.entity.SenderEntity;
import com.firm.order.modules.sender.service.ISenderService;
import com.firm.order.modules.sender.vo.SenderVO;
import com.firm.order.utils.JsonBackData;

@Controller
@RequestMapping(value="sender")
public class SenderController {

	private static Logger logger = LoggerFactory.getLogger(SenderController.class);
	
	@Autowired
	private ISenderService service;
	
	@RequestMapping(value = "save",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData save(@RequestBody SenderVO vo) {
		JsonBackData back = new JsonBackData();
		try {

			SenderVO reVO = service.save(vo, SenderEntity.class, SenderVO.class);
			back.setBackData(reVO);
			back.setSuccess(true);
			back.setBackMsg("用户信息保存成功！");

		} catch (Exception e) {
			logger.error("用户信息保存方法：", e);
			back.setSuccess(false);
			back.setBackMsg("用户信息保存失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "findbyid")
	@ResponseBody
	public JsonBackData findById(String id) {
		JsonBackData back = new JsonBackData();
		try {
				SenderVO reVO = service.findVOById(id, SenderVO.class);
				back.setBackData(reVO);
				back.setSuccess(true);
				back.setBackMsg("查询寄件信息成功！");
			
		}catch (Exception e) {
			logger.error("查询寄件信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询寄件信息失败,"+e.getMessage());
		}
		return back;
	}
	
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
				Page<SenderVO> userVO = service.queryList(pageable,map);
				back.setBackData(userVO);
				back.setSuccess(true);
				back.setBackMsg("查询寄件信息列表成功！");
			
		}catch (Exception e) {
			logger.error("查询寄件信息列表方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询寄件信息列表失败,"+e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "delete",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData delete(@RequestBody Map<String,Object> param) {
		JsonBackData back = new JsonBackData();
		try {	
				if(!param.containsKey("id")){
					back.setSuccess(false);
					back.setBackMsg("删除寄件信息失败,id不能为空!");
					return back;
				}
				String id = (String) param.get("id");
				service.delete(id);
				back.setSuccess(true);
				back.setBackMsg("删除寄件信息成功！");					
		}catch (Exception e) {
			logger.error("删除寄件信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("删除寄件信息失败,"+e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "enabled",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData enabled(@RequestBody Map<String,Object> param) {
		JsonBackData back = new JsonBackData();
		try {	
			
				if(!param.containsKey("id")){
					back.setSuccess(false);
					back.setBackMsg("启用/停用寄件信息失败,id不能为空!");
					return back;
				}
				if(!param.containsKey("enabled")){
					back.setSuccess(false);
					back.setBackMsg("启用/停用删除寄件信息失败,启用状态不能为空!");
					return back;
				}
				String id = (String) param.get("id");
				Object enabledObj = param.get("enabled");
				int enabled=0;
				if(enabledObj instanceof Integer){
					enabled = ((Integer) enabledObj).intValue();
				}else if(enabledObj instanceof String ){
					enabled = Integer.parseInt((String) param.get("enabled"));
				}
				service.enabled(id,enabled);
				back.setSuccess(true);
				back.setBackMsg("启用/停用寄件信息成功！");					
		}catch (Exception e) {
			logger.error("启用/停用寄件信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("启用/停用寄件信息失败,"+e.getMessage());
		}
		return back;
	}

}
