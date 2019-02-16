package com.firm.order.modules.warehouse.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.firm.order.modules.base.service.IBaseService;
import com.firm.order.modules.warehouse.entity.WarehouseEntity;
import com.firm.order.modules.warehouse.vo.WarehouseVO;

public interface IWarehouseService extends IBaseService<WarehouseEntity,WarehouseVO>{

	Page<WarehouseVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception;

	List<WarehouseVO> save(List<WarehouseVO> list) throws Exception;

}
