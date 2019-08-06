package com.firm.orders.base.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.firm.orders.base.utils.DynamicSpecifications;
import com.firm.orders.base.utils.SearchFilter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.firm.orders.base.dao.BaseDao;
import com.firm.orders.base.dao.BaseQueryDao;
import com.firm.orders.base.entity.BaseEntity;
import com.firm.orders.base.entity.SuperEntity;
import com.firm.orders.base.service.IBaseService;
import com.firm.orders.base.utils.JavaUuidGenerater;
import com.firm.orders.base.vo.BaseVO;

@Service
public class BaseServiceImpl<E extends BaseEntity, V extends BaseVO> implements IBaseService<E, V> {
	private final Logger logger = LoggerFactory.getLogger(BaseServiceImpl.class);

	@Autowired
	protected BaseQueryDao queryDao;
	@Autowired
	private BaseDao<E> baseDao;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public BaseDao<E> getBaseDao() {
		return this.baseDao;
	}

	public void setBaseDao(BaseDao<E> baseDao) {
		this.baseDao = baseDao;
	}

	protected E beforeSave(V vo, Class<E> clazzE, Class<V> clazzV) throws Exception {
		BaseEntity entity = this.handleSingleV2E(vo, clazzE);
		return (E) entity;
	}

	@Transactional
	public V save(V vo, Class<E> clazzE, Class<V> clazzV) throws Exception {
		E entity = this.beforeSave(vo, clazzE, clazzV);
		entity = this.saveEntity(entity, clazzE, clazzV);
		V returnVO = this.afterSave((E) entity, clazzE, clazzV);
		return returnVO;
	}

	@Transactional
	public V saveEntityReturnVO(E entity, Class<E> clazzE, Class<V> clazzV) throws Exception {
		this.saveEntity(entity, clazzE, clazzV);
		V returnVO = this.afterSave(entity, clazzE, clazzV);
		return returnVO;
	}

	@Transactional
	public E saveEntity(E entity, Class<E> clazzE, Class<V> clazzV) throws Exception {
		if (entity.getId() == null && entity instanceof SuperEntity) {
			((SuperEntity) entity).setCreateTime(new Timestamp(System.currentTimeMillis()));
			((SuperEntity) entity).setUpdateTime(new Timestamp(System.currentTimeMillis()));
		}

		if (entity.getId() != null && entity instanceof SuperEntity) {
			E e = findEntityById(entity.getId(), clazzE);
			((SuperEntity) entity).setCreateTime(((SuperEntity) e).getCreateTime());
			((SuperEntity) entity).setUpdateTime(new Timestamp(System.currentTimeMillis()));
		}
		return this.baseDao.save(entity);
	}

	@Transactional
	public void saveBatch(List<V> vos, Class<E> clazzE, Class<V> clazzV) throws Exception {
		for (int i = 0; i < vos.size(); ++i) {
			this.save((V) vos.get(i), clazzE, clazzV);
		}

	}

	protected V afterSave(E entity, Class<E> clazzE, Class<V> clazzV) throws Exception {
		V returnVO = this.handleSingleE2V(entity, clazzV);
		return returnVO;
	}

	protected void beforeDelete() {
	}

	@Transactional
	public void delete(V vo) throws Exception {
		this.delete(vo.getId());
	}

	@Transactional
	public void delete(String id) throws Exception {
		this.beforeDelete();
		BaseEntity entity = this.findEntityById(id);
		this.baseDao.delete(entity);
		this.afterDelete();
	}

	@Transactional
	public void deleteBatch(List<V> vos) throws Exception {
		for (int i = 0; i < vos.size(); ++i) {
			this.delete(vos.get(i));
		}

	}

	@Transactional
	public void deleteBatch(String[] ids) throws Exception {
		for (int i = 0; i < ids.length; ++i) {
			this.delete(ids[i]);
		}

	}

	protected void afterDelete() {
	}

	public Page<V> queryPage(Map<String, Object> searchParams, Pageable pageable, Class<V> clazzV, Class<E> clazzE)
			throws Exception {
		Specification spec = this.buildSpecification(searchParams, clazzE);
		Page page = this.baseDao.findAll(spec, pageable);
		return this.handleMultiPage(page, clazzV);
	}

	private Specification<E> buildSpecification(Map<String, Object> searchParams, Class<E> clazzE) {
		Map filters = SearchFilter.parse(searchParams);
		Specification spec = DynamicSpecifications.bySearchFilter(filters.values(), clazzE);
		return spec;
	}

