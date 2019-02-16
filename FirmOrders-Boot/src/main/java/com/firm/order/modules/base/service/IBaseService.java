package com.firm.order.modules.base.service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.firm.order.modules.base.entity.BaseEntity;
import com.firm.order.modules.base.vo.BaseVO;

public interface IBaseService<E extends BaseEntity,V extends BaseVO> {

	V save(V v, Class<E> clsE, Class<V> clsV) throws Exception;

	void saveBatch(List<V> list, Class<E> clsE, Class<V> clsV) throws Exception;

	void delete(V v) throws Exception;

	void delete(String id) throws Exception;

	void deleteBatch(String[] array) throws Exception;

	void deleteBatch(List<V> list) throws Exception;

	V findVOById(String id, Class<V> clsV) throws Exception;

	E findEntityById(String id, Class<E> clsE) throws Exception;

	Page<V> queryPage(Map<String, Object> map, Pageable p, Class<V> clsV, Class<E> clsE) throws Exception;

	List<V> findAll(Class<V> clsV) throws IllegalAccessException, InvocationTargetException, InstantiationException;
	
	List<V> findByWhereClause(String whereClause, Class<V> clsV) throws Exception;
	
	List<V> findBySql(String sql, Class<V> clsV) throws Exception;
}
