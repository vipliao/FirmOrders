package com.firm.orders.order.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.multipart.MultipartFile;

import com.firm.orders.base.utils.JsonBackData;
import com.firm.orders.order.entity.OrderEntity;
import com.firm.orders.order.service.IOrderService;
import com.firm.orders.order.vo.OrderVO;


@Controller
@RequestMapping(value="order")
public class OrderController {
	
private static Logger logger = LoggerFactory.getLogger(OrderController.class);
	
	@Autowired
	private IOrderService service;
	
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
				Page<OrderVO> reVO = service.queryList(pageable,map);
				back.setBackData(reVO);
				back.setSuccess(true);
				back.setBackMsg("查询订单列表成功！");
			
		}catch (Exception e) {
			logger.error("查询订单列表方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询订单列表失败,"+e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "findbyid")
	@ResponseBody
	public JsonBackData finbyid(@RequestParam String id) {
		JsonBackData back = new JsonBackData();
		try {
				OrderVO reVO = service.findVOById(id, OrderVO.class);
				back.setBackData(reVO);
				back.setSuccess(true);
				back.setBackMsg("查询订单信息成功！");
			
		}catch (Exception e) {
			logger.error("查询订单信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("查询订单信息失败,"+e.getMessage());
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
			back.setBackMsg("删除订单信息成功！");
			
		}catch (Exception e) {
			logger.error("删除订单信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("删除订单信息失败,"+e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "save",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData save(@RequestBody OrderVO vo) {
		JsonBackData back = new JsonBackData();
		try {

			OrderVO reVO = service.save(vo, OrderEntity.class, OrderVO.class);
			back.setBackData(reVO);
			back.setSuccess(true);
			back.setBackMsg("订单信息保存成功！");

		} catch (Exception e) {
			logger.error("订单信息保存方法：", e);
			back.setSuccess(false);
			back.setBackMsg("订单信息保存失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "importWarehouseReceipt",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData importWarehouseReceipt(@RequestParam MultipartFile file,@RequestParam(required=false,defaultValue="-1") int warehouse) {
		JsonBackData back = new JsonBackData();
		try {

			service.importWarehouseReceiptExcel(warehouse,file);
			back.setSuccess(true);
			back.setBackMsg("导入仓库回执成功！");

		} catch (Exception e) {
			logger.error("导入仓库回执方法：", e);
			back.setSuccess(false);
			back.setBackMsg("导入仓库回执失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "importOrderExpress",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData importOrderExpress(@RequestParam MultipartFile file) {
		JsonBackData back = new JsonBackData();
		try {
			service.importOrderExpressExcel(file);
			back.setSuccess(true);
			back.setBackMsg("导入快递状态信息成功！");

		} catch (Exception e) {
			logger.error("导入快递状态信息方法：", e);
			back.setSuccess(false);
			back.setBackMsg("导入快递状态信息失败," + e.getMessage());
		}
		return back;
	}
	
	
	@RequestMapping(value = "exportOrder",method = RequestMethod.POST)
	@ResponseBody
	public Object exportOrder(HttpServletRequest request, @RequestBody Map<String,Object> map) throws Exception {
		
		try {
			return service.exportOrderExcel(map);
		} catch (Exception e) {
			logger.error("导出订单信息方法：", e);
			JsonBackData back = new JsonBackData();
			back.setSuccess(false);
			back.setBackMsg("导出订单信息失败," + e.getMessage());
			return back;
		}
		
	}
	
	@RequestMapping(value = "exportTemplate",method = RequestMethod.GET)
	@ResponseBody
	public Object exportTemplate(HttpServletRequest request, @RequestParam(required=false,defaultValue="-1") int type) throws Exception {
		try {
			return  service.exportTemplateExcel(type);
		} catch (Exception e) {
			logger.error("导出模板方法：", e);
			JsonBackData back = new JsonBackData();
			back.setSuccess(false);
			back.setBackMsg("导出模板信息失败," + e.getMessage());
			return back;
		}

	}
	
	@RequestMapping(value = "exportMultiPurchase",method = RequestMethod.POST)
	@ResponseBody
	public Object exportMultiPurchase(HttpServletRequest request, @RequestBody Map<String,Object> map) throws Exception {
		try {
			return service.exportMultiPurchase(map);	
		} catch (Exception e) {
			logger.error("导出复购客户信息方法：", e);
			JsonBackData back = new JsonBackData();
			back.setSuccess(false);
			back.setBackMsg("导出复购客户信息失败," + e.getMessage());
			return back;
		}
		
		
	}
	
	@RequestMapping(value = "exportRegionOrder",method = RequestMethod.POST)
	@ResponseBody
	public Object exportRegionOrder(HttpServletRequest request, @RequestBody Map<String,Object> map) throws Exception {
		try {
			return service.exportRegionOrder(map);	
		} catch (Exception e) {
			logger.error("导出各区域订单信息方法：", e);
			JsonBackData back = new JsonBackData();
			back.setSuccess(false);
			back.setBackMsg("导出各区域订单信息失败," + e.getMessage());
			return back;
		}
		
		
	}
	
	
	@RequestMapping(value = "regionOrder",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData regionOrder(@RequestBody Map<String,Object> map) throws Exception {
		JsonBackData back = new JsonBackData();
		try {
			Object re = service.regionOrder(map);	
			back.setBackData(re);
			back.setSuccess(true);
			back.setBackMsg("查询时间段内订单数据成功！");

		} catch (Exception e) {
			logger.error("查询时间段内订单数据方法:", e);
			back.setSuccess(false);
			back.setBackMsg("查询时间段内订单数据失败," + e.getMessage());
		}
		return back;
		
	}
	
	@RequestMapping(value = "countPeriodOrder",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData countPeriodOrder(@RequestBody Map<String,Object> map) throws Exception {
		JsonBackData back = new JsonBackData();
		try {
			Object re = service.countPeriodOrder(map);	
			back.setBackData(re);
			back.setSuccess(true);
			back.setBackMsg("查询时间段内各区业绩情况成功！");

		} catch (Exception e) {
			logger.error("查询时间段内各区业绩情况方法:", e);
			back.setSuccess(false);
			back.setBackMsg("查询时间段内各区业绩情况失败," + e.getMessage());
		}
		return back;
	}
	@RequestMapping(value = "multiPurchaseOrder",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData multiPurchaseOrder(@RequestBody Map<String,Object> map) throws Exception {
		JsonBackData back = new JsonBackData();
		try {
			Object re = service.multiPurchaseOrder(map);	
			back.setBackData(re);
			back.setSuccess(true);
			back.setBackMsg("查询不同订单性质的客户信息成功！");

		} catch (Exception e) {
			logger.error("查询不同订单性质的客户信方法:", e);
			back.setSuccess(false);
			back.setBackMsg("查询不同订单性质的客户信失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "personalOrder",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData personalOrder(@RequestBody Map<String,Object> map) throws Exception {
		JsonBackData back = new JsonBackData();
		try {
			Object re = service.personalOrder(map);	
			back.setBackData(re);
			back.setSuccess(true);
			back.setBackMsg("查询时间段内个人出单情况成功！");

		} catch (Exception e) {
			logger.error("查询时间段内个人出单情况方法:", e);
			back.setSuccess(false);
			back.setBackMsg("查询时间段内个人出单情况失败," + e.getMessage());
		}
		return back;
	}
	
	@RequestMapping(value = "regionOrderNature",method = RequestMethod.POST)
	@ResponseBody
	public JsonBackData regionOrderNature(@RequestBody Map<String,Object> map) throws Exception {
		JsonBackData back = new JsonBackData();
		try {
			Object re = service.regionOrderNature(map);	
			back.setBackData(re);
			back.setSuccess(true);
			back.setBackMsg("查询时间段内各区不同性质订单汇总成功！");

		} catch (Exception e) {
			logger.error("查询时间段内各区不同性质订单汇总方法:", e);
			back.setSuccess(false);
			back.setBackMsg("查询时间段内各区不同性质订单汇总失败," + e.getMessage());
		}
		return back;
	}

}
