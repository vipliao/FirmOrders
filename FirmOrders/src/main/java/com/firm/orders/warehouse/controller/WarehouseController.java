package com.firm.orders.warehouse.controller;

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

import com.firm.orders.base.utils.JsonBackData;
import com.firm.orders.warehouse.entity.WarehouseEntity;
import com.firm.orders.warehouse.service.IWarehouseService;
import com.firm.orders.warehouse.vo.WarehouseVO;

@Controller
@RequestMapping(value = "warehouse")
public class WarehouseController {
	private static Logger logger = LoggerFactory.getLogger(WarehouseController.class);
	@Autowired
	private IWarehouseService service;
	
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
								Direction.fromStringOrNull(sortType), sortField);
					} else {
						pageable = new PageRequest(iPageNumber <= 0 ? 0 : (iPageNumber - 1), Integer.parseInt(pageSize));
					}
				}
				Page<WarehouseVO> reVO = service.queryList(pageable,map);
				back.setBackData(reVO);
				back.setSuccess(true);
				back.setBackMsg("查询仓库列表成功！");
			
		}catch (Exception e) {
			logger.error("查询仓库列表方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询仓库失败,"+e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "findbyid")
	@ResponseBody
	public JsonBackData finbyid(@RequestParam String id) {
		JsonBackData back = new JsonBackData();
		try {
			WarehouseVO reVO = service.findVOById(id, WarehouseVO.class);
				back.setBackData(reVO);
				back.setSuccess(true);
				back.setBackMsg("查询仓库信息成功！");
			
		}catch (Exception e) {
			logger.error("查询仓库信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询仓库信息失败,"+e.getMessage());
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
			back.setBackMsg("删除仓库信息成功！");
			
		}catch (Exception e) {
			logger.error("删除仓库信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("删除仓库信息失败,"+e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "save",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData save(@RequestBody WarehouseVO vo) {
		JsonBackData back = new JsonBackData();
		try {

			WarehouseVO reVO = service.save(vo, WarehouseEntity.class, WarehouseVO.class);
			back.setBackData(reVO);
			back.setSuccess(true);
			back.setBackMsg("产品信息保存成功！");

		} catch (Exception e) {
			logger.error("产品信息保存方法：", e);
			back.setSuccess(false);
			back.setBackMsg("产品信息保存失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "bathSave",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData save(@RequestBody List<WarehouseVO> list) {
		JsonBackData back = new JsonBackData();
		try {

			List<WarehouseVO> reVO = service.save(list);
			back.setBackData(reVO);
			back.setSuccess(true);
			back.setBackMsg("仓库信息保存成功！");

		} catch (Exception e) {
			logger.error("仓库信息保存方法：", e);
			back.setSuccess(false);
			back.setBackMsg("仓库信息保存失败," + e.getMessage());
		}
		return back;
	}


}
