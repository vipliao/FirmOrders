package com.firm.orders.base.dao;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.SQLQuery;
import org.hibernate.transform.AliasToBeanConstructorResultTransformer;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.hibernate.transform.Transformers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class BaseQueryDao {
	@PersistenceContext
	   private EntityManager em;

	   public BaseQueryDao() {
	   }

	   public BaseQueryDao(EntityManager entityManager) {
	   }

	   private void setParameters(Query query, Map<String, Object> paras) {
	      if(paras != null && !paras.isEmpty()) {
	         Set set = paras.keySet();
	         Iterator arg3 = set.iterator();

	         while(arg3.hasNext()) {
	            String key = (String)arg3.next();
	            query.setParameter(key, paras.get(key));
	         }

	      }
	   }

	   private void setParameters(Query query, List<Object> paras) {
	      if(paras != null && !paras.isEmpty()) {
	         for(int i = 0; i < paras.size(); ++i) {
	            query.setParameter(i, paras.get(i));
	         }

	      }
	   }

	   public Object findOneBySql(String sql, List<Object> paras) {
	      Query query = this.em.createNativeQuery(sql);
	      this.setParameters(query, paras);
	      return query.getSingleResult();
	   }

	   public <T> Object findOneBySql(String sql, List<Object> paras, Class<T> clazz) {
	      Query query = this.em.createNativeQuery(sql);
	      this.setParameters(query, paras);
	      ((SQLQuery)query.unwrap(SQLQuery.class)).setResultTransformer(Transformers.aliasToBean(clazz));
	      return query.getSingleResult();
	   }

	   public List<Object> findListBySql(String sql) {
	      Query rquery = this.em.createNativeQuery(sql);
	      return rquery.getResultList();
	   }

	   public List<Object> findListBySql(String sql, Map<String, Object> paras, int offset, int pageSize) {
	      Query rquery = this.em.createNativeQuery(sql);
	      this.setParameters(rquery, paras);
	      rquery.setFirstResult(offset);
	      rquery.setMaxResults(pageSize);
	      return rquery.getResultList();
	   }

	   public List<Object> findListBySql(String sql, List<Object> paras, int offset, int pageSize) {
	      Query rquery = this.em.createNativeQuery(sql);
	      this.setParameters(rquery, paras);
	      rquery.setFirstResult(offset);
	      rquery.setMaxResults(pageSize);
	      return rquery.getResultList();
	   }

	   public List<Object> findListBySql(String sql, Map<String, Object> paras) {
	      Query rquery = this.em.createNativeQuery(sql);
	      this.setParameters(rquery, paras);
	      return rquery.getResultList();
	   }

	   public Page<Object> findListBySql(String sql, Map<String, Object> paras, Pageable pageable) {
	      if(pageable == null) {
	         return new PageImpl(this.findListBySql(sql, paras));
	      } else {
	         String csql = "select count(1) from (" + sql + ") t";
	         return this.findListBySql(sql, csql, paras, pageable);
	      }
	   }

	   public Page<Object> findListBySql(String qsql, String csql, Map<String, Object> paras, Pageable pageable) {
	      if(pageable == null) {
	         return new PageImpl(this.findListBySql(qsql, paras));
	      } else {
	         Query rquery = this.em.createNativeQuery(qsql);
	         Query cquery = this.em.createNativeQuery(csql);
	         this.setParameters(rquery, paras);
	         this.setParameters(cquery, paras);
	         rquery.setFirstResult(pageable.getOffset());
	         rquery.setMaxResults(pageable.getPageSize());
	         List list = rquery.getResultList();
	         long total = ((BigInteger)cquery.getSingleResult()).longValue();
	         return new PageImpl(list, pageable, total);
	      }
	   }

	   public List<Object> findListBySql(String sql, List<Object> paras) {
	      Query rquery = this.em.createNativeQuery(sql);
	      this.setParameters(rquery, paras);
	      return rquery.getResultList();
	   }

	   public Page<Object> findListBySql(String sql, List<Object> paras, Pageable pageable) {
	      if(pageable == null) {
	         return new PageImpl(this.findListBySql(sql, paras));
	      } else {
	         String csql = "select count(1) from (" + sql + ") t";
	         return this.findListBySql(sql, csql, paras, pageable);
	      }
	   }

	   public Page<Object> findListBySql(String qsql, String csql, List<Object> paras, Pageable pageable) {
	      if(pageable == null) {
	         return new PageImpl(this.findListBySql(qsql, paras));
	      } else {
	         Query rquery = this.em.createNativeQuery(qsql);
	         Query cquery = this.em.createNativeQuery(csql);
	         this.setParameters(rquery, paras);
	         this.setParameters(cquery, paras);
	         rquery.setFirstResult(pageable.getOffset());
	         rquery.setMaxResults(pageable.getPageSize());
	         List list = rquery.getResultList();
	         long total = ((BigInteger)cquery.getSingleResult()).longValue();
	         return new PageImpl(list, pageable, total);
	      }
	   }

	   public <T> Page<T> findPageBySql(String sql, Map<String, Object> params, Pageable pageable, Class<T> c) {
	      Query query = this.em.createNativeQuery(sql);
	      this.setParameters(query, params);
	      ((SQLQuery)query.unwrap(SQLQuery.class)).setResultTransformer(Transformers.aliasToBean(c));
	      long total = 0L;
	      if(pageable != null) {
	         query.setFirstResult(pageable.getOffset());
	         query.setMaxResults(pageable.getPageSize());
	         String content = "select count(0) from (" + sql + ") t";
	         Query countQuery = this.em.createNativeQuery(content);
	         this.setParameters(countQuery, params);
	         total = ((BigInteger)countQuery.getSingleResult()).longValue();
	      }

	      List content1 = query.getResultList();
	      return new PageImpl(content1, pageable, pageable != null?total:(long)content1.size());
	   }

	   public Page<Map> findPageMapBySql(String sql, Map<String, Object> params, Pageable pageable) {
	      Query query = this.em.createNativeQuery(sql);
	      ((SQLQuery)query.unwrap(SQLQuery.class)).setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
	      this.setParameters(query, params);
	      long total = 0L;
	      if(pageable != null) {
	         query.setFirstResult(pageable.getOffset());
	         query.setMaxResults(pageable.getPageSize());
	         String content = "select count(0) from (" + sql + ") t";
	         Query countQuery = this.em.createNativeQuery(content);
	         this.setParameters(countQuery, params);
	         total = ((BigInteger)countQuery.getSingleResult()).longValue();
	      }

	      List content1 = query.getResultList();
	      return new PageImpl(content1, pageable, pageable != null?total:(long)content1.size());
	   }

	   public <T> Page<T> findPageEntityBySql(String sql, Map<String, Object> params, Pageable pageable, Constructor<T> cons) {
	      Query query = this.em.createNativeQuery(sql);
	      ((SQLQuery)query.unwrap(SQLQuery.class)).setResultTransformer(new AliasToBeanConstructorResultTransformer(cons));
	      this.setParameters(query, params);
	      long total = 0L;
	      if(pageable != null) {
	         query.setFirstResult(pageable.getOffset());
	         query.setMaxResults(pageable.getPageSize());
	         String content = "select count(0) from (" + sql + ") t";
	         Query countQuery = this.em.createNativeQuery(content);
	         this.setParameters(countQuery, params);
	         total = ((BigInteger)countQuery.getSingleResult()).longValue();
	      }

	      List content1 = query.getResultList();
	      return new PageImpl(content1, pageable, pageable != null?total:(long)content1.size());
	   }

	   public <T> Page<T> findPageBySql(String sql, List params, Pageable pageable, Class<T> c) {
	      Query query = this.em.createNativeQuery(sql);
	      this.setParameters(query, params);
	      ((SQLQuery)query.unwrap(SQLQuery.class)).setResultTransformer(Transformers.aliasToBean(c));
	      long total = 0L;
	      if(pageable != null) {
	         query.setFirstResult(pageable.getOffset());
	         query.setMaxResults(pageable.getPageSize());
	         String content = "select count(*) from (" + sql + ") t";
	         Query countQuery = this.em.createNativeQuery(content);
	         this.setParameters(countQuery, params);
	         total = ((BigInteger)countQuery.getSingleResult()).longValue();
	      }

	      List content1 = query.getResultList();
	      return new PageImpl(content1, pageable, pageable != null?total:(long)content1.size());
	   }

	   public <T> List<T> findListBySql(String hql, Map<String, Object> params, Class<T> c) {
	      Query query = this.em.createNativeQuery(hql);
	      this.setParameters(query, params);
	      ((SQLQuery)query.unwrap(SQLQuery.class)).setResultTransformer(Transformers.aliasToBean(c));
	      return query.getResultList();
	   }

	   public <T> List<T> findListBySql(String sql, List params, Class<T> c) {
	      Query query = this.em.createNativeQuery(sql);
	      this.setParameters(query, params);
	      ((SQLQuery)query.unwrap(SQLQuery.class)).setResultTransformer(Transformers.aliasToBean(c));
	      return query.getResultList();
	   }

	   public List<Map<String, Object>> findMapBySql(String sql, List params) {
	      Query query = this.em.createNativeQuery(sql);
	      this.setParameters(query, params);
	      ((SQLQuery)query.unwrap(SQLQuery.class)).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
	      return query.getResultList();
	   }

	   public List<Map<String, Object>> findMapBySql(String sql, Map params) {
	      Query query = this.em.createNativeQuery(sql);
	      this.setParameters(query, params);
	      ((SQLQuery)query.unwrap(SQLQuery.class)).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
	      return query.getResultList();
	   }

	   public void executeUpdateSQL(String sql) throws Exception {
	      this.em.createNativeQuery(sql).executeUpdate();
	   }
}
