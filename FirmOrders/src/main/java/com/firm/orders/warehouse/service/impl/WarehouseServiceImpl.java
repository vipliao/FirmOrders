package com.firm.orders.warehouse.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.firm.orders.base.service.impl.BaseServiceImpl;
import com.firm.orders.base.utils.JavaUuidGenerater;
import com.firm.orders.user.vo.UserVO;
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
		UserVO user = getCurrentUser();
		if(user.getRoleLevel()>=2){
			sql.append(" and biz_range="+user.getRoleBizRange());
		}
		if(map !=null){
			if(map.containsKey("keyWords")){
				String keyWords = (String) map.get("keyWords");
				if(null != keyWords && !keyWords.equals("")){
					sql.append(" and (name like '"+keyWords+"%' or code like '"+keyWords+"%')");
				}
			}
		}
		sql.append(" order by code asc,create_time desc");
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

	private String increaseCode(String code) {
		int s = Integer.parseInt(code);
		++s;
		String reslut = s > 10 ? (s > 100 ? s + "" : "0" + s) : "00" + s; 
		return reslut;
	}
	
	@Override
	public WarehouseVO save(WarehouseVO vo, Class<WarehouseEntity> clazzE, Class<WarehouseVO> clazzV) throws Exception {
		checkNameAndCode(vo);
		if(vo.getCode()==null || vo.getCode().equals("")) {
			String sql1 = "select max(code) from warehouse_info where code like '00%'";
			List<String> list1 = jdbcTemplate.queryForList(sql1.toString(), String.class);
			String beginCode=null;
			if(CollectionUtils.isEmpty(list1)){
				beginCode="001";
			}else{
				beginCode = increaseCode(list1.get(0));
			}
			vo.setCode(beginCode);
		}
		
		return super.save(vo, clazzE, clazzV);
	}
	
	@Transactional
	@Override
	public List<WarehouseVO> save(List<WarehouseVO> list) throws Exception {
		List<WarehouseVO> addList = new ArrayList<>();
		List<String> delIds = new ArrayList<>();
		List<String> sqls = new ArrayList<>();
		if(null != list && list.size()>0){
			String sql1 = "select max(code) from warehouse_info where code like '00%'";
			List<String> list1 = jdbcTemplate.queryForList(sql1.toString(), String.class);
			String beginCode=null;
			if(CollectionUtils.isEmpty(list1) || list1.get(0)==null || list1.get(0).equals("")){
				beginCode="001";
			}else{
				beginCode = increaseCode(list1.get(0));
			}
			for(int i=0;i<list.size();i++){
				checkNameAndCode(list.get(i));
				/*if(vo.getOrdeCodePrefix()==null || vo.getOrdeCodePrefix().equals("")){
					vo.setOrdeCodePrefix("order");
				}*/
				if(list.get(i).getId()!=null && !list.get(i).getId().equals("")){
					delIds.add(list.get(i).getId());	
					if(list.get(i).getVoState()!=3){
						addList.add(list.get(i));
					}
				}else{
					if(list.get(i).getVoState()!=3){
						if(list.get(i).getCode()==null || list.get(i).getCode().equals("")){
							list.get(i).setCode(beginCode);
							beginCode = increaseCode(beginCode);
						}
						
						addList.add(list.get(i));
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
					sql.append(vo.getBizRange());
					sql.append(")");
					sqls.add(sql.toString().replace("#", ""));
				}
			}
			
			jdbcTemplate.batchUpdate(sqls.toArray(new String[sqls.size()]));
		}
		
		return queryList(null, null).getContent();
	}
	private void checkNameAndCode(WarehouseVO vo) throws Exception {
		UserVO user = getCurrentUser();
		if(user.getRoleLevel()>=2 && vo.getBizRange()!=user.getRoleBizRange()) {
			throw new Exception("没有保存数据的权限!");
		}
		if(vo.getName()==null || vo.getName().equals("")){
			throw new Exception("存在名称为空的数据!");
		}
		if(vo.getId() ==null || vo.getId().equals("")){
			StringBuffer sql = new StringBuffer("select * from warehouse_info where name='"+vo.getName()+"'");
			if(vo.getCode() != null && vo.getCode().equals("")) {
				sql.append("' or code='"+vo.getCode()+"'");
			}
			List<WarehouseVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<WarehouseVO>(WarehouseVO.class));
			if(list !=null) {
				for(WarehouseVO db: list) {
					if(db.getName().equals(vo.getName())) {
						throw new Exception("存在名称"+vo.getName()+"重复的数据!");
					}
					if(vo.getCode() != null && !vo.getCode().equals("") && db.getCode().equals(vo.getCode())){
						throw new Exception("存在编码"+vo.getCode()+"重复的数据!");
					}
				}
			}
		}
		
	}
	
	private int getTotalCount(String sql) {
	      String totalSql = "select count(1) from (" + sql + ") t";
	      Integer total = (Integer)this.jdbcTemplate.queryForObject(totalSql, Integer.class);
	      return total.intValue();
	}
	private UserVO getCurrentUser(){
		Subject subject = SecurityUtils.getSubject();
		UserVO user = (UserVO) subject.getSession().getAttribute("currentUser");
		return user;
	}

	@Override
	public WarehouseVO findVOByCode(String code) throws Exception {
		String sql = "select * from warehouse_info where code ='"+code+"'";
		List<WarehouseVO> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<WarehouseVO>(WarehouseVO.class));
		return list.get(0);
	}
	


}
