package com.firm.orders.role.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.firm.orders.base.service.IBaseService;
import com.firm.orders.role.entity.RoleEntity;
import com.firm.orders.role.vo.RoleVO;

public interface IRoleService extends IBaseService<RoleEntity,RoleVO> {
	Page<RoleVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception;

}