	protected Page<V> handleMultiPage(Page<E> it, Class<V> cls) throws Exception {
		ArrayList l = new ArrayList();
		if (it == null) {
			return new PageImpl(l);
		} else {
			Iterator pageV = it.getContent().iterator();

			while (pageV.hasNext()) {
				BaseEntity entity = (BaseEntity) pageV.next();
				BaseVO vo = (BaseVO) cls.newInstance();
				BeanUtils.copyProperties(entity, vo);
				l.add(vo);
			}

			PageImpl pageV1 = new PageImpl(l);
			return pageV1;
		}
	}

	protected List<E> handleMultiV2E(Iterable<V> it, Class<E> cls)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		ArrayList l = new ArrayList();
		if (it == null) {
			return l;
		} else {
			Iterator arg3 = it.iterator();

			while (arg3.hasNext()) {
				BaseVO vo = (BaseVO) arg3.next();
				BaseEntity entity = (BaseEntity) cls.newInstance();
				BeanUtils.copyProperties(vo, entity);
				l.add(entity);
			}

			return l;
		}
	}

	protected List<V> handleMultiE2V(Iterable<E> it, Class<V> cls)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		ArrayList l = new ArrayList();
		if (it == null) {
			return l;
		} else {
			Iterator arg3 = it.iterator();

			while (arg3.hasNext()) {
				BaseEntity entity = (BaseEntity) arg3.next();
				BaseVO vo = (BaseVO) cls.newInstance();
				BeanUtils.copyProperties(entity, vo);
				l.add(vo);
			}

			return l;
		}
	}

	protected V handleSingleE2V(E e, Class<V> cls)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		if (e == null) {
			return null;
		} else {
			V vo = cls.newInstance();
			BeanUtils.copyProperties(e, vo, new String[] { null, "parent" });
			return vo;
		}
	}

	protected E handleSingleV2E(V vo, Class<E> cls)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		if (vo == null) {
			return null;
		} else {
			BaseEntity entity = (BaseEntity) cls.newInstance();
			BeanUtils.copyProperties(vo, entity);
			return (E) entity;
		}
	}

	public V findVOById(final String id, Class<V> cls) throws Exception {
		BaseEntity entity = (BaseEntity) this.baseDao.findOne(new Specification() {
			@Override
			public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("id").as(String.class), id));
			}
		});
		if (entity == null) {
			throw new Exception("查询失败，数据已被删除，或丢失！");
		} else {
			V vo = (V) cls.newInstance();
			this.handleSingleE2V((E) entity, vo);
			return vo;
		}
	}

	@Deprecated
	protected V handleSingleE2V(E entity) {
		return null;
	}

	protected V handleSingleE2V(E entity, V vo) throws Exception {
		BeanUtils.copyProperties(entity, vo, new String[] { "parent" });
		return vo;
	}

	protected E findEntityById(final String id) throws Exception {
		BaseEntity entity = (BaseEntity) this.baseDao.findOne(new Specification() {
			@Override
			public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("id").as(String.class), id));
			}

		});
		return (E) entity;
	}

	public E findEntityById(final String id, Class<E> cls) throws Exception {
		BaseEntity entity = (BaseEntity) this.baseDao.findOne(new Specification() {

			@Override
			public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("id").as(String.class), id));
			}
		});

		return (E) entity;
	}

	public List<V> findAll(Class<V> cls)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		List entity = this.baseDao.findAll();
		return this.handleMultiE2V(entity, cls);
	}

	protected static String trimString(String s) {
		return s == null ? "" : s.trim();
	}

	@Override
	public List<V> findByWhereClause(String whereClause, Class<V> clsV) throws Exception {
		return null;
	}

	@Override
	public List<V> findBySql(String sql, Class<V> clsV) throws Exception {
		return null;

	}

	/*
	 * public List<? extends SuperBillSubVO> deleteSubVOs(List<? extends
	 * SuperBillSubVO> vos, Class<? extends SuperBillSubEntity> clazz) throws
	 * Exception { if (vos.size() <= 0) { return vos; } else { ArrayList
	 * returnVO = new ArrayList(); Table table = (Table)
	 * clazz.getAnnotation(Table.class); String tableName = table.name(); String
	 * ids = "";
	 * 
	 * for (int deleteSql = 0; deleteSql < vos.size(); ++deleteSql) {
	 * SuperBillSubVO superBillSubVO = (SuperBillSubVO) vos.get(deleteSql); if
	 * (superBillSubVO.getVostate() == 3) { if (ids.equals("")) { ids = ids +
	 * "\'" + superBillSubVO.getId() + "\'"; } else { ids = ids + ",\'" +
	 * superBillSubVO.getId() + "\'"; } } else { returnVO.add(superBillSubVO); }
	 * }
	 * 
	 * if (!ids.equals("")) { String arg8 = "delete from " + tableName +
	 * " where id in (" + ids + ")"; this.queryDao.executeUpdateSQL(arg8); }
	 * 
	 * return returnVO; } }
	 */

	public <T> Map<String, Object> getFieldNames(Class<T> clazz) {
		Map<String, Object> map = new HashMap<>();
		Table table = clazz.getAnnotation(Table.class);
		if (table != null) {
			map.put("tableName", table.name().toLowerCase());
		}
		List<String> dbFieldNames = new ArrayList<>();
		List<String> fieldNames = new ArrayList<>();
		Field[] fields = FieldUtils.getAllFields(clazz);
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getName().toLowerCase().equals("serialversionuid")) {
				continue;
			}
			fieldNames.add(fields[i].getName().toLowerCase());
			Column column = fields[i].getAnnotation(Column.class);
			if (column != null) {
				dbFieldNames.add(column.name().toLowerCase());
			} else {
				dbFieldNames.add(fields[i].getName().toLowerCase());
			}
			Id id = null;
			if (fields[i].getAnnotation(Id.class) != null) {
				id = (Id) fields[i].getAnnotation(Id.class);
			}
			if (id != null) {
				if (column != null) {
					map.put("dbPrimaryKeyName", column.name().toLowerCase());
				}
				map.put("primaryKeyName", fields[i].getName().toLowerCase());
			}
			/*
			 * OneToMany oneToMany= fields[i].getAnnotation(OneToMany.class);
			 * if(fields[i].getAnnotation(OneToMany.class) != null) { String
			 * dbFiledNames =fields[i].getGenericReturnType().getTypeName();
			 * Class filedMap = arg13[fieldNames].getReturnType(); String
			 * dbFiledMap = null; if(dbFiledNames.startsWith("java") &&
			 * Collection.class.isAssignableFrom(filedMap) &&
			 * dbFiledNames.contains("<")) { dbFiledMap =
			 * dbFiledNames.substring(dbFiledNames.indexOf("<") + 1,
			 * dbFiledNames.indexOf(">")); json = new JSONObject();
			 * arg15.add(json); json.put("childentityname", dbFiledMap);
			 * json.put("childfieldname", fields[fieldNames].getName()); } }
			 * if(oneToMany != null ){ fields[i] }
			 */
		}
		map.put("fieldNames", fieldNames);
		map.put("dbFieldNames", dbFieldNames);
		return map;
	}

	/*public E save(E e) throws Exception {
		if (e != null) {
			List<E> list = new ArrayList<>();
			list.add(e);
			bathSave(list);
			return list.get(0);
		}
		return null;

	}

	@Transactional
	@SuppressWarnings("unchecked")
	public <T extends BaseEntity> List<T> bathSave(List<T> list) throws Exception {
		if (list == null || list.isEmpty()) {
			return null;
		}
		Map<String, Object> map = getFieldNames((Class<T>) list.get(0).getClass());
		List<String> fieldNames = (ArrayList<String>) map.get("fieldNames");
		List<String> dbFieldNames = (ArrayList<String>) map.get("dbFieldNames");
		String tableName = (String) map.get("tableName");
		String primaryKeyName = (String) map.get("primaryKeyName");
		StringBuffer sql = getJdbcInsertSql(dbFieldNames, tableName);
		List<Object[]> batchArgs = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			T entity = list.get(i);
			if (entity.getAttributeValue(primaryKeyName) == null
					|| entity.getAttributeValue(primaryKeyName).equals("")) {
				entity.setAttributeValue(primaryKeyName, JavaUuidGenerater.generateUuid());
			} else {

			}
			Object[] rows = new Object[dbFieldNames.size()];
			for (int j = 0; j < dbFieldNames.size(); ++j) {
				rows[j] = entity.getAttributeValue((String) fieldNames.get(j));
			}
			batchArgs.add(rows);
		}
		jdbcTemplate.batchUpdate(sql.toString(), batchArgs);
		return list;
	}
*/
/*	private StringBuffer getJdbcInsertSql(List<String> dbFiledNames, String tableName) throws Exception {
		StringBuffer sql = new StringBuffer();
		StringBuffer sql1 = new StringBuffer("(");
		sql.append("insert into   " + tableName + "(");
		for (int j = 0; j < dbFiledNames.size(); ++j) {
			sql.append((String) dbFiledNames.get(j));
			sql1.append("?");
			if (dbFiledNames.size() - 1 != j) {
				sql.append(",");
				sql1.append(",");
			}
		}

		sql.append(") VALUES");
		sql1.append(")");
		
		 * sql.append("(");
		 * 
		 * for (int j = 0; j < dbFiledNames.size(); ++j) { sql.append("?"); if
		 * (dbFiledNames.size() - 1 != j) { sql.append(","); } }
		 * 
		 * sql.append(")");
		 
		return sql.append(sql1);
	}*/

}
