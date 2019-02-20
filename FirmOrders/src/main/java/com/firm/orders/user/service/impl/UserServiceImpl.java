package com.firm.orders.user.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firm.orders.assessory.service.IAssessoryService;
import com.firm.orders.assessory.vo.AssessoryVO;
import com.firm.orders.base.service.impl.BaseServiceImpl;
import com.firm.orders.base.utils.ChineseToPinyinHelper;
import com.firm.orders.base.utils.JavaUuidGenerater;
import com.firm.orders.base.utils.SnowflakeIdGenerater;
import com.firm.orders.base.utils.SymmetricEncoder;
import com.firm.orders.role.service.IRoleService;
import com.firm.orders.role.vo.RoleVO;
import com.firm.orders.user.entity.UserEntity;
import com.firm.orders.user.service.IUserService;
import com.firm.orders.user.vo.LoginedUserVO;
import com.firm.orders.user.vo.UserOwnResourceVO;
import com.firm.orders.user.vo.UserResourceReportVO;
import com.firm.orders.user.vo.UserVO;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserEntity, UserVO> implements IUserService {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private IAssessoryService assessoryService;
	
	@Autowired
	private IRoleService roleService;
	
	@Value("${encrypt.encodeRules}")
	private String encodeRules;

	@Override
	public LoginedUserVO login(String loginName,String password) throws Exception {			
		Subject subject = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(loginName, password);
		//token.setRememberMe(true);
		subject.login(token);
		UserVO user = (UserVO) subject.getSession().getAttribute("currentUser");
		user.setPassword(null);
		LoginedUserVO loginUser= new LoginedUserVO();
		loginUser.setLoginedUser(user);
		loginUser.setLogintime(subject.getSession().getLastAccessTime());
		Long timout = subject.getSession().getTimeout();
		Date outofServicetime = new Date(subject.getSession().getLastAccessTime().getTime()+timout);
		loginUser.setOutofServicetime(outofServicetime);
		String loginToken = SymmetricEncoder.AESEncode(encodeRules, JavaUuidGenerater.generateUuid()
				+loginName+"//"+subject.getSession().getId()+"//"
				+new SnowflakeIdGenerater(21,14).nextId());
		loginUser.setToken(loginToken);
		return loginUser;

	}
	
	@Transactional
	@Override
	public void delete(String id) throws Exception {
		String detailDelSql = "delete from user_own_resource where user_id in (select id from user_info where id='"+id+"')";
		String mainDelSql = "delete from user_info where id = '" + id + "'";
		jdbcTemplate.batchUpdate(detailDelSql,mainDelSql);
	}

	@Override
	@Transactional
	public void deleteByPhone(String phone) throws Exception {
		String detailDelSql = "delete from user_own_resource where user_id in (select id from user_info where phone='"+phone+"')";
		String mainDelSql = "delete from user_info where phone = '" + phone + "'";
		jdbcTemplate.batchUpdate(detailDelSql,mainDelSql);
	}
	
	public UserVO anth(String loginName,String password) throws Exception {	
		if (loginName == null || loginName.equals("")) {
			throw new Exception("没有登录名信息");
		}
		if (password == null || password.equals("")) {
			throw new Exception("没有登录password信息");
		}
		UserVO user = queryUserByPhoneOrCode(loginName);
		if(!password.trim().equals(SymmetricEncoder.AESDncode(encodeRules, user.getPassword().trim())) ){
			throw new Exception("密码错误！");
		}
		return user;

	}

	@Override
	public UserVO queryUserByPhoneOrCode(String code) throws Exception {
		String sql = "select u.*,r.role_code roleCode,r.role_name roleName,r.level roleLevel,r.biz_range roleBizRange from user_info u left join role_info r on u.role_id=r.id where 1=1 and  (u.phone='"
				+ code + "' or u.user_code='" + code + "')";
		List<UserVO> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<UserVO>(UserVO.class));
		if(users == null || users.size()<=0){
			throw new Exception("查询用户不存在！");
		}
		if(users.size() >1){
			throw new Exception("查询的用户存在多个！");
		}
		UserVO user = users.get(0);
		if (user != null && user.getId() != null && !user.getId().equals("")) {
			if(user.getIsFrozen()==1){
				throw new Exception("用户已冻结！");
			}
			List<UserVO> list = new ArrayList<>();
			list.add(user);
			queryOwnResources(list);
			return queryAssessorys(list).get(0);
		}
		return null;

	}
	
	@Override
	public UserVO findVOById(String id, Class<UserVO> cls) throws Exception {
		String sql = "select u.*,r.role_code roleCode,r.role_name roleName,r.level roleLevel,r.biz_range roleBizRange from user_info u left join role_info r on u.role_id=r.id where 1=1 and u.id='"
				+ id + "'";
		List<UserVO> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<UserVO>(UserVO.class));
		if(users==null || users.size()<=0){
			throw new Exception("没有查询到对应的用户");
		}
		if(users.size() >1){
			throw new Exception("对应的用户不唯一");
		}
		queryOwnResources(users).get(0);
		return queryAssessorys(users).get(0);
	}


	@Override
	public Page<UserVO> queryUserList(Pageable pageable, Map<String, Object> map) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select u.*,r.role_code roleCode,r.role_name roleName,r.level roleLevel,r.biz_range roleBizRange from user_info u left join role_info r on u.role_id=r.id where 1=1");
		if(map !=null){
			if(map.containsKey("keyWords")){
				String keyWords = (String) map.get("keyWords");
				if(null != keyWords && !keyWords.equals("")){
					sql.append(" and (u.user_code like '"+keyWords+"%' or u.user_name like '"+keyWords+"%' or u.phone like '"+keyWords+"%')");
				}
			}
			if(map.containsKey("isFrozen")){
				int dr = (int) map.get("isFrozen");
				if(dr==0){
					sql.append(" and u.is_frozen=0");
				}else if(dr==1){
					sql.append(" and u.is_frozen=1");
				}
			}
		}
		sql.append(" and u.role_id  <> (select role.id from role_info role where role.role_code='001')");
		
		if (getCurrentUser().getRoleLevel() == 3) {
			sql.append(" and u.id='" + getCurrentUser().getId() + "'");
		}
		if (getCurrentUser().getRoleLevel() == 2) {
			sql.append(" and r.biz_range= " + getCurrentUser().getRoleBizRange());
		}
		
		sql.append(" order by u.create_time desc");
		int total =  getTotalCount(sql.toString());
		if(pageable != null){
			sql.append(" limit " + pageable.getPageNumber() * pageable.getPageSize() + "," + pageable.getPageSize());
		}
		List<UserVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<UserVO>(UserVO.class));
		if (list != null && list.size() > 0) {
			queryAssessorys(list);
			queryOwnResources(list);
			return new PageImpl<UserVO>(list, pageable, pageable != null ? total : (long) list.size());
		}
		return null;
	}
	
	
	private int getTotalCount(String sql) {
	      String totalSql = "select count(1) from (" + sql + ") t";
	      Integer total = (Integer)this.jdbcTemplate.queryForObject(totalSql, Integer.class);
	      return total.intValue();
	}


	@Override
	@Transactional
	public UserVO save(UserVO vo, Class<UserEntity> clazzE, Class<UserVO> clazzV) throws Exception {
		if (vo == null) {
			throw new Exception("没有数据!");
		}
		if (vo.getPhone() == null || vo.getPhone().equals("")) {
			throw new Exception("phone没有数据!");
		}
		RoleVO roleVO = getCurrentUserRole();
		RoleVO preUserRole = queryUserRole(vo);
		if(roleVO !=null && roleVO.getLevel()==2 && roleVO.getBizRange() != preUserRole.getBizRange()){
			throw new Exception("没有权限保存此角色用户的数据！");
		}
		UserVO vo1 = new UserVO(); 
		BeanUtils.copyProperties(vo,vo1);
		vo1.setChildrenDetail(vo.getChildrenDetail());
		vo1.setAssessorys(vo.getAssessorys());
		
		if(vo1.getId()==null || vo1.getId().equals("")){//新增
			//手机号不能重复，用户名为已注册手机号的也不能注册
			String sql = "select * from user_info where 1=1 and  phone in ('"+vo.getPhone()+"','"+vo.getUserName()+"')";
			List<UserVO> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<UserVO>(UserVO.class));
			if (users.size() > 0) {
				throw new Exception("用户已占用!");
			}
			String sql1 = "select count(1) from user_info where 1=1 and  user_name like '"+vo.getUserName()+"%'";
			Integer count = jdbcTemplate.queryForObject(sql1, Integer.class);
			if(count== 0){
				if(vo1.getUserName()!=null && !vo1.getUserName().equals("")){
					vo1.setUserCode(ChineseToPinyinHelper.toLowerPinYin(vo1.getUserName()));
				}else{
					vo1.setUserCode(ChineseToPinyinHelper.toLowerPinYin(vo1.getPhone()));
				}	
				
			}else{
				vo1.setUserCode(ChineseToPinyinHelper.toLowerPinYin(vo1.getUserName())+"0"+count);
			}
			if(vo1.getPassword()==null || vo1.getPassword().equals("")){
				vo1.setPassword(SymmetricEncoder.AESEncode(encodeRules, "000000"));
			}else{
				vo1.setPassword(SymmetricEncoder.AESEncode(encodeRules, vo1.getPassword()));
			}
		}else{
			String sql = "select * from user_info where 1=1 and  id ='"+vo.getId()+"'";
			List<UserVO> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<UserVO>(UserVO.class));
			if(users == null ||users.isEmpty()){
				throw new Exception("用户不存在!");
			}
			vo1.setUserCode(users.get(0).getUserCode());
			vo1.setPassword(users.get(0).getPassword());
			vo1.setCreateTime(users.get(0).getCreateTime());
		}
		UserVO user = super.save(vo1, clazzE, clazzV);
		user.setChildrenDetail(vo1.getChildrenDetail());
		saveUserOwnResources(user.getId(),user.getChildrenDetail());
		if (user != null && user.getId() != null && !user.getId().equals("")) {
			if (vo1.getAssessorys() != null && vo1.getAssessorys().size() > 0) {
				List<String> delAIds= new ArrayList<>();
				List<String> addAIds = new ArrayList<>();
				for(AssessoryVO avo:vo1.getAssessorys()){
					if(avo.getDr()==1){
						delAIds.add(avo.getId());
					}else if(avo.getBusinessId() ==null || avo.getBusinessId().equals("")){
						addAIds.add(avo.getId());
					}
				}
				if(delAIds !=null && delAIds.size()>0){
					String idList = delAIds.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
							.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
					String sql2 ="delete from assessory_info where id in "+idList;
					jdbcTemplate.update(sql2);
				}
				if(addAIds !=null && addAIds.size()>0){
					String idList = addAIds.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
							.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
					String sql3 ="update  assessory_info set business_id='"+user.getId()+"' where id in "+idList;
					jdbcTemplate.update(sql3);
				}
			}
			List<UserVO> list = new ArrayList<>();
			list.add(user);
			queryOwnResources(list).get(0);
			queryUserRole(list).get(0);
			return queryAssessorys(list).get(0);
		}
		return null;
	}
	
	private List<UserVO> queryUserRole(List<UserVO> users) throws Exception{
		if (users == null || users.size() <= 0) {
			return null;
		}
		List<String> roleIds = new ArrayList<>();
		for (UserVO vo : users) {
			roleIds.add(vo.getRoleId());
		}
		String idList = roleIds.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
				.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
		String sql = "select * from role_info where id in "+idList;
		List<RoleVO> list= jdbcTemplate.query(sql, new BeanPropertyRowMapper<RoleVO>(RoleVO.class));
		if (list != null && list.size() > 0) {
			for (RoleVO vo : list) {
				for (UserVO b : users) {
					if (b.getRoleId().equals(vo.getId())) {
						b.setRoleCode(vo.getRoleCode());
						b.setRoleName(vo.getRoleName());
						b.setRoleLevel(vo.getLevel());
					}
					
				}
			}
		}
		return users;
		
	}
	
	private RoleVO queryUserRole(UserVO user) throws Exception {
		if (user == null) {
			return null;
		}
		String sql = "select * from role_info where id = '" + user.getRoleId() + "'";
		List<RoleVO> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<RoleVO>(RoleVO.class));
		return list.get(0);
	}

	private List<UserVO> queryOwnResources(List<UserVO> users) throws Exception {
		if (users == null || users.size() <= 0) {
			return null;
		}
		List<String> userIds = new ArrayList<>();
		for (UserVO vo : users) {
			userIds.add(vo.getId());
		}
		String idList = userIds.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
				.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
		String sql = "select * from user_own_resource where user_id in "+idList;
		List<UserOwnResourceVO> list= jdbcTemplate.query(sql, new BeanPropertyRowMapper<UserOwnResourceVO>(UserOwnResourceVO.class));
		if (list != null && list.size() > 0) {
			for(UserVO user:users){
				List<UserOwnResourceVO> a = new ArrayList<>();
				for (UserOwnResourceVO vo : list) {
					if (user.getId().equals(vo.getUserId())) {
						a.add(vo);
					}
				}
				user.setChildrenDetail(a);
			}
		}
		return users;
	}
	
	@Override
	public List<UserOwnResourceVO> queryOwnResources(String userId) throws Exception{
		if (userId == null || userId.equals("")) {
			throw new Exception("用户Id不能为空!");
		}
		List<UserOwnResourceVO> list1 = jdbcTemplate.query("select * from user_own_resource where user_id='"+userId+"' order by create_time desc", new BeanPropertyRowMapper<UserOwnResourceVO>(UserOwnResourceVO.class));
		return list1;
	}
	
	private List<UserVO> queryAssessorys(List<UserVO> users) throws Exception {
		if (users == null || users.size() <= 0) {
			return null;
		}
		List<String> userIds = new ArrayList<>();
		for (UserVO vo : users) {
			userIds.add(vo.getId());
		}
		List<AssessoryVO> assessorys = assessoryService.queryAssessorys(userIds);
		if (assessorys != null && assessorys.size() > 0) {
			for (UserVO b : users) {
				List<AssessoryVO> a = new ArrayList<>();
				for (AssessoryVO assessory : assessorys) {
					if (b.getId().equals(assessory.getBusinessId())) {
						a.add(assessory);
					}
					b.setAssessorys(a);
				}
			}
		}
		return users;
	}

	
	@Override
	@Transactional
	public void frozenUser(String id,int type) throws Exception {
		StringBuilder sql = new StringBuilder( );
		if(type == 0){//解冻
			sql.append("update user_info set is_frozen=0 where 1=1 and id='"+id+"'");
		}else {//冻结
			sql.append("update user_info set is_frozen=1 where 1=1 and id='"+id+"'");
		}
		jdbcTemplate.update(sql.toString());
		
	}

	@Transactional
	@Override
	public String reSetPassWord(String phone) throws Exception {
		checkUser(phone);
		String defalutPwd="000000";
		String defalutPwdenCode = SymmetricEncoder.AESEncode(encodeRules, "000000");
		String sql = "update user_info set password='"+defalutPwdenCode+"' where 1=1 and phone='"+phone+"'";
		jdbcTemplate.update(sql);
		return defalutPwd;
	}

	@Transactional
	@Override
	public void updatePassword(String phone,String password) throws Exception {
		checkUser(phone);
		String enPwd = SymmetricEncoder.AESEncode(encodeRules, password);
		String sql = "update user_info set password='"+enPwd+"' where 1=1 and phone='"+phone+"'";
		jdbcTemplate.update(sql);
	}
	
	@Transactional
	@Override
	public void changeUserPic(String phone,String assessoryId,int assessoryBusinessType) throws Exception{
		checkUser(phone);
		String delSql = "delete from assessory_info where business_type=" + assessoryBusinessType
				+ " and business_id = (select id from user_info where phone='" + phone + "')";
		String insertSql = "update assessory_info set business_id = (select id from user_info where phone='" + phone
				+ "') where id='" + assessoryId + "'";
		String[] str = new String[2];
		str[0] = delSql;
		str[1] = insertSql;
		jdbcTemplate.batchUpdate(str);
		
	}

	private boolean checkUser(String phone) throws Exception{
		
		if(phone == null || phone.equals("")){
			throw new Exception("手机号不能为空");
		}
		String sql = "select * from user_info where 1=1 and  phone='"+phone+"'";
		List<UserVO> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<UserVO>(UserVO.class));
		if(users == null || users.size()<=0){
			throw new Exception("用户不存在！");
		}
		if(users.size() >1){
			throw new Exception("用户存在多个！");
		}
		return true;
	}

	@Transactional
	@Override
	public List<UserOwnResourceVO> saveUserOwnResources(String userId,List<UserOwnResourceVO> list) throws Exception{
		if(null == userId || userId.equals("")){
			throw new Exception("用户id不能为空！");
		}
		List<UserOwnResourceVO> addList = new ArrayList<>();
		List<String> delIds = new ArrayList<>();
		List<String> sqls = new ArrayList<>();
		if(null != list && list.size()>0){
			for(UserOwnResourceVO vo:list){
				if(vo.getResourcePhone()==null || vo.getResourcePhone().equals("")){
					throw new Exception("存在资源手机号为空的数据!");
				}
				if(vo.getResourceWechatCode()==null || vo.getResourceWechatCode().equals("")){
					throw new Exception("存在资源微信号为空的数据!");
				}
				if(vo.getId()!=null && !vo.getId().equals("")){
					delIds.add(vo.getId());	
					if(vo.getVoState()!=3){
						addList.add(vo);
					}
				}else{
					if(vo.getVoState()!=3){
						vo.setUserId(userId);
						addList.add(vo);
					}
					
				}
			}
			if(delIds!=null && delIds.size()>0){
				String delIdsList = delIds.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
						.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
				String delLSql = "delete from user_own_resource where id in "+delIdsList;
				sqls.add(delLSql);
			}
			if(addList!=null && addList.size()>0){
				for(UserOwnResourceVO vo:addList){
					if(vo.getId() == null || vo.getId().equals("")){
						String id = JavaUuidGenerater.generateUuid();
						vo.setId(id);
						vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
					}
					vo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
					
					StringBuffer sql = new StringBuffer("insert into user_own_resource"
							+ " (id,create_time,update_time,memo,resource_wechat_code,resource_phone,min_fans,user_id)"
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
					if(null !=vo.getResourceWechatCode()){
						sql.append("'"+vo.getResourceWechatCode()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					if(null !=vo.getResourcePhone()){
						sql.append("'"+vo.getResourcePhone()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					sql.append(vo.getMinFans()+",");
					if(null !=vo.getUserId()){
						sql.append("'"+vo.getUserId()+"'");
					}else{
						sql.append("#"+null+"#");
					}
					sql.append(")");
					sqls.add(sql.toString().replace("#", ""));
				}
			}
			
			jdbcTemplate.batchUpdate(sqls.toArray(new String[sqls.size()]));
		}
		
		return queryOwnResources(userId);
		
	}
	
	@Transactional
	public UserVO saveUserOwnResources1(String userId,List<UserOwnResourceVO> list) throws Exception{
		List<UserOwnResourceVO> reUserORList = saveUserOwnResources(userId,list);
		UserVO reVO = findVOById(userId, UserVO.class);
		if(reVO!=null){
			reVO.setChildrenDetail(reUserORList);
			List<UserVO> users = new ArrayList<>();
			users.add(reVO);
			queryAssessorys(users);
			return users.get(0);
		}
		return null;
	}

	@Override
	@Transactional
	public void deleteUserOR(String id) throws Exception {
		if(id==null || id.equals("")){
			throw new Exception("id为空!");
		}
		String detailDelSql = "delete from user_own_resource where id ='"+id+"'";
		jdbcTemplate.update(detailDelSql);
		
	}

	@Override
	public UserOwnResourceVO qureyOneUserORById(String id) throws Exception {
		if(id==null || id.equals("")){
			throw new Exception("id为空!");
		}
		String sql = "select * from user_own_resource where id ='"+id+"'";
		List<UserOwnResourceVO> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<UserOwnResourceVO>(UserOwnResourceVO.class));
		if(list ==null ||list.isEmpty()){
			return null;
		}
		return list.get(0);
	}

	@Override
	public Page<UserOwnResourceVO> queryUserORList(Pageable pageable, Map<String, Object> map) throws Exception {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from user_own_resource where 1=1");
		String currentUserId = (String) ((UserVO) SecurityUtils.getSubject().getSession().getAttribute("currentUser")).getId();
		String currentUserName =  (String) ((UserVO) SecurityUtils.getSubject().getSession().getAttribute("currentUser")).getUserName();
		String currentUserCode = (String) ((UserVO) SecurityUtils.getSubject().getSession().getAttribute("currentUser")).getUserCode();
		RoleVO roleVO  = getCurrentUserRole();
		if(roleVO != null ){
			if(roleVO.getLevel() ==2){
				//二级管理员
				sql.append(" and user_id in (select us.id from user_info us,role_info role where role.biz_range="+roleVO.getBizRange()+")");
			}
			if(roleVO.getLevel()==3){
				//业务员
				sql.append(" and user_id = '" + currentUserId + "'");
			}
			
		}
		/*if ("004".equals(roleCode)) {
			sql.append(" and user_id = '" + currentUserId + "'");
		}*/
		if(map !=null){
			if(map.containsKey("keyWords")){
				String keyWords = (String) map.get("keyWords");
				if(null != keyWords && !keyWords.equals("")){
					if(roleVO != null && roleVO.getLevel()==3 && !keyWords.startsWith(currentUserName) && !keyWords.startsWith(currentUserCode)){
							//业务员
							sql.append(" and (resource_phone like '"+keyWords+"%' or resource_wechat_code like '"+keyWords+"%')");
						}else{
						sql.append(" and (resource_phone like '"+keyWords+"%' or resource_wechat_code like '"+keyWords+"%'"
								+ " or user_id in (select id from user_info where (user_name like '"+keyWords+"%') or (user_code like '"+keyWords+"%')))");
					}
				}
			}
			if(map.containsKey("userId")){
				String userId = (String) map.get("userId");
				if(roleVO != null && roleVO.getLevel()==3 && !currentUserId.equals(userId)){
					return null;
				}
				if(userId != null && !userId.equals("")){
					sql.append(" and user_id='"+userId+"'");
				}
			}
		}
		sql.append(" order by create_time desc,user_id");
		int total =  getTotalCount(sql.toString());
		if(pageable != null){
			sql.append(" limit " + pageable.getPageNumber() * pageable.getPageSize() + "," + pageable.getPageSize());
		}
		List<UserOwnResourceVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<UserOwnResourceVO>(UserOwnResourceVO.class));
		if (list != null && list.size() > 0) {
			return new PageImpl<UserOwnResourceVO>(list, pageable, pageable != null ? total : (long) list.size());
		}
		return null;
	}
	
	private RoleVO getCurrentUserRole() throws Exception{
		//当前用户
		Subject subject = SecurityUtils.getSubject();
		UserVO user = (UserVO) subject.getSession().getAttribute("currentUser");
		RoleVO roleVO = roleService.findVOById(user.getRoleId(), RoleVO.class);
		return roleVO;
	}
	
	private UserVO getCurrentUser() throws Exception{
		//当前用户
		Subject subject = SecurityUtils.getSubject();
		UserVO user = (UserVO) subject.getSession().getAttribute("currentUser");
		return user;
	}

	@Override
	public Object countRegionUserOwnResources(Map<String, Object> map) throws Exception {
		RoleVO roleVO = getCurrentUserRole();
		if (roleVO != null && roleVO.getLevel()==3) {
			throw new Exception("业务员没有操作权限!");
		}
	 
		Date date = null;
		if(map!= null && map.containsKey("date")){
			String dateStr = (String) map.get("date");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
			date = dateFormat.parse(dateStr);
		}else{
			date = new Date();
		}
		String firstDate = "2019-02-28 23:59:59";
		String dateStr = getMonthLastdate(date,1);
		String beforeDateStr = getMonthLastdate(date,2);
		StringBuffer sql = new StringBuffer("select sum(case when  uor.create_time <='"+firstDate+"' then uor.min_fans else 0 end ) as fristNum,");  
		sql.append(" sum(case when uor.create_time <='"+beforeDateStr+"' then uor.min_fans else 0 end ) as  beforeNum,");
		sql.append(" sum(case when uor.create_time <='"+dateStr+"' then uor.min_fans else 0 end ) as  currentNum,");
		sql.append(" sum(case when uor.create_time between  '"+beforeDateStr+"' and '"+dateStr+"' then uor.min_fans else 0 end ) as  addNum,");
		sql.append(" sum(case when uor.create_time between  '"+firstDate+"' and '"+dateStr+"' then uor.min_fans else 0 end ) as  sumAddNum,");
		sql.append(" us.region as region");
		sql.append(" from user_own_resource uor");
		sql.append(" inner join user_info us on uor.user_id = us.id");
		sql.append(" left join role_info role on us.role_id = role.id");
		sql.append(" where 1=1 ");
		sql.append(" and role.biz_range="+roleVO.getBizRange());
		sql.append(" group by  us.region");
		List<UserResourceReportVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<UserResourceReportVO>(UserResourceReportVO.class));
		if(list==null ||list.isEmpty()){
			return null;
		}
		
		List<Map<String,Object>> reList= new ArrayList<>();
		Map<String,Object> fistMap = new HashMap<>();
		fistMap.put("region", "区域");
		fistMap.put("fristNum",firstDate.substring(0,7).replace("-", "年")+"月");
		fistMap.put("beforeNum", beforeDateStr.substring(0,7).replace("-", "年")+"月");
		fistMap.put("currentNum", dateStr.substring(0,7).replace("-", "年")+"月");
		fistMap.put("addNum", "增粉数");
		fistMap.put("sumAddNum", "总增粉");
		reList.add(fistMap);
		
		int sumfristNum = 0;
		int sumBeforeNum=0;
		int sumcurrentNum=0;
		int sumAddNum=0;
		int sumSumAddNum=0;
		for(UserResourceReportVO vo:list){
			Map<String,Object> partMap = new HashMap<>();
			partMap.put("region", vo.getRegion());
			partMap.put("fristNum",vo.getFristNum());
			sumfristNum = sumfristNum+vo.getFristNum();
			partMap.put("beforeNum", vo.getBeforeNum());
			sumBeforeNum = sumBeforeNum+vo.getBeforeNum();
			partMap.put("currentNum", vo.getCurrentNum());
			sumcurrentNum = sumcurrentNum+vo.getCurrentNum();
			partMap.put("addNum", vo.getAddNum());
			sumAddNum= sumAddNum+vo.getAddNum();
			partMap.put("sumAddNum", vo.getSumAddNum());
			sumSumAddNum = sumSumAddNum+vo.getSumAddNum();
			reList.add(partMap);
		}
		
		
		Map<String,Object> endMap = new HashMap<>();
		endMap.put("region", "合计");
		/*endMap.put("fristNum",list.stream().mapToInt(UserResourceReportVO::getFristNum).sum());
		endMap.put("beforeNum", list.stream().mapToInt(UserResourceReportVO::getBeforeNum).sum());
		endMap.put("currentNum", list.stream().mapToInt(UserResourceReportVO::getCurrentNum).sum());
		endMap.put("addNum",list.stream().mapToInt(UserResourceReportVO::getAddNum).sum());
		endMap.put("sumAddNum", list.stream().mapToInt(UserResourceReportVO::getSumAddNum).sum());*/
		endMap.put("fristNum",sumfristNum);
		endMap.put("beforeNum",sumBeforeNum);
		endMap.put("currentNum", sumcurrentNum);
		endMap.put("addNum",sumAddNum);
		endMap.put("sumAddNum", sumSumAddNum);
		reList.add(endMap);
		
		return reList;
	}
	
	public String getMonthLastdate(Date date,int type) throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		//int month = calendar.get(Calendar.MONTH);
		if(type==1){
			calendar.add(Calendar.MONTH, 0);
		}else if(type==2){
			calendar.add(Calendar.MONTH, -1);
		}
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(calendar.getTime())+" 23:59:59";
	}
	
	
	
}
