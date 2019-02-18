package com.firm.orders.warehouse.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.firm.orders.base.utils.JavaUuidGenerater;
import com.firm.orders.warehouse.entity.WarehouseEntity;
import com.firm.orders.warehouse.service.IWarehouseService;
import com.firm.orders.warehouse.vo.WarehouseVO;

@Service
public class WarehouseServiceImpl extends BaseServiceImpl<WarehouseEntity, WarehouseVO> implements IWarehouseService{

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Override
	public Page<WarehouseVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from warehouse_info where 1=1");
		if(map !=null){
			if(map.containsKey("keyWords")){
				String keyWords = (String) map.get("keyWords");
				if(null != keyWords && !keyWords.equals("")){
					sql.append(" and (name like '"+keyWords+"%' or code like '"+keyWords+"%')");
				}
			}
		}
		sql.append(" order by create_time desc");
		int total =  getTotalCount(sql.toString());
		if(pageable != null){
			sql.append(" limit " + pageable.getPageNumber() * pageable.getPageSize() + "," + pageable.getPageSize());
		}
		List<WarehouseVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<WarehouseVO>(WarehouseVO.class));
		if (list != null && list.size() > 0) {
		
			return new PageImpl<WarehouseVO>(list, pageable, pageable != null ? total : (long) list.size());
		}
		return null;
	}

	@Override
	public List<WarehouseVO> save(List<WarehouseVO> list) throws Exception {
		List<WarehouseVO> addList = new ArrayList<>();
		List<String> delIds = new ArrayList<>();
		List<String> sqls = new ArrayList<>();
		if(null != list && list.size()>0){
			for(WarehouseVO vo:list){
				if(vo.getName()==null || vo.getName().equals("")){
					throw new Exception("名称为空的数据!");
				}
				/*if(vo.getOrdeCodePrefix()==null || vo.getOrdeCodePrefix().equals("")){
					vo.setOrdeCodePrefix("order");
				}*/
				if(vo.getId()!=null && !vo.getId().equals("")){
					delIds.add(vo.getId());	
					if(vo.getVoState()!=3){
						addList.add(vo);
					}
				}else{
					if(vo.getVoState()!=3){
						addList.add(vo);
					}
					
				}
			}
			if(delIds!=null && delIds.size()>0){
				String delIdsList = delIds.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
						.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
				String delLSql = "delete from warehouse_info where id in "+delIdsList;
				sqls.add(delLSql);
			}
			if(addList!=null && addList.size()>0){
				for(WarehouseVO vo:addList){
					if(vo.getId() == null || vo.getId().equals("")){
						String id = JavaUuidGenerater.generateUuid();
						vo.setId(id);
						vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
					}
					vo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
					
					StringBuffer sql = new StringBuffer("insert into warehouse_info"
							+ " (id,create_time,update_time,memo,name,code,orde_code_prefix,biz_range)"
							+ " values ");
					sql.append("(");
					sql.append("'"+vo.getId()+"',");
					if( null !=vo.getCreateTime()){
						sql.append("'"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(vo.getCreateTime())+"',");
					}else{
						sql.append("#"+null+"#,");
					}					
					sql.append("'"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(vo.getUpdateTime())+"',");
					if(null != vo.getMemo()){
						sql.append("'"+vo.getMemo()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					if(null !=vo.getName()){
						sql.append("'"+vo.getName()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					if(null !=vo.getCode()){
						sql.append("'"+vo.getCode()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					if(null !=vo.getOrdeCodePrefix()){
						sql.append("'"+vo.getOrdeCodePrefix()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					sql.append(vo.getBizRange()+",");
					sql.append(")");
					sqls.add(sql.toString().replace("#", ""));
				}
			}
			
			jdbcTemplate.batchUpdate(sqls.toArray(new String[sqls.size()]));
		}
		
		return queryList(null, null).getContent();
	}
	
	private int getTotalCount(String sql) {
	      String totalSql = "select count(1) from (" + sql + ") t";
	      Integer total = (Integer)this.jdbcTemplate.queryForObject(totalSql, Integer.class);
	      return total.intValue();
	}



}
