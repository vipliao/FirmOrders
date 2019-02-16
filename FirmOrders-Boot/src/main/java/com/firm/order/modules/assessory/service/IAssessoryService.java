package com.firm.order.modules.assessory.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.firm.order.modules.assessory.entity.AssessoryEntity;
import com.firm.order.modules.assessory.vo.AssessoryVO;
import com.firm.order.modules.base.service.IBaseService;

public interface IAssessoryService extends IBaseService<AssessoryEntity, AssessoryVO>{
	
	List<AssessoryVO> queryAssessorys(List<String> businessIds) throws Exception;

	Page<AssessoryVO> queryByType(Pageable pageable,Map<String,Object> map) throws Exception;

}
