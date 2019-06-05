package com.firm.order.modules.product.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.firm.order.modules.base.service.impl.BaseServiceImpl;
import com.firm.order.modules.product.entity.ProductEntity;
import com.firm.order.modules.product.service.IProductService;
import com.firm.order.modules.product.vo.ProductVO;
import com.firm.order.modules.user.vo.UserVO;

@Service
public class ProductServiceImpl extends BaseServiceImpl<ProductEntity, ProductVO> implements IProductService{
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	

	@Override
	public Page<ProductVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("Select product.*,warehouse.name  wareHouseName "
				+ "from product_info product"
				+ " left join warehouse_info warehouse"
				+ " on warehouse.code = product.warehouse"
				+ "  where 1=1");
		if(map !=null){
			if(map.containsKey("keyWords")){
				String keyWords = (String) map.get("keyWords");
				if(null != keyWords && !keyWords.equals("")){
					sql.append(" and (warehouse like '"+keyWords+"%' or name like '"+keyWords+"%' or bar_code like '"+keyWords+"%')");
				}
			}
		}
		if(getCurrentUser().getRoleLevel()>1) {
			sql.append(" and warehouse in (select code from warehouse_info where biz_range="+getCurrentUser().getRoleBizRange()+")");

		}
		sql.append(" order by create_time desc");
		int total =  getTotalCount(sql.toString());
		if(pageable != null){
			sql.append(" limit " + pageable.getPageNumber() * pageable.getPageSize() + "," + pageable.getPageSize());
		}
		List<ProductVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<ProductVO>(ProductVO.class));
		if (list != null && list.size() > 0) {
			return new PageImpl<ProductVO>(list, pageable != null?pageable: PageRequest.of(0,list.size()), pageable != null ? total : (long) list.size());
		}
		return null;
	}
	
	private int getTotalCount(String sql) {
	      String totalSql = "select count(1) from (" + sql + ") t";
	      Integer total = (Integer)this.jdbcTemplate.queryForObject(totalSql, Integer.class);
	      return total.intValue();
	}
	
	private UserVO getCurrentUser() throws Exception{
		//当前用户
		Subject subject = SecurityUtils.getSubject();
		UserVO user = (UserVO) subject.getSession().getAttribute("currentUser");
		return user;
	}

}
