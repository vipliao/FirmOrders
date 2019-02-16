package com.firm.order.modules.assessory.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.firm.order.modules.assessory.entity.AssessoryEntity;
import com.firm.order.modules.assessory.service.IAssessoryService;
import com.firm.order.modules.assessory.vo.AssessoryVO;
import com.firm.order.modules.base.service.impl.BaseServiceImpl;

@Service
public class AssessoryServiceImpl extends BaseServiceImpl<AssessoryEntity, AssessoryVO> implements IAssessoryService{
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<AssessoryVO> queryAssessorys(List<String> businessIds) throws Exception{
		String idList = businessIds.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
				.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
		String sql="select * from assessory_info where 1=1 and business_id in"+idList;
		return jdbcTemplate.query(sql,new BeanPropertyRowMapper<>(AssessoryVO.class));
	}

	

	@Override
	public Page<AssessoryVO> queryByType(Pageable pageable,Map<String,Object> map) throws Exception {
		StringBuilder sql=new StringBuilder("select * from assessory_info where 1=1 and dr=0 ");
		String type=null;
		if(map.containsKey("type")){
			type = (String) map.get("type");	
		}
		if(type!=null){
			if(type.equals("1")){
				sql.append(" and type='1'");
			}else if(type.equals("2")){
				sql.append(" and type='2'");
			}else if(type.equals("0")){
				sql.append(" and type ='0'");
			} 
		}
		int total =  getTotalCount(sql.toString());
		if(pageable != null){
			sql.append(" limit " + pageable.getPageNumber() * pageable.getPageSize() + "," + pageable.getPageSize());
		}
		List<AssessoryVO> list = jdbcTemplate.query(sql.toString(),new BeanPropertyRowMapper<>(AssessoryVO.class));
		return new PageImpl<AssessoryVO>(list, pageable, pageable != null ? total : (long) list.size());

	}
	
	private int getTotalCount(String sql) {
	      String totalSql = "select count(1) from (" + sql + ") t";
	      Integer total = (Integer)this.jdbcTemplate.queryForObject(totalSql, Integer.class);
	      return total.intValue();
	}
	
	

}
