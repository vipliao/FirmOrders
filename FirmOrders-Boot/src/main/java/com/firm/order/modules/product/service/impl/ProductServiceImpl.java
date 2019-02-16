package com.firm.order.modules.product.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.firm.order.modules.base.service.impl.BaseServiceImpl;
import com.firm.order.modules.product.entity.ProductEntity;
import com.firm.order.modules.product.service.IProductService;
import com.firm.order.modules.product.vo.ProductVO;

@Service
public class ProductServiceImpl extends BaseServiceImpl<ProductEntity, ProductVO> implements IProductService{
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Page<ProductVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("Select * from product_info where 1=1");
		if(map !=null){
			if(map.containsKey("keyWords")){
				String keyWords = (String) map.get("keyWords");
				if(null != keyWords && !keyWords.equals("")){
					sql.append(" and (warehouse like '"+keyWords+"%' or name like '"+keyWords+"%' or bar_code like '"+keyWords+"%')");
				}
			}
		}
		sql.append(" order by create_time desc");
		int total =  getTotalCount(sql.toString());
		if(pageable != null){
			sql.append(" limit " + pageable.getPageNumber() * pageable.getPageSize() + "," + pageable.getPageSize());
		}
		List<ProductVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<ProductVO>(ProductVO.class));
		if (list != null && list.size() > 0) {
			return new PageImpl<ProductVO>(list, pageable, pageable != null ? total : (long) list.size());
		}
		return null;
	}
	
	private int getTotalCount(String sql) {
	      String totalSql = "select count(1) from (" + sql + ") t";
	      Integer total = (Integer)this.jdbcTemplate.queryForObject(totalSql, Integer.class);
	      return total.intValue();
	}

}
