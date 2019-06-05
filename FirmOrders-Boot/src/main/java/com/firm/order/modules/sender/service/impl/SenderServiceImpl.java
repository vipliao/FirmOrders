package com.firm.order.modules.sender.service.impl;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
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
import org.springframework.util.StringUtils;

import com.firm.order.modules.base.service.impl.BaseServiceImpl;
import com.firm.order.modules.sender.entity.SenderEntity;
import com.firm.order.modules.sender.service.ISenderService;
import com.firm.order.modules.sender.vo.SenderVO;
import com.firm.order.modules.user.vo.UserVO;

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
		
		UserVO userVO = getCurrentUser();
		if(userVO != null ) {
			if(userVO.getRoleLevel()>1){
				sql.append(" and biz_range="+userVO.getRoleBizRange());
			}
		}
		sql.append(" order by create_time desc");
		int total =  getTotalCount(sql.toString());
		if(pageable != null){
			sql.append(" limit " + pageable.getPageNumber() * pageable.getPageSize() + "," + pageable.getPageSize());
		}
		List<SenderVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<SenderVO>(SenderVO.class));
		if (list != null && list.size() > 0) {
			return new PageImpl<SenderVO>(list, pageable != null?pageable: PageRequest.of(0,list.size()), pageable != null ? total : (long) list.size());
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
		UserVO userVO = getCurrentUser();
		if(userVO != null ) {
			if(userVO.getRoleLevel()>1 && !StringUtils.isEmpty(vo.getId()) && vo.getBizRange() != userVO.getRoleBizRange()){
				throw new Exception("没有权限保存业务范围外的数据!");
			}
			if(StringUtils.isEmpty(vo.getId())){
				vo.setBizRange(userVO.getRoleBizRange());
			}
		}
		String sql = "select * from sender_info";
		List<SenderVO> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<SenderVO>(SenderVO.class));
		if(CollectionUtils.isEmpty(list) || list.size()==1){
			vo.setIsEnabled(1);
		}
		if (!CollectionUtils.isEmpty(list) && list.size() == 2 && list.get(0).getBizRange() != vo.getBizRange()
				&& list.get(1).getBizRange() != vo.getBizRange()) {
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
		String sql1 = "select * from sender_info where id='"+id+"'";
		List<SenderVO> list = jdbcTemplate.query(sql1, new BeanPropertyRowMapper<SenderVO>(SenderVO.class));
		if(list == null|| list.isEmpty()){
			throw new Exception("要操作的数据不存在!");
		}
		UserVO userVO = getCurrentUser();
		if(userVO != null ) {
			if(userVO.getRoleLevel()>1 && list.get(0).getBizRange() != userVO.getRoleBizRange()){
				throw new Exception("没有权限保存业务范围外的数据!");
			}
		}
		if(id==null|| id.equals("")){
			throw new Exception("id不能为空!");
		}
		if(enabled !=0 && enabled !=1){
			throw new Exception("启用/停用状态设置不正确!");
		}
		String sql = "update sender_info set is_enabled="+enabled+" where id='"+id+"'";
		jdbcTemplate.update(sql);
	}
	
	private UserVO getCurrentUser(){
		Subject subject = SecurityUtils.getSubject();
		UserVO user = (UserVO) subject.getSession().getAttribute("currentUser");
		return user;
	}
}
