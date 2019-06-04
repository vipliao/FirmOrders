package com.firm.order.modules.product.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.firm.order.modules.base.service.IBaseService;
import com.firm.order.modules.product.entity.ProductEntity;
import com.firm.order.modules.product.vo.ProductVO;

public interface IProductService extends IBaseService<ProductEntity, ProductVO>{

	Page<ProductVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception;
	
	

}
