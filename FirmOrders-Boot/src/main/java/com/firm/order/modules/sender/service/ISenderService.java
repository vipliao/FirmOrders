package com.firm.order.modules.sender.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.firm.order.modules.base.service.IBaseService;
import com.firm.order.modules.sender.entity.SenderEntity;
import com.firm.order.modules.sender.vo.SenderVO;

public interface ISenderService extends IBaseService<SenderEntity, SenderVO>{
	Page<SenderVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception;

	void enabled(String id, int enabled) throws Exception; 
}
