package com.firm.order.modules.role.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.firm.order.modules.base.service.IBaseService;
import com.firm.order.modules.role.entity.RoleEntity;
import com.firm.order.modules.role.vo.RoleVO;

public interface IRoleService extends IBaseService<RoleEntity,RoleVO> {
	Page<RoleVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception;

}
