package com.firm.orders.sender.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.firm.orders.base.service.IBaseService;
import com.firm.orders.sender.entity.SenderEntity;
import com.firm.orders.sender.vo.SenderVO;

public interface ISenderService extends IBaseService<SenderEntity, SenderVO>{
	Page<SenderVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception;

	void enabled(String id, int enabled) throws Exception; 
}
