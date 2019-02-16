package com.firm.orders.product.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.firm.orders.base.service.IBaseService;
import com.firm.orders.product.entity.ProductEntity;
import com.firm.orders.product.vo.ProductVO;

public interface IProductService extends IBaseService<ProductEntity, ProductVO>{

	Page<ProductVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception;
	
	

}
