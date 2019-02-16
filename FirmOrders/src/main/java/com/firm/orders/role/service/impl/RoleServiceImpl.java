package com.firm.orders.role.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.firm.orders.base.service.impl.BaseServiceImpl;
import com.firm.orders.role.entity.RoleEntity;
import com.firm.orders.role.service.IRoleService;
import com.firm.orders.role.vo.RoleVO;

@Service
public class RoleServiceImpl extends BaseServiceImpl<RoleEntity, RoleVO> implements IRoleService{
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Page<RoleVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("Select * from role_info where 1=1");
		if(map !=null){
			
		}
		sql.append(" and role_code <> '001'");
		sql.append(" order by create_time desc");
		int total =  getTotalCount(sql.toString());
		if(pageable != null){
			sql.append(" limit " + pageable.getPageNumber() * pageable.getPageSize() + "," + pageable.getPageSize());
		}
		List<RoleVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<RoleVO>(RoleVO.class));
		if (list != null && list.size() > 0) {
			return new PageImpl<RoleVO>(list, pageable, pageable != null ? total : (long) list.size());
		}
		return null;
	}
	
	
	private int getTotalCount(String sql) {
	      String totalSql = "select count(1) from (" + sql + ") t";
	      Integer total = (Integer)this.jdbcTemplate.queryForObject(totalSql, Integer.class);
	      return total.intValue();
	}

}
