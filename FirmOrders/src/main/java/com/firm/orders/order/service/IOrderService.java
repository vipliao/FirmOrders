package com.firm.orders.order.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.firm.orders.base.service.IBaseService;
import com.firm.orders.order.entity.OrderEntity;
import com.firm.orders.order.vo.OrderVO;

public interface IOrderService extends IBaseService<OrderEntity, OrderVO> {

	Page<OrderVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception;

	Page<OrderVO> queryWaitEnsureList(Pageable pageable, Map<String, Object> map) throws Exception;

	void importWarehouseReceiptExcel(int warehouse, MultipartFile file) throws Exception;

	void importOrderExpressExcel(MultipartFile file) throws Exception;

	ResponseEntity<byte[]> exportOrderExcel(Map<String, Object> map) throws Exception;

	ResponseEntity<byte[]> exportTemplateExcel(int type) throws Exception;

	ResponseEntity<byte[]> exportMultiPurchase(Map<String, Object> map) throws Exception;

	ResponseEntity<byte[]> exportRegionOrder(Map<String, Object> map) throws Exception;
	
	List<Map<String, Object>>  regionOrder(Map<String, Object> map) throws Exception;

	Object countPeriodOrder(Map<String, Object> map) throws Exception;

	Object multiPurchaseOrder(Map<String, Object> map) throws Exception;

	Object personalOrder(Map<String, Object> map) throws Exception;

	Object regionOrderNature(Map<String, Object> map) throws Exception;

	Object updateOrders(Map<String,Object> map) throws Exception;

}
