package com.firm.order.modules.sender.service.impl;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.firm.order.modules.base.service.impl.BaseServiceImpl;
import com.firm.order.modules.sender.entity.SenderEntity;
import com.firm.order.modules.sender.service.ISenderService;
import com.firm.order.modules.sender.vo.SenderVO;

@Service
public class SenderServiceImpl extends BaseServiceImpl<SenderEntity, SenderVO> implements ISenderService{

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	
	@Override
	public Page<SenderVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("Select * from sender_info where 1=1");
		if(map !=null){
			if(map.containsKey("keyWords")){
				String keyWords = (String) map.get("keyWords");
				if(null != keyWords && !keyWords.equals("")){
					sql.append(" and (sender_name like '"+keyWords+"%' or sender_phone like '"+keyWords+"%' or sender_addr like '"+keyWords+"%')");
				}
			}
			if(map.containsKey("enabled")){
				int enabled = Integer.parseInt((String) map.get("enabled"));
				sql.append(" and is_enabled ="+enabled);
			}
		}
		sql.append(" order by create_time desc");
		int total =  getTotalCount(sql.toString());
		if(pageable != null){
			sql.append(" limit " + pageable.getPageNumber() * pageable.getPageSize() + "," + pageable.getPageSize());
		}
		List<SenderVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<SenderVO>(SenderVO.class));
		if (list != null && list.size() > 0) {
			return new PageImpl<SenderVO>(list, pageable, pageable != null ? total : (long) list.size());
		}
		return null;
	}
	private int getTotalCount(String sql) {
	      String totalSql = "select count(1) from (" + sql + ") t";
	      Integer total = (Integer)this.jdbcTemplate.queryForObject(totalSql, Integer.class);
	      return total.intValue();
	}
	
	@Transactional
	@Override
	public SenderVO save(SenderVO vo, Class<SenderEntity> clazzE, Class<SenderVO> clazzV) throws Exception {
		String sql = "select count(1) from sender_info";
		int count = jdbcTemplate.queryForObject(sql, Integer.class);
		if(count==0){
			vo.setIsEnabled(1);
		}
		return super.save(vo, clazzE, clazzV);
	}
	
	@Transactional
	@Override
	public void delete(String id) throws Exception {
		String sql = "select * from sender_info where id='"+id+"'";
		List<SenderVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<SenderVO>(SenderVO.class));
		if(list == null|| list.isEmpty()){
			throw new Exception("要删除的数据不存在!");
		}
		if(list.get(0).getIsEnabled()==1){
			throw new Exception("不能删除已启用的数据!");
		}
		super.delete(id);
	}
	
	@Transactional
	@Override
	public void enabled(String id,int enabled) throws Exception {
		if(id==null|| id.equals("")){
			throw new Exception("id不能为空!");
		}
		if(enabled !=0 && enabled !=1){
			throw new Exception("启用/停用状态设置不正确!");
		}
		String sql = "update sender_info set is_enabled="+enabled+" where id='"+id+"'";
		jdbcTemplate.update(sql);
	}
}
