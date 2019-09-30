package com.firm.order.modules.order.service.impl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.firm.order.modules.base.entity.BaseEntity;
import com.firm.order.modules.base.entity.SuperEntity;
import com.firm.order.modules.base.service.impl.BaseServiceImpl;
import com.firm.order.modules.order.entity.OrderEntity;
import com.firm.order.modules.order.service.IOrderService;
import com.firm.order.modules.order.vo.OrderProductVO;
import com.firm.order.modules.order.vo.OrderVO;
import com.firm.order.modules.product.vo.ProductVO;
import com.firm.order.modules.user.vo.UserVO;
import com.firm.order.modules.warehouse.service.IWarehouseService;
import com.firm.order.modules.warehouse.vo.WarehouseVO;
import com.firm.order.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;




@Service
public class OrderServiceImpl extends BaseServiceImpl<OrderEntity, OrderVO> implements IOrderService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private IWarehouseService warehouseService;

	//@Autowired
	//private ThreadPoolTaskExecutor taskExecutor;

	@Transactional
	@Override
	public OrderVO save(OrderVO vo, Class<OrderEntity> clazzE, Class<OrderVO> clazzV) throws Exception {
		OrderEntity entity = bulidEntity(vo, clazzE, clazzV);
		List<OrderProductVO> reDetailVOs  = saveOrderProduct(entity.getId(),vo.getWarehouse(),vo.getChildrenDetail());
		if(entity.getId() !=null && !entity.getId().equals("")){
			String mainDelSql = "delete from order_info where id = '" + entity.getId() + "'";
			jdbcTemplate.update(mainDelSql);
		}
		List<OrderEntity> saveList = new ArrayList<>();
		saveList.add(entity);
		List<OrderEntity> newList = bathSave(saveList);
		OrderVO reVO = handleSingleE2V(newList.get(0), OrderVO.class);
		/*List<OrderVO> list = new ArrayList<>();
		list.add(reVO);
		reVO = queryOrderProducts(list).get(0);*/
		reVO.setChildrenDetail(reDetailVOs);
		return reVO;

	}

	@Transactional
	OrderEntity bulidEntity(OrderVO vo, Class<OrderEntity> clazzE, Class<OrderVO> clazzV) throws Exception{
		if (vo == null) {
			throw new Exception("没有数据!");
		}
		if(null == vo.getChildrenDetail() || vo.getChildrenDetail().isEmpty()){
			throw new Exception("订单中没有添加有关产品的数据!");
		}
		List<OrderProductVO> validChildren= new ArrayList<>();
		for(OrderProductVO detail : vo.getChildrenDetail()){
			if(3 != detail.getVoState()) {
				validChildren.add(detail);
			}
		}
		if(CollectionUtils.isEmpty(validChildren)) {
			throw new Exception("订单中没有添加有关产品的数据!");
		}

		if(vo.getDeliverDate() == null || vo.getDeliverDate().toString().equals("")){
			throw new Exception("订单发货时间不能为空!");
		}
		UserVO userVO  = getCurrentUser();
		if(userVO != null){
			WarehouseVO warehouseVO = warehouseService.findVOByCode(vo.getWarehouse());
			if(warehouseVO != null){
				if(userVO.getRoleLevel()>1 &&  userVO.getRoleBizRange() !=  warehouseVO.getBizRange()){
					throw new Exception("没有权限操作"+warehouseVO.getName()+"仓库的订单!");
				}
			}
			if(userVO.getRoleLevel()>=3 && !vo.getRegion().startsWith(userVO.getRegion())){
				throw new Exception("没有权限操作"+userVO.getRegion()+"区域的订单!");
			}
			if(userVO.getRoleLevel()==4){//业务员
				//当天时间过了10点,不能新增发货时间为今天的订单
				String deliverDateStr = new SimpleDateFormat("yyMMdd").format(vo.getDeliverDate());
				String today = new SimpleDateFormat("yyMMdd").format(new Date());
				Date todayLockTime = new SimpleDateFormat("yyMMdd hh:mm:ss").parse(today+" 16:00:00");
				if(today.equals(deliverDateStr) && new Date().getTime()>todayLockTime.getTime()){
					throw new Exception("发货日期为今天的订单不处于业务员可操作的状态!");
				}
				if(Integer.parseInt(today) > Integer.parseInt(deliverDateStr)){
					throw new Exception("发货日期为今天之前的订单不处于业务员可操作的状态!");
				}
			}

		}

		if(vo.getId()!=null){
			if(vo.getOrderCode() == null || vo.getOrderCode().equals("")){
				throw new Exception("订单编号不能为空!");
			}
			/*boolean isCando = isSalesManCando(vo.getId());
			if(!isCando){
				throw new Exception("订单不处于业务员可操作的状态!");
			}*/
		}else{

			vo.setOrderCode(generaterBillCode(vo.getWarehouse(),vo.getDeliverDate()));

		}
		vo.setCostRatio(calculateCostRatio(vo));
		vo.setIsOverCost(isOverCost(vo)?0:1);
		vo.setRegion(vo.getRegion().trim());
		OrderEntity entity = handleSingleV2E(vo, OrderEntity.class);
		if (entity.getId() == null || entity.getId().equals("")) {
			((SuperEntity) entity).setId(JavaUuidGenerater.generateUuid());
			((SuperEntity) entity).setCreateTime(new Timestamp(System.currentTimeMillis()));
			((SuperEntity) entity).setUpdateTime(new Timestamp(System.currentTimeMillis()));

		}else{
			OrderEntity e = findEntityById(entity.getId(), clazzE);
			((SuperEntity) entity).setCreateTime(((SuperEntity) e).getCreateTime());
			((SuperEntity) entity).setUpdateTime(new Timestamp(System.currentTimeMillis()));

		}

		return entity;
	}


	@SuppressWarnings("unchecked")
	public <T extends BaseEntity> List<T> bathSave(List<T> list) throws Exception{
		if (list == null || list.isEmpty()) {
			return null;
		}

		Map<String, Object> map = getFieldNames((Class<T>) list.get(0).getClass());
		List<String> fieldNames = (ArrayList<String>) map.get("fieldNames");
		List<String> dbFieldNames = (ArrayList<String>) map.get("dbFieldNames");
		String tableName = (String) map.get("tableName");
		String primaryKeyName = (String) map.get("primaryKeyName");
		StringBuffer sql = getJdbcInsertSql(dbFieldNames, tableName);
		List<Object[]> batchArgs = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			T entity = list.get(i);
			/*if(entity.getAttributeValue(primaryKeyName) ==null || entity.getAttributeValue(primaryKeyName).equals("")){
				entity.setAttributeValue(primaryKeyName,mainKey );
			}*/
			Object[] rows = new Object[dbFieldNames.size()];
			for (int j = 0; j < dbFieldNames.size(); ++j) {
				rows[j] = entity.getAttributeValue((String) fieldNames.get(j));
			}
			batchArgs.add(rows);
		}
		jdbcTemplate.batchUpdate(sql.toString(), batchArgs);
		return list;
	}



	private StringBuffer getJdbcInsertSql(List<String> dbFiledNames, String tableName) throws Exception {
		StringBuffer sql = new StringBuffer();
		StringBuffer sql1 = new StringBuffer("(");
		sql.append("insert into   " + tableName + "(");
		for (int j = 0; j < dbFiledNames.size(); ++j) {
			sql.append((String) dbFiledNames.get(j));
			sql1.append("?");
			if (dbFiledNames.size() - 1 != j) {
				sql.append(",");
				sql1.append(",");
			}
		}

		sql.append(") VALUES");
		sql1.append(")");
		/*sql.append("(");

		for (int j = 0; j < dbFiledNames.size(); ++j) {
			sql.append("?");
			if (dbFiledNames.size() - 1 != j) {
				sql.append(",");
			}
		}

		sql.append(")");*/
		return sql.append(sql1);
	}



	/*private void setAudits(SuperEntity entity,int type) throws Exception{
		if (entity.getId() == null || entity.getId().equals("")) {
			((SuperEntity) entity).setCreateTime(new Timestamp(System.currentTimeMillis()));
			((SuperEntity) entity).setUpdateTime(new Timestamp(System.currentTimeMillis()));

		}else{
			SuperEntity e  = null;
			if(type==1){
				e = findEntityById(entity.getId(), OrderEntity.class);
			}else{
				String sql = "select * from order_product where id ='"+entity.getId()+"'";
				List<OrderProductEntity> list= jdbcTemplate.query(sql, new BeanPropertyRowMapper<OrderProductEntity>(OrderProductEntity.class));
				if(list != null && list.size()>0){
					e = list.get(0);
				}
			}
			if(e != null ){
				((SuperEntity) entity).setCreateTime(((SuperEntity) e).getCreateTime());
			}else{
				((SuperEntity) entity).setCreateTime(new Timestamp(System.currentTimeMillis()));
			}
			((SuperEntity) entity).setUpdateTime(new Timestamp(System.currentTimeMillis()));

		}
	}
	*/
	@Override
	public OrderVO findVOById(String id, Class<OrderVO> cls) throws Exception {
		OrderVO vo = super.findVOById(id, cls);
		List<OrderVO> list = new ArrayList<>();
		list.add(vo);
		vo = queryOrderProducts(list).get(0);
		return vo;
	}

	public BigDecimal calculateCostRatio(OrderVO vo) throws Exception{
		if (vo == null) {
			throw new Exception("订单信息不能为空");
		}
		if (null == vo.getChildrenDetail() || vo.getChildrenDetail().isEmpty()) {
			throw new Exception("订单"+vo.getOrderCode()+"中没有添加有关产品的数据!");
		}
		List<OrderProductVO> validChildren= new ArrayList<>();
		for(OrderProductVO detail : vo.getChildrenDetail()){
			if(3 != detail.getVoState()) {
				validChildren.add(detail);
			}
		}
		if(CollectionUtils.isEmpty(validChildren)) {
			throw new Exception("订单"+vo.getOrderCode()+"中没有添加有关产品的数据!");
		}
		BigDecimal costRatio = new BigDecimal(0.00);
		BigDecimal sumProductCost = new BigDecimal(0.00);
		List<String> productIdList = validChildren.stream().map(OrderProductVO::getProductId).collect(Collectors.toList());
		String productIdListStr = productIdList.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
				.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
		List<ProductVO> products = jdbcTemplate.query("select * from product_info where id in "+productIdListStr,new BeanPropertyRowMapper<ProductVO>(ProductVO.class));
		//根据仓库业务范围区分是男科还是蜂蜜，//暂时注释
		WarehouseVO warehouseVO = warehouseService.findVOByCode(vo.getWarehouse());
		for (OrderProductVO productVO : validChildren) {
			if (productVO.getProductCostPrice() == null
					|| productVO.getProductCostPrice().compareTo(new BigDecimal(0.00)) <= 0) {
				List<ProductVO> newList = products.stream().filter(a -> a.getId().equals(productVO.getProductId())).distinct().collect(Collectors.toList());
				BigDecimal price = newList!=null && !newList.isEmpty()?newList.get(0).getCostPrice():null;
				if(price ==null || price.compareTo(new BigDecimal(0.00))<=0){
					List<OrderProductVO> pro = jdbcTemplate.query("select * from order_product where id = '"+productVO.getId()+"'",new BeanPropertyRowMapper<OrderProductVO>(OrderProductVO.class));
					if(pro!=null && !pro.isEmpty()){
						price = pro.get(0).getProductCostPrice();
						productVO.setProductCostPrice(price);
					}else{
						throw new Exception("产品" + productVO.getProductName() + "成本价格不能为空，请联系管理员！");
					}
				}else{
					productVO.setProductCostPrice(price);
				}

			}
			if (productVO.getPnumber() == null || productVO.getPnumber().compareTo(new BigDecimal(0.00)) <= 0) {
				throw new Exception("产品" + productVO.getProductName() + "数量不能为空！");
			}
			sumProductCost = sumProductCost.add(productVO.getPnumber().multiply(productVO.getProductCostPrice()));
			if (warehouseVO != null && "005".equals(warehouseVO.getCode())) {
				sumProductCost = sumProductCost.add(productVO.getPnumber().multiply(new BigDecimal(18)));
			}
		}

		if (vo.getCollectionAmout() == null || vo.getCollectionAmout().compareTo(new BigDecimal(0.00)) <= 0) {
			vo.setCollectionAmout(new BigDecimal(0.00));
		}
		if (vo.getDepositAmout() == null || vo.getDepositAmout().compareTo(new BigDecimal(0.00)) <= 0) {
			vo.setDepositAmout(new BigDecimal(0.00));
		}
		vo.setTotalAmount(vo.getDepositAmout().add(vo.getCollectionAmout()));
		BigDecimal costAmount =new BigDecimal(0.00);


		if(warehouseVO != null){
			if(warehouseVO.getBizRange()==1){
				//蜂蜜

				/* ==========================广西仓库=======================================
				 *  成本费用=货物费（订单中产品的sum（产品成本价格*数量））+服务费/打包费
				 *  		+手续费（每个订单代收金额*百分比）+邮费/运费
				 *  成本比例=仓库成本费用/订单总金额
				 * ==========================================================================
				 */
				if ("005".equals(warehouseVO.getCode())) {
					costAmount = sumProductCost.add(new BigDecimal(3)).add(vo.getCollectionAmout().multiply(new BigDecimal(0.02)));
				} else {
					if (vo.getExpressCompany()==0){
						//顺丰
						costAmount = (sumProductCost.add(new BigDecimal(5.5))
								.add(vo.getCollectionAmout().multiply(new BigDecimal(0.05))));
					}if (vo.getExpressCompany()==1){
						//邮政
						costAmount = (sumProductCost.add(new BigDecimal(5.5))
								.add(vo.getCollectionAmout().multiply(new BigDecimal(0.015))));
					}else if(vo.getExpressCompany()==2 || vo.getExpressCompany()==3){
						//中通或圆通
						costAmount = (sumProductCost.add(new BigDecimal(5.5))
								.add(vo.getCollectionAmout().multiply(new BigDecimal(0.015))).add(new BigDecimal(9)));
					}else if(vo.getExpressCompany()==4){
						//德邦
						costAmount = (sumProductCost.add(new BigDecimal(5.5))
								.add(vo.getCollectionAmout().multiply(new BigDecimal(0.015))).add(new BigDecimal(40)));
					}else if(vo.getExpressCompany()==5) {
						//联邦
						costAmount = sumProductCost.add(new BigDecimal(5.5));
					} else {
						costAmount = sumProductCost.add(new BigDecimal(5.5D)).add(vo.getCollectionAmout().multiply(new BigDecimal(0.015D))).add(new BigDecimal(9));
					}
				}
				vo.setCostAmount(costAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN));
			}else if(warehouseVO.getBizRange()==2){
				//男科

				/* ==========================武汉仓库=======================================
				 *  成本费用=货物费（订单中产品的sum（产品成本价格*数量））+服务费（每个订单3元）
				 *  		+手续费（每个订单代收金额2.5%）+邮费（每个订单50）
				 *	成本比例=武汉仓库成本费用/订单总金额
				 * ==========================================================================
				 */

				costAmount = (sumProductCost.add(new BigDecimal(3))
						.add(vo.getCollectionAmout().multiply(new BigDecimal(0.025))).add(new BigDecimal(50)));
				vo.setCostAmount(costAmount.setScale(2, BigDecimal.ROUND_HALF_EVEN));
			}

			if (vo.getTotalAmount() == null || vo.getTotalAmount().compareTo(new BigDecimal(0.00)) <= 0) {
				vo.setTotalAmount(new BigDecimal(0.00));
				costRatio = new BigDecimal(1.00);
			} else {
				costRatio = costAmount.divide(vo.getTotalAmount(),4,BigDecimal.ROUND_HALF_EVEN);
			}
		}else{
			throw new Exception("仓库数据输入有误！");
		}
		return costRatio;
	}

	private boolean isOverCost(OrderVO vo) throws Exception{
		if (vo.getOrderNature() == null || vo.getOrderNature().equals("")) {
			throw new Exception("订单性质不能为空！");
		}
		BigDecimal costRatio = calculateCostRatio(vo);

		//根据仓库业务范围区分是男科还是蜂蜜，暂时注释
		WarehouseVO warehouseVO = warehouseService.findVOByCode(vo.getWarehouse());
		if(warehouseVO != null){
			if(warehouseVO.getBizRange()==2){
				//男科
				/*===============================================================================
				 * 武汉仓库成本超过：订单性质为热线 、回访且成本比例小于16%，则不超；订单性质为复购且成本比例小于18% ，则不超
				 * 北京仓库成本超过：对于业务员订单性质为热线 、回访且成本比例小于24%则不超 ；订单性质为复购且成本比例小于26%则不超
				 * ===============================================================================
				 */

				if ("热线".equals(vo.getOrderNature()) || "回访".equals(vo.getOrderNature())) {
					if (new BigDecimal(0.16).compareTo(costRatio) < 0) {
						return false;
					}
				} else  {
					if (new BigDecimal(0.18).compareTo(costRatio) < 0) {
						return false;
					}
				}
			}else if(warehouseVO.getBizRange()==1){

				if ("005".equals(warehouseVO.getCode())) {
					if ((new BigDecimal(0.29)).compareTo(costRatio) < 0) {
						return false;
					}
				} else if (new BigDecimal(0.19).compareTo(costRatio) < 0) {
					//蜂蜜
					return false;
				}
			}
		}

		return true;
	}

	@Override
	@Transactional
	public void delete(String id) throws Exception {
		boolean isCando = isSalesManCando(id);
		if(!isCando){
			throw new Exception("订单不处于业务员可操作的状态!");
		}
		String detailDelSql = "delete from order_product where order_id in (select id from order_info where id='"+id+"')";
		String mainDelSql = "delete from order_info where id = '" + id + "'";
		jdbcTemplate.batchUpdate(detailDelSql,mainDelSql);
	}

	private boolean isSalesManCando(String orderId) throws Exception{
		OrderVO dbvo = findVOById(orderId, OrderVO.class);
		if(null !=dbvo){
			String today = new SimpleDateFormat("yyMMdd").format(new Date());
			int todayInt = Integer.parseInt(today);
			Date todayLockTime = new SimpleDateFormat("yyMMdd hh:mm:ss").parse(today+" 16:00:00");
			int deliverDateInt = Integer.parseInt(new SimpleDateFormat("yyMMdd").format(dbvo.getDeliverDate()));
			UserVO userVO = getCurrentUser();
			if (userVO != null && userVO.getRoleLevel()==4) {
				if(todayInt == deliverDateInt){//发货时间是今天
					if(new Date().getTime()>todayLockTime.getTime()){
						return false;
					}
				}else if(todayInt > deliverDateInt){//发货时间今天之前
					return false;
				}
			}
		}
		return true;
	}

	private String generaterBillCode(String warehouse,Date deliverDate) throws Exception{
		String prefix = "D";
		int digits = 4;
		if(warehouse.equals("001")){
			prefix = "S91";
			digits = 3;
		}else if (warehouse.equals("002")){
			prefix = "S7001";
			digits=4;
		}
		/*
		 * String deliverDateStr = new SimpleDateFormat("yyMMdd").format(deliverDate);
		 * String sql =
		 * "select max(order_code) from order_info where order_code like '"+prefix+
		 * deliverDateStr+"%'"; List<String> list =
		 * jdbcTemplate.queryForList(sql,String.class); if(list!=null &&
		 * !list.isEmpty()){ return
		 * BillCodeGenerater.generaterBillCode(prefix,deliverDate,
		 * "yyMMdd",digits,list.get(0)); }else{ return
		 * BillCodeGenerater.generaterBillCode(prefix,deliverDate,
		 * "yyMMdd",digits,null); }
		 */

		String orderCode =null;
		String deliverDateStr = FastDateFormat.getInstance("yyMMdd").format(deliverDate);
		String currentorderCode = (String) EhCacheUtil.getInstance().get("orderCodeCache", prefix+deliverDateStr);
		if(StringUtils.isBlank(currentorderCode)){
			String sql = "select max(order_code) from order_info where order_code like '"+prefix+deliverDateStr+"%'";
			List<String> list = jdbcTemplate.queryForList(sql,String.class);
			if(list!=null && !list.isEmpty()){
				orderCode = BillCodeGenerater.generaterBillCode(prefix,deliverDate, "yyMMdd",digits,list.get(0));
			}else{
				orderCode = BillCodeGenerater.generaterBillCode(prefix,deliverDate, "yyMMdd",digits,null);
			}
		}else{
			orderCode = BillCodeGenerater.generaterBillCode(prefix, deliverDate, "yyMMdd", digits,currentorderCode);
		}
		EhCacheUtil.getInstance().put("orderCodeCache", prefix+deliverDateStr, orderCode);
		return orderCode;
	}

	private List<OrderVO> queryOrderProducts(List<OrderVO> orders) throws Exception {
		if (orders == null || orders.size() <= 0) {
			return null;
		}
		List<String> orderIds = new ArrayList<>();
		for (OrderVO vo : orders) {
			orderIds.add(vo.getId());
		}
		String idList = orderIds.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
				.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
		String sql = "select product.*,warehouse.name productWarehouseName from order_product product left join warehouse_info warehouse on warehouse.code = product.product_warehouse where order_id in "+idList;
		List<OrderProductVO> list= jdbcTemplate.query(sql, new BeanPropertyRowMapper<OrderProductVO>(OrderProductVO.class));
		if (list != null && list.size() > 0) {
			for (OrderVO b : orders) {
				List<OrderProductVO> a = new ArrayList<>();
				for (OrderProductVO vo : list) {
					if (b.getId().equals(vo.getOrderId())) {
						a.add(vo);
					}
				}
				b.setChildrenDetail(a);
			}

		}
		return orders;
	}

	@Transactional
	List<OrderProductVO> saveOrderProduct(String mainTableKey, String wareHouse, List<OrderProductVO> list) throws Exception{
		if(null == mainTableKey || mainTableKey.equals("")){
			throw new Exception("主表主键不能为空！");
		}
		if(null == wareHouse || wareHouse.equals("")){
			throw new Exception("所属仓库不能为空！");
		}
		List<OrderProductVO> addList = new ArrayList<>();
		List<String> delIds = new ArrayList<>();
		List<String> sqls = new ArrayList<>();
		List<String> productIds= new ArrayList<>();
		if(null != list && list.size()>0){
			for(OrderProductVO vo:list){
				if(vo.getProductBarCode() ==null && vo.getProductBarCode().equals("")){
					throw new Exception("存在有产品条码/代码为空的数据！");
				}
				if(vo.getId()!=null && !vo.getId().equals("")){
					delIds.add(vo.getId());
				}
				if(vo.getVoState()!=3){
					vo.setOrderId(mainTableKey);
					addList.add(vo);
					productIds.add(vo.getProductId());
				}

			}
			List<ProductVO> productList = new ArrayList<>();
			if(productIds !=null && productIds.size()>0){
				String productIdStr = productIds.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
						.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
				productList = jdbcTemplate.query("select  *  from product_info where id in"+productIdStr,new BeanPropertyRowMapper<ProductVO>(ProductVO.class));
			}
			if(productIds ==null || productIds.isEmpty()){
				throw new Exception("所选产品不存在!");
			}else{
				List<WarehouseVO> warehouses = warehouseService.queryList(null, null).getContent();
				for(ProductVO vo:productList){
					if(!vo.getWareHouse().equals(wareHouse)){
						if(!CollectionUtils.isEmpty(warehouses)) {
							String wareHouseName = "";
							for(WarehouseVO warehouse:warehouses) {
								if(wareHouse.equals(warehouse.getCode())){
									wareHouseName = warehouse.getName();
									break;
								}
							}
							throw new Exception("所选存在产品不属于订单所属仓库,订单所属仓库为"+wareHouseName);
						}
					}


				}
			}
			if(delIds!=null && delIds.size()>0){
				String delIdsList = delIds.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
						.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");
				String delLSql = "delete from order_product where id in "+delIdsList;
				sqls.add(delLSql);
			}

			if(addList!=null && addList.size()>0){
				for(OrderProductVO vo:addList){
					for(ProductVO product:productList){
						if(vo.getProductId().equals(product.getId())){
							vo.setProductWarehouse(product.getWareHouse());
							vo.setProductCostPrice(product.getCostPrice());
							vo.setProductBarCode(product.getBarCode());
						}
					}
					if(vo.getId() == null || vo.getId().equals("")){
						String id = JavaUuidGenerater.generateUuid();
						vo.setId(id);
						vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
					}else{

						List<Date>  createTime= jdbcTemplate.queryForList("select create_time  from order_product where id ='"+vo.getId()+"'", Date.class);
						vo.setCreateTime(createTime.get(0));
					}
					vo.setUpdateTime(new Timestamp(System.currentTimeMillis()));

					StringBuffer sql = new StringBuffer("insert into order_product"
							+ " (id,create_time,update_time,memo,product_id,product_name,pnumber,product_unit,product_bar_code,product_cost_price,order_id,product_warehouse)"
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
					if(null !=vo.getProductId()){
						sql.append("'"+vo.getProductId()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					if(null !=vo.getProductName()){
						sql.append("'"+vo.getProductName()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					sql.append(vo.getPnumber()+",");
					if(null !=vo.getProductUnit()){
						sql.append("'"+vo.getProductUnit()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					if(null !=vo.getProductBarCode()){
						sql.append("'"+vo.getProductBarCode()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					if(null !=vo.getProductCostPrice()){
						sql.append("'"+vo.getProductCostPrice()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					if(null !=vo.getOrderId()){
						sql.append("'"+vo.getOrderId()+"',");
					}else{
						sql.append("#"+null+"#,");
					}
					if(null !=vo.getOrderId()){
						sql.append("'"+vo.getProductWarehouse()+"'");
					}else{
						sql.append("#"+null+"#");
					}
					sql.append(")");
					sqls.add(sql.toString().replace("#", ""));

				}
			}

			jdbcTemplate.batchUpdate(sqls.toArray(new String[sqls.size()]));
		}
		return addList;

	}

	@Override
	public Page<OrderVO> queryList(Pageable pageable, Map<String, Object> map) throws Exception {
		StringBuilder sql = queryList(map);
		sql.append(getOrderBySQL(null));
		int total =  getTotalCount(sql.toString());
		if(pageable != null){
			sql.append(" limit " + pageable.getPageNumber() * pageable.getPageSize() + "," + pageable.getPageSize());
		}
		List<OrderVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<OrderVO>(OrderVO.class));
		if (list != null && list.size() > 0) {
			List<WarehouseVO> warehouses = warehouseService.queryList(null, null).getContent();
			if(!CollectionUtils.isEmpty(warehouses)) {
				for(WarehouseVO warehouseVO : warehouses){
					for(OrderVO orderVO : list) {
						if(orderVO.getWarehouse().equals(warehouseVO.getCode())){
							orderVO.setWarehouseName(warehouseVO.getName());
						}
					}
					warehouseVO.getName();
				}
			}

			queryOrderProducts(list);
			return new PageImpl<OrderVO>(list, pageable, pageable != null ? total : (long) list.size());
		}
		return null;

	}

	private UserVO getCurrentUser() throws Exception{
		//当前用户
		Subject subject = SecurityUtils.getSubject();
		UserVO user = (UserVO) subject.getSession().getAttribute("currentUser");
		return user;
	}

	@Override
	public Page<OrderVO> queryWaitEnsureList(Pageable pageable, Map<String, Object> map) throws Exception{
		map.put("optionType", 1);
		return queryList(pageable, map);
	}


	private int getTotalCount(String sql) {
		String totalSql = "select count(1) from (" + sql + ") t";
		Integer total = (Integer)this.jdbcTemplate.queryForObject(totalSql, Integer.class);
		return total.intValue();
	}

	@Override
	@Transactional
	public void importWarehouseReceiptExcel(int warehouse, MultipartFile file) throws Exception {
		String templateName = "订单数据模板.xlsx";
		String sheetName = null;
		/*if (warehouse == 0) {
			templateName = "S91平台仓库回执模板.xls";
			sheetName = "发货表";
		} else if (warehouse == 1) {
			templateName = "52部仓库回执模板.xlsx";
			sheetName = "Sheet2";
		}*/
		List<Map<String, Object>> list = PoiHelper.importExcel(templateName, file, sheetName, 2);
		if (list == null || list.isEmpty()) {
			return;
		}
		List<String> importOrderCodes = new ArrayList<>();
		for (Map<String, Object> map : list) {
			if (map == null) {
				continue;
			}
			if(map.get("orderCode") == null || map.get("orderCode").equals("")){
				continue;
			}
			importOrderCodes.add((String)map.get("orderCode"));
		}
		String importOrderCodesStr = importOrderCodes.toString().replaceAll(" ", "").replaceAll("\\,", "\\'\\,\\'")
				.replaceAll("\\[", "\\('").replaceAll("\\]", "\\')");;
		/*String sql = "select * from order_info where (ISNULL(express_code) || LENGTH(trim(express_code))<1) and order_state <> 3"
				+ " and (deliver_date between date_sub(curdate(),interval 2 month) and (select max(deliver_date) from order_info))";*/
		String sql = "select * from order_info where order_code in"+importOrderCodesStr;
		List<OrderVO> dbDatas = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<OrderVO>(OrderVO.class));
		if (dbDatas != null && dbDatas.size() > 0) {
			List<String> saveSqls= new ArrayList<>();
			queryOrderProducts(dbDatas);
			for (OrderVO db : dbDatas) {
				Set<String> set = new HashSet<>();
				for (Map<String, Object> map : list) {
					if (map == null) {
						continue;
					}
					if(map.get("orderCode") == null || map.get("orderCode").equals("")){
						continue;
					}
					if(map.get("orderCode").equals(db.getOrderCode()) && map.get("receiverName").toString().trim().equals(db.getReceiverName().trim())){
						//赋值订单主表数据
						if(!set.contains(db.getOrderCode()+"-"+db.getReceiverName())){

							if(map.get("expressCode") !=null && !map.get("expressCode").equals("")){
								db.setExpressCode(map.get("expressCode").toString());
							}
							if(map.get("expressCompany") !=null){
								if (map.get("expressCompany").toString().startsWith("顺丰")) {
									db.setExpressCompany(0);
								} else if (map.get("expressCompany").toString().startsWith("邮政")) {
									db.setExpressCompany(1);
								}else if (map.get("expressCompany").toString().startsWith("圆通")) {
									db.setExpressCompany(2);
								}else if (map.get("expressCompany").toString().startsWith("中通")) {
									db.setExpressCompany(3);
								}else if (map.get("expressCompany").toString().startsWith("德邦")) {
									db.setExpressCompany(4);
								}else if (map.get("expressCompany").toString().startsWith("联邦")) {
									db.setExpressCompany(5);
								}
							}
							if(map.get("incomlineTime") !=null && !map.get("incomlineTime").equals("")){
								db.setIncomlineTime(new SimpleDateFormat("yyyy-MM-dd").parse(map.get("incomlineTime").toString()));
							}
							if(map.get("deliverDate") !=null && !map.get("deliverDate").equals("")){
								db.setDeliverDate((new SimpleDateFormat("yyyy-MM-dd").parse(map.get("deliverDate").toString())));
							}
							if(map.get("orderDate") !=null && !map.get("orderDate").equals("")){
								db.setOrderDate((new SimpleDateFormat("yyyy-MM-dd").parse(map.get("orderDate").toString())));
							}
							if(map.get("advertChannel") !=null && !map.get("advertChannel").equals("")){
								db.setAdvertChannel(map.get("advertChannel").toString());
							}
							if(map.get("orderNature") !=null && !map.get("orderNature").equals("")){
								db.setOrderNature((map.get("orderNature").toString()));
							}
							if(map.get("depositAmout") !=null && !map.get("depositAmout").equals("")){
								db.setDepositAmout(new BigDecimal(map.get("depositAmout").toString()));
							}
							if(map.get("collectionAmout") !=null && !map.get("collectionAmout").equals("")){
								db.setCollectionAmout(new BigDecimal(map.get("collectionAmout").toString()));
							}

							db.setExpressState(1);// 已发货
							db.setOrderState(2);// 已发快递
						}
						List<OrderProductVO> pList = db.getChildrenDetail();
						//赋值订单中产品明细字表数据
						if (pList == null || pList.isEmpty()) {
							continue;
						}
						List<String> barCodes = new ArrayList<>();
						Iterator<?> it = map.entrySet().iterator();
						while (it.hasNext()) {
							@SuppressWarnings("unchecked")
							Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
							if (entry.getKey().indexOf("productBarCode") != -1) {
								barCodes.add(entry.getKey());

							}
						}
						for (OrderProductVO child : pList) {
							if (child.getProductBarCode() == null || child.getProductBarCode().equals("")) {
								continue;
							}
						/*	if (warehouse == 0) {
								// 武汉
								for (String barCode : barCodes) {
									if(child.getProductBarCode().trim() != null && child.getProductBarCode().trim().toUpperCase().equals(map.get(barCode).toString().toUpperCase().trim())){
										String endCodeStr = barCode.substring(barCode.length()-1, barCode.length());
										child.setPnumber(new BigDecimal(map.get("pnumber_"+endCodeStr).toString()));

										String saveSql = "update order_product set"
												+ " pnumber='"+child.getPnumber()+"'"
												+ " where id='"+child.getId()+"'"
												+"  and product_bar_code='"+child.getProductBarCode()+"'";
										saveSqls.add(saveSql);
									}

								}
							} else if (warehouse == 1) {
								// 北京
								if(child.getProductBarCode().trim() != null && child.getProductBarCode().trim().equals(map.get("productBarCode").toString().trim())){
									child.setPnumber(new BigDecimal(map.get("pnumber").toString()));
									String saveSql = "update order_product set"
											+ " pnumber='"+child.getPnumber()+"'"
											+ " where id='"+child.getId()+"'"
											+"  and product_bar_code='"+child.getProductBarCode()+"'";
									saveSqls.add(saveSql);
								}


							}*/
							if(child.getProductBarCode().trim() != null && child.getProductBarCode().trim().equals(map.get("productBarCode").toString().trim())){
								child.setPnumber(new BigDecimal(map.get("pnumber").toString()));
								String saveSql = "update order_product set"
										+ " pnumber='"+child.getPnumber()+"'"
										+ " where id='"+child.getId()+"'"
										+"  and product_bar_code='"+child.getProductBarCode()+"'";
								saveSqls.add(saveSql);
							}

						}

						db.setCostRatio( calculateCostRatio(db));
						db.setIsOverCost(isOverCost(db)?0:1);
						//if(db.getExpressCode() != null && !db.getExpressCode().equals("")){
						StringBuffer saveSql = new StringBuffer("update order_info set");
						saveSql.append(" express_state ="+db.getExpressState());
						saveSql.append(" ,order_state="+db.getOrderState());
						saveSql.append(" ,incomline_time='"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(db.getIncomlineTime())+"'");
						saveSql.append(" ,deliver_date='"+new SimpleDateFormat("yyyy-MM-dd").format(db.getDeliverDate())+"'");
						saveSql.append(" ,order_date='"+new SimpleDateFormat("yyyy-MM-dd").format(db.getOrderDate())+"'");
						if(db.getAdvertChannel()!=null && !db.getAdvertChannel().equals("")){
							saveSql.append(" ,advert_channel='"+db.getAdvertChannel()+"'");
						}
						if(db.getOrderNature() != null && !db.getOrderNature().equals("")){
							saveSql.append(" ,order_nature='"+db.getOrderNature()+"'");
						}
						if(db.getDepositAmout() != null && db.getDepositAmout().compareTo(new BigDecimal(0.00))>=0){
							saveSql.append(" ,deposit_amout='"+db.getDepositAmout()+"'");
						}else if(db.getDepositAmout() == null){
							saveSql.append(" ,deposit_amout='0.00'");
						}
						if(db.getCollectionAmout() != null && db.getCollectionAmout().compareTo(new BigDecimal(0.00))>=0){
							saveSql.append(" ,collection_amout='"+db.getCollectionAmout()+"'");
						}else if(db.getCollectionAmout() == null){
							saveSql.append(" ,collection_amout='0.00'");
						}
						if(db.getTotalAmount() != null && db.getTotalAmount().compareTo(new BigDecimal(0.00))>=0){
							saveSql.append(" ,total_amount='"+db.getTotalAmount()+"'");
						}else if(db.getTotalAmount() == null){
							saveSql.append(" ,total_amount='0.00'");
						}
						if(db.getCostAmount() != null && db.getCostAmount().compareTo(new BigDecimal(0.00))>=0){
							saveSql.append(" ,cost_amount='"+db.getCostAmount()+"'");
						}else if(db.getCostAmount() == null){
							saveSql.append(" ,cost_amount='0.00'");
						}
						if(db.getExpressCode() != null && !db.getExpressCode().equals("")){
							saveSql.append(" ,express_code='"+db.getExpressCode()+"'");
						}else{
							saveSql.append(" ,express_code=null");
						}
						saveSql.append(",express_company="+db.getExpressCompany());
						if(db.getCostRatio() != null && db.getCostRatio().compareTo(new BigDecimal(0.00))>=0){
							saveSql.append(" ,cost_ratio='"+db.getCostRatio()+"'");
						}else if(db.getCostRatio() == null){
							saveSql.append(" ,cost_ratio='0.00'");
						}
						saveSql.append(",is_over_cost="+db.getIsOverCost());
						saveSql.append(" where id='"+db.getId()+"'");
						saveSqls.add(saveSql.toString());
							/*String saveSql = "update order_info set"
									+ " express_state ="+db.getExpressState()
									+" ,order_state="+db.getOrderState()
									+" ,incomline_time='"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(db.getIncomlineTime())+"'"
									+" ,deliver_date='"+new SimpleDateFormat("yyyy-MM-dd").format(db.getDeliverDate())+"'"
									+" ,order_date='"+new SimpleDateFormat("yyyy-MM-dd").format(db.getOrderDate())+"'"
									+" ,advert_channel='"+db.getAdvertChannel()+"'"
									+" ,order_nature='"+db.getOrderNature()+"'"
									+" ,deposit_amout='"+db.getDepositAmout()+"'"
									+" ,collection_amout='"+db.getCollectionAmout()+"'"
									+" ,total_amount='"+db.getTotalAmount()+"'"
									+" ,cost_amount='"+db.getCostAmount()+"'"
									+" ,express_code='"+db.getExpressCode()+"'"
									+",express_company="+db.getExpressCompany()
									+",cost_ratio='"+db.getCostRatio()+"'"
									+",is_over_cost="+db.getIsOverCost()
									+" where id='"+db.getId()+"'";
									saveSqls.add(saveSql);			*/
						//}
						set.add(db.getOrderCode()+"-"+db.getReceiverName());
					}
				}


			}
			if(saveSqls == null || saveSqls.isEmpty()){
				return;
			}
			jdbcTemplate.batchUpdate(saveSqls.toArray(new String[saveSqls.size()]));
		}


	}

	@Override
	@Transactional
	public void importOrderExpressExcel(MultipartFile file) throws Exception {
		String templateName = "订单快递状态更新模板.xlsx";
		String sheetName = "sheet1";
		List<Map<String, Object>> list = PoiHelper.importExcel(templateName, file, sheetName, 2);
		if (list == null || list.isEmpty()) {
			return;
		}
		List<String> saveSqls= new ArrayList<>();
		for (Map<String, Object> map : list) {
			if (map == null) {
				continue;
			}
			if(!map.containsKey("orderCode") || map.get("orderCode") == null || map.get("orderCode").equals("")){
				continue;
			}
			if ((!map.containsKey("expressCode") || map.get("expressCode") == null || map.get("expressCode").equals(""))
					&& (!map.containsKey("expressState") || map.get("expressState") == null
					|| map.get("expressState").equals(""))) {
				continue;
			}
			StringBuffer sql = new StringBuffer("update order_info set");
			if(map.containsKey("expressCode") && map.get("expressCode") != null && !map.get("expressCode").equals("")){
				if(map.containsKey("expressState") && map.get("expressState") != null && !map.get("expressState").equals("")){
					sql.append(" express_code = '"+map.get("expressCode")+"',");
				}else{
					sql.append(" express_code = '"+map.get("expressCode")+"'");
				}

			}
			if(map.containsKey("expressState") && map.get("expressState") != null && !map.get("expressState").equals("")){
				String expressStatestr =  (String) map.get("expressState");
				if(expressStatestr.trim().equals("未发货")){
					sql.append(" express_state = 0");
				}else if(expressStatestr.trim().equals("已发货")){
					sql.append(" express_state = 1,");
					sql.append(" order_state = 2");
				}else if(expressStatestr.trim().equals("取消发货")){
					sql.append(" express_state = 2,");
					sql.append(" order_state = 3");
				}else if(expressStatestr.trim().equals("未妥投")){
					sql.append(" express_state = 3");
				}else if(expressStatestr.trim().equals("退回")){
					sql.append(" express_state = 4,");
					sql.append(" order_state = 3");
				}else if(expressStatestr.trim().equals("签收")){
					sql.append(" express_state = 5,");
					sql.append(" order_state = 3");
				}

			}
			sql.append(" where order_code='"+map.get("orderCode")+"'");
			saveSqls.add(sql.toString());
		}

		if (saveSqls == null || saveSqls.isEmpty()) {
			return;
		}
		jdbcTemplate.batchUpdate(saveSqls.toArray(new String[saveSqls.size()]));


	}

	public ResponseEntity<byte[]> exportOrderExcel(Map<String,Object> map) throws Exception{

		//查询数据
		StringBuilder sql = queryList( map);
		sql.append(getOrderBySQL(" order by order_code,deliver_date"));
		List<OrderVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<OrderVO>(OrderVO.class));
		if(list == null || list.size()<=0){
			throw new Exception("没有可以导出的数据!");
		}
		queryOrderProducts(list);
		/*List<Map<String,Object>> wuhanOrders =new ArrayList<>();
		List<Map<String,Object>> beijingOrders =new ArrayList<>();*/
		List<WarehouseVO> warehouses = warehouseService.queryList(null, null).getContent();
		List<Map<String,Object>> orders =new ArrayList<>();
		for(OrderVO order:list){
			Map<String,Object> orderMap = BeanHelper.beanToMap(order);
			orderMap.remove("childrenDetail");
			if(order.getOrderState()==0){
				orderMap.put("orderSate", "编辑中");
			}else if(order.getOrderState()==0){
				orderMap.put("orderSate", "已锁定");
			}else if(order.getOrderState()==2){
				orderMap.put("orderSate", "已发快递");
			}else if(order.getOrderState()==3){
				orderMap.put("orderSate", "成单");
			}

			if(order.getIsForeignExpress()==0){
				orderMap.put("isForeignExpress", "否");
			}else{
				orderMap.put("isForeignExpress", "是");
			}
			if(order.getIsOverCost()==0){
				orderMap.put("isOverCost", "否");
			}else {
				orderMap.put("isOverCost", "是");
			}
			if(order.getExpressCompany()==0){
				orderMap.put("expressCompany", "顺丰");
			}else if(order.getExpressCompany()==1){
				orderMap.put("expressCompany", "邮政");
			}else if(order.getExpressCompany()==2){
				orderMap.put("expressCompany", "圆通");
			}else if(order.getExpressCompany()==3){
				orderMap.put("expressCompany", "中通");
			}else if(order.getExpressCompany()==4){
				orderMap.put("expressCompany", "德邦");
			}else if(order.getExpressCompany()==5){
				orderMap.put("expressCompany", "联邦");
			}
			if(order.getExpressState()==0){
				orderMap.put("expressState", "未发货");
			}else if(order.getExpressState()==1){
				orderMap.put("expressState", "已发货");
			}else if(order.getExpressState()==2){
				orderMap.put("expressState", "取消发货");
			}else if(order.getExpressState()==3){
				orderMap.put("expressState", "未妥投");
			}else if(order.getExpressState()==4){
				orderMap.put("expressState", "退回");
			}else if(order.getExpressState()==5){
				orderMap.put("expressState", "签收");
			}
			/*
			 * if(order.getWarehouse()==0){ orderMap.put("mailNumber", 1);
			 * orderMap.put("mailGoods", "食品"+order.getOrderCode()); }else
			 * if(order.getWarehouse()==1){ orderMap.put("receiverName_1",
			 * order.getReceiverName()); if(order.getExpressCompany()==0){
			 * orderMap.put("sellerMemo", "KD[顺丰陆运]"); }else
			 * if(order.getExpressCompany()==1){ if(order.getIsForeignExpress()==1){
			 * orderMap.put("sellerMemo", "KD[EMS国际]"); }else{ orderMap.put("sellerMemo",
			 * "KD[EMS快递包裹]"); } } if(order.getCollectionAmout().intValue()==0){
			 * orderMap.put("payTime", order.getCreateTime()); if(order.getOrderState()<2){
			 * orderMap.remove("orderSate"); orderMap.put("orderSate", "买家已付款，等待卖家发货"); }
			 *
			 * }else if (order.getCollectionAmout().intValue()!=0){
			 * if(order.getOrderState()<2){ orderMap.remove("orderSate");
			 * orderMap.put("orderSate", "货到付款"); } } }
			 */

			List<OrderProductVO>  details = order.getChildrenDetail();
			if(details != null && details.size()>0){
				for(int i=0 ;i<details.size();i++){
					if(details.get(i).getProductBarCode() == null || details.get(i).getProductBarCode().equals("")){
						continue;
					}
					String productBarCode = details.get(i).getProductBarCode().toUpperCase();
					BigDecimal pnumber = details.get(i).getPnumber();
					/*if(order.getWarehouse()==0){
						orderMap.put("productBarCode_"+productBarCode,productBarCode);
						orderMap.put("pnumber_"+productBarCode,pnumber);
					}else if(order.getWarehouse()==1){
						Map<String,Object> newOrderMap =new HashMap<>();
						newOrderMap.putAll(orderMap);
						newOrderMap.put("warehouse", "北京");
						newOrderMap.put("orderCode_child",order.getOrderCode());
						newOrderMap.put("productBarCode",productBarCode);
						newOrderMap.put("pnumber",pnumber);
						newOrderMap.put("productName",details.get(i).getProductName());
						newOrderMap.put("productCostPrice",details.get(i).getProductCostPrice());
						newOrderMap.put("productTotalPrice",details.get(i).getProductCostPrice().multiply(pnumber));
						newOrderMap.put("productTotalAmount",details.get(i).getProductCostPrice().multiply(pnumber));
						beijingOrders.add(newOrderMap);
					}*/

					Map<String,Object> newOrderMap =new HashMap<>();
					newOrderMap.putAll(orderMap);
					if(warehouses!=null){
						for(WarehouseVO warehouse:warehouses) {
							if(warehouse.getCode().equals(order.getWarehouse())){
								newOrderMap.put("warehouse", warehouse.getName());
								break;
							}
						}
					}
					/*
					 * if(order.getWarehouse()==0){ newOrderMap.put("warehouse", "广西"); }
					 * if(order.getWarehouse()==1){ newOrderMap.put("warehouse", "北京"); }
					 * if(order.getWarehouse()==2){ newOrderMap.put("warehouse", "武汉2"); }
					 */
					//newOrderMap.put("warehouse", "北京");
					newOrderMap.put("orderCode_child",order.getOrderCode());
					newOrderMap.put("productBarCode",productBarCode);
					newOrderMap.put("pnumber",pnumber);
					newOrderMap.put("productName",details.get(i).getProductName());
					newOrderMap.put("productCostPrice",details.get(i).getProductCostPrice());
					if(details.get(i).getProductCostPrice()==null){
						System.out.println("price is null----"+i);
					}
					if(pnumber ==null){
						System.out.println("pnumber is null-----"+i);
					}
					newOrderMap.put("productTotalPrice",details.get(i).getProductCostPrice().multiply(pnumber));
					newOrderMap.put("productTotalAmount",details.get(i).getProductCostPrice().multiply(pnumber));
					orders.add(newOrderMap);

				}
				/*if((int)orderMap.get("warehouse")==0){
					orderMap.put("warehouse", "武汉");
					wuhanOrders.add(orderMap);
				}*/


			}

		}

		String deliverDateStr="";
		String deliverBeginDateStr = "";
		String deliverEndDateStr ="";

		if(map !=null){
			if(map.containsKey("deliverBeginDate") ){

				if(map.get("deliverBeginDate") instanceof  Date){
					deliverBeginDateStr = new SimpleDateFormat("yyyy-MM-dd").format((Date) map.get("deliverBeginDate"));
				}else if(map.get("deliverBeginDate") instanceof  String){
					deliverBeginDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd").parse((String)map.get("deliverBeginDate")));
				}


			}
			if(map.containsKey("deliverEndDate")){

				if(map.get("deliverEndDate") instanceof  Date){
					deliverEndDateStr = new SimpleDateFormat("yyyy-MM-dd").format((Date) map.get("deliverEndDate"));
				}else if(map.get("deliverEndDate") instanceof  String){
					deliverEndDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd").parse((String)map.get("deliverEndDate")));
				}

			}
			if (map.containsKey("deliverDate")) {

				if(map.get("deliverDate") instanceof  Date){
					deliverDateStr = new SimpleDateFormat("yyyy-MM-dd").format((Date) map.get("deliverDate"));
				}else if(map.get("deliverDate") instanceof  String){
					deliverDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd").parse((String)map.get("deliverDate")));
				}


			}

			if(deliverBeginDateStr != null && !deliverBeginDateStr.equals("") && (deliverEndDateStr==null || deliverEndDateStr.equals(""))){
				deliverDateStr = deliverBeginDateStr+"之后";
			}
			if((deliverBeginDateStr == null || deliverBeginDateStr.equals("") )&& (deliverEndDateStr!=null && !deliverEndDateStr.equals(""))){
				deliverDateStr = deliverEndDateStr+"之前";
			}
			if((deliverBeginDateStr != null && !deliverBeginDateStr.equals("")) && (deliverEndDateStr!=null && !deliverEndDateStr.equals(""))){
				if(deliverBeginDateStr.equals(deliverEndDateStr)) {
					deliverDateStr =deliverBeginDateStr;
				}else {
					deliverDateStr =deliverBeginDateStr+"到"+deliverEndDateStr;
				}

			}
		}
		if(deliverDateStr == null || deliverDateStr.equals("")){
			deliverDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		String templateName =null;
		String exportFileName = null;
		List<PoiHelper.ExportDataObject> exportList = new ArrayList<>();
		/*if((wuhanOrders !=null && wuhanOrders.size()>0) && (beijingOrders == null || beijingOrders.size()<=0)){
			templateName="S91平台发货表模板.xls";
			exportFileName = "S91平台-"+deliverDateStr+"-发货表.xls";
			//只有武汉的订单数据
			PoiHelper.ExportDataObject export = new PoiHelper().new ExportDataObject();
			export.setSheetName("发货表");
			export.setSheetNumber(1);
			export.setStartRow(2);
			export.setExportData(wuhanOrders);
			exportList.add(export);
		}
		if((wuhanOrders ==null || wuhanOrders.size()<=0) && (beijingOrders != null && beijingOrders.size()>0)){
			templateName="52部发货表模板.xls";
			exportFileName = "52部-"+deliverDateStr+"-发货表.xls";
			//只有北京的订单数据
			PoiHelper.ExportDataObject export = new PoiHelper().new ExportDataObject();
			export.setSheetName("Sheet2");
			export.setSheetNumber(1);
			export.setStartRow(2);
			export.setExportData(beijingOrders);
			exportList.add(export);
		}
		if((wuhanOrders !=null && wuhanOrders.size()>0) && (beijingOrders != null &&  beijingOrders.size()>0)){
			templateName = "S91平台+52部发货表模板.xls";
			exportFileName = "S91平台+52部-"+deliverDateStr+"-发货表.xls";
			//武汉和北京的订单数据
			PoiHelper.ExportDataObject wuhanExport = new PoiHelper().new ExportDataObject();
			wuhanExport.setSheetName("武汉-S91平台");
			wuhanExport.setSheetNumber(1);
			wuhanExport.setStartRow(2);
			wuhanExport.setExportData(wuhanOrders);
			exportList.add(wuhanExport);

			PoiHelper.ExportDataObject beijingExport = new PoiHelper().new ExportDataObject();
			beijingExport.setSheetName("北京-52部");
			beijingExport.setSheetNumber(2);
			beijingExport.setStartRow(2);
			beijingExport.setExportData(beijingOrders);
			exportList.add(beijingExport);
		}*/
		if(orders !=null && orders.size()>0){
			templateName = "订单数据模板.xlsx";
			exportFileName = deliverDateStr+"订单发货表.xlsx";
			//武汉和北京的订单数据
			PoiHelper.ExportDataObject export = new PoiHelper().new ExportDataObject();

			export.setSheetNumber(1);
			export.setStartRow(2);
			export.setExportData(orders);
			exportList.add(export);


		}
		return PoiHelper.exportExcel(templateName,exportFileName,exportList);
	}



	private  StringBuilder queryList(Map<String,Object> map) throws Exception{
		StringBuilder sql = new StringBuilder();
		if(!map.containsKey("outAttributeNames")){
			sql.append("Select * from order_info where 1=1");
		}else{
			String outAttributeNames = (String) map.get("outAttributeNames");
			sql.append("Select "+outAttributeNames+" from order_info where 1=1");
		}
		UserVO userVO = getCurrentUser();
		//String roleCode = getCurrentUserRoleCode();
		/*String roleCode = "001";*/
		if (map != null) {
			if (map.containsKey("optionType")) {
				int optionType = (int) map.get("optionType");
				if(map.get("optionType") instanceof  Integer){
					optionType = (int) map.get("optionType");
				}else if(map.get("optionType") instanceof  String){
					optionType = Integer.parseInt((String) map.get("optionType"));
				}else{
					throw new Exception("参数optionType数据类型不正确!");
				}

				/*if (optionType == 1 && !"004".equals(roleCode)) {*/
				if(userVO != null && userVO.getRoleLevel()<=3){
					// 管理员或二级管理员或3级管理员当天10点以后确认当天订单
					sql.append(" and date(deliver_date) =curdate()");
				}
			}
			if (map.containsKey("keyWords")) {
				String keyWords = (String) map.get("keyWords");
				if (null != keyWords && !keyWords.equals("")) {
					sql.append(" and (region like '" + keyWords + "%'" + " or user_name like '" + keyWords + "%'"
							+ " or order_code like '" + keyWords + "%'" + " or receiver_name like '" + keyWords + "%'"
							+ " or receiver_phone like '" + keyWords + "%'" + " or express_code like '" + keyWords
							+ "%'" + " or receiver_phone like '" + keyWords + "%'" + ")");
				}
			}
			if (map.containsKey("orderNature")) {
				String orderNature = (String) map.get("orderNature");
				if (StringUtils.isNoneBlank(orderNature)) {
					sql.append(" and order_nature like '" + orderNature + "%'");
				}
			}
			if (map.containsKey("orderDate")) {
				String orderDate = null;
				if(map.get("orderDate") instanceof  Date){
					orderDate = new SimpleDateFormat("yyyy-MM-dd").format((Date) map.get("orderDate"));
				}else if(map.get("orderDate") instanceof  String){
					String orderDateStr = (String)map.get("orderDate");
					if(orderDateStr != null && !orderDateStr.equals("")){
						orderDate = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd").parse(orderDateStr));
					}else{
						throw new Exception("参数orderDate传入的是空值!");
					}
				}
				if(orderDate !=null){
					sql.append(" and order_date='" + orderDate + "'");
				}

			}
			if(map.containsKey("deliverBeginDate") ){
				String deliverBeginDate = null;
				if(map.get("deliverBeginDate") instanceof  Date){
					deliverBeginDate = new SimpleDateFormat("yyyy-MM-dd").format((Date) map.get("deliverBeginDate"));
				}else if(map.get("deliverBeginDate") instanceof  String){
					String deliverBeginDateStr = (String)map.get("deliverBeginDate");
					if(deliverBeginDateStr !=null && !deliverBeginDateStr.equals("")){
						deliverBeginDate = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd").parse(deliverBeginDateStr));
					}else{
						throw new Exception("参数deliverBeginDate传入的是空值!");
					}
				}
				if(deliverBeginDate != null){
					sql.append(" and deliver_date >= '"+deliverBeginDate+"'");

				}
			}
			if(map.containsKey("deliverEndDate")){
				String deliverEndDate = null;
				if(map.get("deliverEndDate") instanceof  Date){
					deliverEndDate = new SimpleDateFormat("yyyy-MM-dd").format((Date) map.get("deliverEndDate"));
				}else if(map.get("deliverEndDate") instanceof  String){
					String deliverEndDateStr = (String)map.get("deliverEndDate");
					if(deliverEndDateStr !=null && !deliverEndDateStr.equals("")){
						deliverEndDate = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd").parse(deliverEndDateStr));
					}else{
						throw new Exception("参数deliverEndDate传入的是空值!");
					}
				}
				if(deliverEndDate !=null){
					sql.append(" and deliver_date <= '"+deliverEndDate+"'");
				}
			}
			if (map.containsKey("deliverDate")) {
				String deliverDate = null;
				if(map.get("deliverDate") instanceof  Date){
					deliverDate = new SimpleDateFormat("yyyy-MM-dd").format((Date) map.get("deliverDate"));
				}else if(map.get("deliverDate") instanceof  String){
					String deliverDateStr = (String)map.get("deliverDate");
					if(deliverDateStr != null && !deliverDateStr.equals("")){
						deliverDate = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd").parse(deliverDateStr));
					}else {
						throw new Exception("参数deliverDate传入的是空值!");
					}
				}
				if(deliverDate !=null){
					sql.append(" and deliver_date='" + deliverDate + "'");
				}

			}
			if (map.containsKey("warehouse")) {
				int warehouse=0;
				if( map.get("warehouse") instanceof Integer){
					warehouse =  (int)map.get("warehouse");
					sql.append(" and warehouse=" + warehouse);
				}else if( map.get("warehouse") instanceof String){
					warehouse =  Integer.parseInt((String)map.get("warehouse"));
					sql.append(" and warehouse=" + warehouse);
				}else{
					throw new Exception("参数warehouse数据类型不正确!");
				}

			}
			if (map.containsKey("orderState")) {
				int orderState=0;
				if( map.get("orderState") instanceof Integer){
					orderState =  (int)map.get("orderState");
					sql.append(" and warehouse=" + orderState);
				}else if( map.get("orderState") instanceof String){
					orderState =  Integer.parseInt((String)map.get("orderState"));
					sql.append(" and order_state=" + orderState);
				}else{
					throw new Exception("参数orderState数据类型不正确!");
				}


			}
			if (map.containsKey("isOverCost")) {
				int isOverCost  =0;
				if( map.get("isOverCost") instanceof Integer){
					isOverCost =  (int)map.get("isOverCost");
					sql.append(" and is_over_cost=" + isOverCost);
				}else if( map.get("isOverCost") instanceof String){
					isOverCost =  Integer.parseInt((String)map.get("isOverCost"));
					sql.append(" and is_over_cost=" + isOverCost);
				}else{
					throw new Exception("参数isOverCost数据类型不正确!");
				}


			}
			if (map.containsKey("expressState")) {
				int expressState=0;
				if( map.get("expressState") instanceof Integer){
					expressState =  (int)map.get("expressState");
					sql.append(" and express_state=" + expressState);
				}else if( map.get("expressState") instanceof String){
					expressState =  Integer.parseInt((String)map.get("expressState"));
					sql.append(" and express_state=" + expressState);
				}else{
					throw new Exception("参数expressState数据类型不正确!");
				}


			}

		}
		if(userVO != null){
			if(userVO.getRoleLevel()==4){

				String userId = (String) ((UserVO) SecurityUtils.getSubject().getSession().getAttribute("currentUser"))
						.getId();
				sql.append(" and user_id = '" + userId + "'");
				sql.append(" and warehouse in (select code from warehouse_info where biz_range="+userVO.getRoleBizRange()+")");
				// 业务员,今天之前的订单只能查看发货日期自己近两月的 (已注释)
				/*sql.append(
						" and (deliver_date between date_sub(curdate(),interval 2 month) and (select max(deliver_date) from order_info))");*/
			}else if(userVO.getRoleLevel()==2){
				//二级管理员
				//sql.append(" and user_id in (select us.id from user_info us left join role_info ro on us.role_id=ro.id where ro.biz_range="+userVO.getRoleBizRange()+")");
				sql.append(" and warehouse in (select code from warehouse_info where biz_range="+userVO.getRoleBizRange()+")");
			}else if(userVO.getRoleLevel()==3) {
				//三级管理员
				//sql.append(" and user_id in (select us.id from user_info us left join role_info ro on us.role_id=ro.id where ro.biz_range="+userVO.getRoleBizRange()+")");
				sql.append(" and warehouse in (select code from warehouse_info where biz_range="+userVO.getRoleBizRange()+")");
				sql.append(" and region like '"+userVO.getRegion().trim()+"%'");
			}
		}

		return sql;
	}

	private String getOrderBySQL(String orderby){
		/*String orderby = " order by deliver_date desc,region asc,warehouse desc,express_company asc,is_foreign_express asc";*/
		if(orderby == null || orderby.equals("")){
			orderby = " order by create_time desc,deliver_date desc";
		}
		return orderby;
	}

	@Override
	public ResponseEntity<byte[]> exportTemplateExcel(int type) throws Exception {
		String templateName=null;
		switch (type) {
			case 0:
				templateName ="S91平台发货表模板.xls";
				break;
			case 1:
				templateName ="52部发货表模板.xls";
				break;
			case 2:
				templateName ="订单快递状态更新模板.xlsx";
				break;
			default:
				templateName ="订单快递状态更新模板.xlsx";
				break;
		}

		return PoiHelper.exportTemplateExcel(templateName);
	}

	@Override
	public List<Map<String,Object>> multiPurchaseOrder(Map<String, Object> map) throws Exception {
		if(!map.containsKey("orderNature")|| map.get("orderNature")==null || map.get("orderNature").equals("")){
			throw new Exception("订单性质不能为空！");
		}
		if (!map.containsKey("deliverBeginDate") || map.get("deliverBeginDate") == null) {
			throw new Exception("发货开始时间不能为空！");
		}
		if (!map.containsKey("deliverEndDate") || map.get("deliverEndDate") == null) {
			throw new Exception("发货结束时间不能为空！");
		}
		/*Date deliverMonth =null;
		if(map.containsKey("deliverMonth")){
			if(map.get("deliverMonth") instanceof  Date){
				deliverMonth = new SimpleDateFormat("yyyy-MM").parse(new SimpleDateFormat("yyyy-MM").format((Date) map.get("deliverMonth")));
			}else if(map.get("deliverMonth") instanceof  String){
				deliverMonth =new SimpleDateFormat("yyyy-MM").parse((String)map.get("deliverMonth"));
			}

		}
		if(deliverMonth !=null){
			Date deliverBeginDate = DateHelper.getFirstDayOfMonth(deliverMonth);
			Date deliverEndDate = DateHelper.getLastDayOfMonth(deliverMonth);
			map.put("deliverBeginDate", deliverBeginDate);
			map.put("deliverEndDate", deliverEndDate);
			map.remove("deliverMonth");
		}*/
		String outAttributeNames = " region,user_name,receiver_name,sum(total_amount) total_amount ";
		map.put("outAttributeNames", outAttributeNames);
		StringBuilder sql = queryList(map);
		sql.append(" and receiver_name is not null ");
		sql.append(" group by receiver_name ");
		sql.append(getOrderBySQL(" order by region"));
		List<OrderVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<OrderVO>(OrderVO.class));
		if(list == null || list.size()<=0){
			return null;
		}
		List<Map<String,Object>> exportDataList = new ArrayList<>();
		Map<String,Object> data = new HashMap<>();
		//data.put("title",new SimpleDateFormat("yyyy年MM月").format(deliverMonth)+map.get("orderNature")+"的客户档案");
		String deliverBeginDate = null;
		if (map.get("deliverBeginDate") instanceof Date) {
			deliverBeginDate = DateFormatUtils.format((Date) map.get("deliverBeginDate"), "yyyyMMdd");
		} else if (map.get("deliverEndDate") instanceof String) {
			deliverBeginDate = DateFormatUtils.format(
					(DateUtils.parseDate((String) map.get("deliverBeginDate"), new String[] { "yyyy-MM-dd" })),
					"yyyyMMdd");
		}
		String deliverEndDate = null;
		if (map.get("deliverEndDate") instanceof Date) {
			deliverEndDate = DateFormatUtils.format((Date) map.get("deliverEndDate"), "yyyyMMdd");
		} else if (map.get("deliverEndDate") instanceof String) {
			deliverEndDate = DateFormatUtils.format(
					(DateUtils.parseDate((String) map.get("deliverEndDate"), new String[] { "yyyy-MM-dd" })),
					"yyyyMMdd");
		}
		data.put("title",deliverBeginDate + "-" + deliverEndDate +map.get("orderNature")+"的客户档案");
		exportDataList.add(data);
		Map<String,Object> data1 = new HashMap<>();
		data1.put("orderNature",map.get("orderNature")+"购买总金额");
		exportDataList.add(data1);
		for(int i=0;i<list.size();i++){
			if(list.get(i) == null ){
				continue;
			}
			Map<String,Object> data2 = new HashMap<>();
			data2.put("region", list.get(i).getRegion());
			data2.put("userName", list.get(i).getUserName());
			data2.put("receiverName", list.get(i).getReceiverName());
			data2.put("totalAmount", list.get(i).getTotalAmount());
			exportDataList.add(data2);
		}
		return exportDataList;
	}

	@Override
	public ResponseEntity<byte[]> exportMultiPurchase(Map<String, Object> map) throws Exception {
		List<Map<String,Object>> exportDataList = multiPurchaseOrder(map);
		if(exportDataList == null || exportDataList.isEmpty()){
			throw new Exception("没有数据!");
		}
		String templateName="时间段内复购客户档案模板.xlsx";
		String exportFileName= "";
		for(Map<String,Object> map1: exportDataList){
			if(map1.containsKey("title")){
				exportFileName=map1.get("title").toString().trim()+".xlsx";
				break;
			}
		}
		List<PoiHelper.ExportDataObject> exportData = new ArrayList<>();
		PoiHelper.ExportDataObject dataObj = new PoiHelper().new ExportDataObject();
		dataObj.setExportData(exportDataList);
		dataObj.setSheetName("sheet1");
		dataObj.setSheetNumber(1);
		dataObj.setStartRow(1);
		exportData.add(dataObj);
		return PoiHelper.exportExcel(templateName, exportFileName, exportData);
	}

	/**
	 * 查询一段时间订单数据
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private List<OrderVO> queryregionOrdersDatas(Map<String, Object> map) throws Exception{
		if (!map.containsKey("deliverBeginDate") || map.get("deliverBeginDate") == null) {
			throw new Exception("发货开始时间不能为空！");
		}
		if (!map.containsKey("deliverEndDate") || map.get("deliverEndDate") == null) {
			throw new Exception("发货结束时间不能为空！");
		}

		String outAttributeNames = " region,order_nature,count(id) order_state,sum(deposit_amout) deposit_amout,express_state,"
				+ " sum(collection_amout) collection_amout,sum(total_amount) total_amount,sum(cost_amount) cost_amount ";
		map.put("outAttributeNames", outAttributeNames);
		StringBuilder sql = queryList(map);
		sql.append(" and region is not null ");
		sql.append(" and order_nature is not null ");
		sql.append(" group by region,order_nature,express_state ");
		sql.append(getOrderBySQL(" order by order_code,deliver_date"));
		List<OrderVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<OrderVO>(OrderVO.class));
		if (list == null || list.size() <= 0) {
			return null;
		}
		return list;
	}

	/**
	 * 重新整理各区域订单数据
	 * @param list
	 * @param exportDataList
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> RecombinRegionOrdersData(List<OrderVO> list,List<Map<String, Object>> exportDataList) throws Exception {

		/// 按区域分组
		Map<String, Object> data2 = new HashMap<>();
		data2.put("region", "总计");
		data2.put("orderNature", "");
		// 求和
		int sumOrderNum = list.stream().mapToInt(OrderVO::getOrderState).sum();
		data2.put("orderNum", sumOrderNum);
		data2.put("depositAmout",
				list.stream().map(p -> p.getDepositAmout() != null ? p.getDepositAmout() : new BigDecimal(0.00))
						.reduce(new BigDecimal(0.00), BigDecimal::add));
		data2.put("collectionAmout",
				list.stream().map(p -> p.getCollectionAmout() != null ? p.getCollectionAmout() : new BigDecimal(0.00))
						.reduce(new BigDecimal(0.00), BigDecimal::add));
		data2.put("totalAmount",
				list.stream().map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
						.reduce(new BigDecimal(0.00), BigDecimal::add));
		data2.put("costAmount",
				list.stream().map(p -> p.getCostAmount() != null ? p.getCostAmount() : new BigDecimal(0.00))
						.reduce(new BigDecimal(0.00), BigDecimal::add));
		// 筛选已签收的数据
		List<OrderVO> sumSignList = list.stream().filter(a -> a.getExpressState() == 5).collect(Collectors.toList());
		int sumSignNum = sumSignList.stream().mapToInt(OrderVO::getOrderState).sum();
		NumberFormat percent = NumberFormat.getPercentInstance();
		percent.setMaximumFractionDigits(2);
		if (sumSignNum > 0) {
			data2.put("signNum", sumSignNum);
			data2.put("signAmount",
					sumSignList.stream()
							.map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
							.reduce(new BigDecimal(0.00), BigDecimal::add));
			BigDecimal singRate = new BigDecimal(sumSignNum).divide(new BigDecimal(sumOrderNum), 4,
					RoundingMode.HALF_UP);
			data2.put("signRate", percent.format(singRate.doubleValue()));
		} else {
			data2.put("signNum", 0);
			data2.put("signAmount", 0);
			data2.put("signRate", percent.format(0));
		}

		// 按区域分组
		Map<String, List<OrderVO>> sortMap = new LinkedHashMap<>();
		Map<String, List<OrderVO>> collect = list.stream().collect(Collectors.groupingBy(OrderVO::getRegion));
		collect.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.forEachOrdered(e -> sortMap.put(e.getKey(), e.getValue()));
		for (Map.Entry<String, List<OrderVO>> entry : sortMap.entrySet()) {

			if (entry.getValue() != null && !entry.getValue().isEmpty()) {
				List<OrderVO> tmpList = entry.getValue();
				Map<String, Object> data3 = new HashMap<>();
				data3.put("region", entry.getKey());
				data3.put("orderNature", "合计");
				// 求和
				int orderNum = tmpList.stream().mapToInt(OrderVO::getOrderState).sum();
				data3.put("orderNum", orderNum);
				data3.put("depositAmout",
						tmpList.stream()
								.map(p -> p.getDepositAmout() != null ? p.getDepositAmout() : new BigDecimal(0.00))
								.reduce(new BigDecimal(0.00), BigDecimal::add));
				data3.put("collectionAmout",
						tmpList.stream().map(
								p -> p.getCollectionAmout() != null ? p.getCollectionAmout() : new BigDecimal(0.00))
								.reduce(new BigDecimal(0.00), BigDecimal::add));
				data3.put("totalAmount",
						tmpList.stream()
								.map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
								.reduce(new BigDecimal(0.00), BigDecimal::add));
				data3.put("costAmount",
						tmpList.stream().map(p -> p.getCostAmount() != null ? p.getCostAmount() : new BigDecimal(0.00))
								.reduce(new BigDecimal(0.00), BigDecimal::add));
				// 筛选已签收的数据
				List<OrderVO> regionSignList = tmpList.stream().filter(a -> a.getExpressState() == 5)
						.collect(Collectors.toList());
				int regionSignNum = regionSignList.stream().mapToInt(OrderVO::getOrderState).sum();
				if (regionSignNum > 0) {
					data3.put("signNum", regionSignNum);
					data3.put("signAmount",
							regionSignList.stream()
									.map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
									.reduce(new BigDecimal(0.00), BigDecimal::add));
					BigDecimal singRate = new BigDecimal(regionSignNum).divide(new BigDecimal(orderNum), 4,
							RoundingMode.HALF_UP);
					data3.put("signRate", percent.format(singRate.doubleValue()));
				} else {
					data3.put("signNum", 0);
					data3.put("signAmount", 0);
					data3.put("signRate", percent.format(0));
				}
				// 按订单性质分组
				Map<String, List<OrderVO>> partSortMap = new LinkedHashMap<>();
				Map<String, List<OrderVO>> partCollect = tmpList.stream()
						.collect(Collectors.groupingBy(OrderVO::getOrderNature));
				partCollect.entrySet().stream().sorted(Map.Entry.comparingByKey())
						.forEachOrdered(e -> partSortMap.put(e.getKey(), e.getValue()));
				for (Map.Entry<String, List<OrderVO>> partEntry : partSortMap.entrySet()) {
					if (partEntry.getValue() != null && !partEntry.getValue().isEmpty()) {
						List<OrderVO> partList = partEntry.getValue();
						if (partList != null && !partList.isEmpty()) {
							Map<String, Object> data4 = new HashMap<>();
							data4.put("region", partList.get(0).getRegion());
							data4.put("orderNature", partList.get(0).getOrderNature());
							int partOrderNum = partList.stream().mapToInt(OrderVO::getOrderState).sum();
							data4.put("orderNum", partOrderNum);
							data4.put("depositAmout", partList.stream()
									.map(p -> p.getDepositAmout() != null ? p.getDepositAmout() : new BigDecimal(0.00))
									.reduce(new BigDecimal(0.00), BigDecimal::add));
							data4.put("collectionAmout", partList.stream().map(
									p -> p.getCollectionAmout() != null ? p.getCollectionAmout() : new BigDecimal(0.00))
									.reduce(new BigDecimal(0.00), BigDecimal::add));
							data4.put("totalAmount",
									partList.stream().map(
											p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
											.reduce(new BigDecimal(0.00), BigDecimal::add));
							data4.put("costAmount",
									partList.stream().map(
											p -> p.getCostAmount() != null ? p.getCostAmount() : new BigDecimal(0.00))
											.reduce(new BigDecimal(0.00), BigDecimal::add));
							// 筛选已签收的数据
							List<OrderVO> partSignList = partList.stream().filter(a -> a.getExpressState() == 5)
									.collect(Collectors.toList());
							if (partSignList != null && !partList.isEmpty()) {
								int partSignNum = partSignList.stream().mapToInt(OrderVO::getOrderState).sum();
								if (partSignNum > 0) {
									data4.put("signNum", partSignNum);
									data4.put("signAmount", partSignList.stream().map(
											p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
											.reduce(new BigDecimal(0.00), BigDecimal::add));
									BigDecimal singRate = new BigDecimal(partSignNum)
											.divide(new BigDecimal(partOrderNum), 4, RoundingMode.HALF_UP);
									data4.put("signRate", percent.format(singRate.doubleValue()));
								} else {
									data4.put("signNum", 0);
									data4.put("signAmount", 0);
									data4.put("signRate", percent.format(0));
								}
							} else {
								data4.put("signNum", 0);
								data4.put("signAmount", 0);
								data4.put("signRate", percent.format(0));
							}
							exportDataList.add(data4);
						}

					}

				}

				exportDataList.add(data3);

			}

		}
		exportDataList.add(data2);

		return exportDataList;
	}

	@Override
	public List<Map<String, Object>> regionOrder (Map<String, Object> map) throws Exception{
		List<OrderVO> list = queryregionOrdersDatas(map);
		if(list == null || list.isEmpty()){
			return null;
		}
		List<Map<String, Object>> reDataList = new ArrayList<>();
		RecombinRegionOrdersData(list, reDataList);
		return reDataList;
	}

	@Override
	public ResponseEntity<byte[]> exportRegionOrder(Map<String, Object> map) throws Exception {

		List<OrderVO> list = queryregionOrdersDatas(map);
		if(list == null || list.isEmpty()){
			throw new Exception("没有数据!");
		}
		String deliverBeginDate = null;
		if (map.get("deliverBeginDate") instanceof Date) {
			deliverBeginDate = DateFormatUtils.format((Date) map.get("deliverBeginDate"), "yyyyMMdd");
		} else if (map.get("deliverEndDate") instanceof String) {
			deliverBeginDate = DateFormatUtils.format(
					(DateUtils.parseDate((String) map.get("deliverBeginDate"), new String[] { "yyyy-MM-dd" })),
					"yyyyMMdd");
		}
		String deliverEndDate = null;
		if (map.get("deliverEndDate") instanceof Date) {
			deliverEndDate = DateFormatUtils.format((Date) map.get("deliverEndDate"), "yyyyMMdd");
		} else if (map.get("deliverEndDate") instanceof String) {
			deliverEndDate = DateFormatUtils.format(
					(DateUtils.parseDate((String) map.get("deliverEndDate"), new String[] { "yyyy-MM-dd" })),
					"yyyyMMdd");
		}
		List<Map<String, Object>> exportDataList = new ArrayList<>();
		Map<String, Object> data = new HashMap<>();
		data.put("title", "时间段:" + deliverBeginDate + "-" + deliverEndDate + "的订单数据");
		exportDataList.add(data);
		Map<String, Object> data1 = new HashMap<>();
		exportDataList.add(data1);
		RecombinRegionOrdersData(list, exportDataList);
		List<Map<String, Object>> mergedCellList = new ArrayList<>();
		if(exportDataList != null && !exportDataList.isEmpty()){
			List<Map<String, Object>> exportDataList1 = new ArrayList<>();
			exportDataList1.addAll(exportDataList);
			//按区域分组，计数
			Map<String, Long> groupSortMap = new LinkedHashMap<>();
			Map<String, Long> groupMap = exportDataList1.stream().filter(p->p != null && p.get("region")!=null && !p.get("region").equals("总计")).collect(Collectors.groupingBy(p->p.get("region").toString(),Collectors.counting()));
			groupMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(e -> groupSortMap.put(e.getKey(), e.getValue()));
			int count=0;
			for (String key : groupSortMap.keySet()) {
				if(key==null){
					continue;
				}
				Map<String, Object> mergedCellMap = new HashMap<>();
				if (count == 0) {
					mergedCellMap.put("firstRow", 3);
					mergedCellMap.put("lastRow", 3+groupSortMap.get(key).intValue()-1);
					mergedCellMap.put("firstCol", 1);
					mergedCellMap.put("lastCol", 1);
					count = 3+groupSortMap.get(key).intValue();
				} else {
					mergedCellMap.put("firstRow", count );
					mergedCellMap.put("lastRow", count+groupSortMap.get(key).intValue()-1);
					mergedCellMap.put("firstCol", 1);
					mergedCellMap.put("lastCol", 1);
					count += groupSortMap.get(key).intValue();
				}
				mergedCellList.add(mergedCellMap);
			}
			Map<String, Object> mergedCellMap = new HashMap<>();
			mergedCellMap.put("firstRow", count );
			mergedCellMap.put("lastRow", count);
			mergedCellMap.put("firstCol", 1);
			mergedCellMap.put("lastCol", 1);
			mergedCellList.add(mergedCellMap);

		}
		String templateName = "时间段内各区域订单数据模板.xlsx";
		String exportFileName = deliverBeginDate + "-" + deliverEndDate + "的订单数据.xlsx";
		List<PoiHelper.ExportDataObject> exportData = new ArrayList<>();
		PoiHelper.ExportDataObject dataObj = new PoiHelper().new ExportDataObject();
		dataObj.setExportData(exportDataList);
		dataObj.setExportmergedCells(mergedCellList);
		dataObj.setSheetName("sheet1");
		dataObj.setSheetNumber(1);
		dataObj.setStartRow(1);
		exportData.add(dataObj);
		return PoiHelper.exportExcel(templateName, exportFileName, exportData);
	}

	@Override
	public Object countPeriodOrder(Map<String, Object> map) throws Exception {
		List<OrderVO> list = queryregionOrdersDatas(map);
		if(list ==null ||list.isEmpty()){
			return null;
		}
		List<Map<String, Object>> reDataList = new ArrayList<>();
		// 按区域分组
		Map<String, List<OrderVO>> sortMap = new LinkedHashMap<>();
		Map<String, List<OrderVO>> collect = list.stream().collect(Collectors.groupingBy(OrderVO::getRegion));
		collect.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.forEachOrdered(e -> sortMap.put(e.getKey(), e.getValue()));
		NumberFormat percent = NumberFormat.getPercentInstance();
		for (Map.Entry<String, List<OrderVO>> entry : sortMap.entrySet()) {
			if (entry.getValue() != null && !entry.getValue().isEmpty()) {
				List<OrderVO> tmpList = entry.getValue();
				Map<String, Object> data3 = new HashMap<>();
				data3.put("region", entry.getKey());
				// 求和
				int orderNum = tmpList.stream().mapToInt(OrderVO::getOrderState).sum();
				data3.put("orderNum", orderNum);
				data3.put("depositAmout",
						tmpList.stream()
								.map(p -> p.getDepositAmout() != null ? p.getDepositAmout() : new BigDecimal(0.00))
								.reduce(new BigDecimal(0.00), BigDecimal::add));
				data3.put("collectionAmout",
						tmpList.stream().map(
								p -> p.getCollectionAmout() != null ? p.getCollectionAmout() : new BigDecimal(0.00))
								.reduce(new BigDecimal(0.00), BigDecimal::add));
				data3.put("totalAmount",
						tmpList.stream()
								.map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
								.reduce(new BigDecimal(0.00), BigDecimal::add));
				data3.put("costAmount",
						tmpList.stream().map(p -> p.getCostAmount() != null ? p.getCostAmount() : new BigDecimal(0.00))
								.reduce(new BigDecimal(0.00), BigDecimal::add));
				// 筛选已签收的数据
				List<OrderVO> regionSignList = tmpList.stream().filter(a -> a.getExpressState() == 5)
						.collect(Collectors.toList());
				int regionSignNum = regionSignList.stream().mapToInt(OrderVO::getOrderState).sum();
				if (regionSignNum > 0) {
					data3.put("signNum", regionSignNum);
					data3.put("signAmount",
							regionSignList.stream()
									.map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
									.reduce(new BigDecimal(0.00), BigDecimal::add));
					BigDecimal singRate = new BigDecimal(regionSignNum).divide(new BigDecimal(orderNum), 4,
							RoundingMode.HALF_UP);
					data3.put("signRate", percent.format(singRate.doubleValue()));
				} else {
					data3.put("signNum", 0);
					data3.put("signAmount", 0);
					data3.put("signRate", percent.format(0));
				}
				reDataList.add(data3);

			}

		}
		return reDataList;
	}




	@Override
	public Object personalOrder(Map<String, Object> map) throws Exception {
		List<Map<String, Object>> reDatas = new ArrayList<>();
		if (!map.containsKey("deliverBeginDate") || map.get("deliverBeginDate") == null) {
			throw new Exception("发货开始时间不能为空！");
		}
		if (!map.containsKey("deliverEndDate") || map.get("deliverEndDate") == null) {
			throw new Exception("发货结束时间不能为空！");
		}

		String outAttributeNames = " order_nature,count(id) order_state,sum(deposit_amout) deposit_amout,express_state,"
				+ " sum(collection_amout) collection_amout,sum(total_amount) total_amount ";
		map.put("outAttributeNames", outAttributeNames);
		StringBuilder sql = queryList(map);
		//当前用户
		Subject subject = SecurityUtils.getSubject();
		UserVO user = (UserVO) subject.getSession().getAttribute("currentUser");
		if(map.containsKey("phone")){
			if(!user.getPhone().equals(map.get("phone").toString().trim()) && user.getRoleLevel()==4){
				throw new Exception("业务员只能查看自己的订单数据！");
			}else{
				String phone = (String) map.get("phone");
			/*	if(user.getRoleLevel() ==3){
					//三级管理员，只能看本业务范围的本区域的数据
					sql.append(" and user_id =(select us.id from user_info us where us.phone='"+phone+"'"
							+ " and us.role_id in ("
								+ "select ro.id from role_info ro where (ro.biz_range= "+user.getRoleBizRange()+"))"
							+ " and us.region like '"+user.getRegion()+"%')");
				}
				if(user.getRoleLevel()==2){
					//二级管理员,只能看本业务范围的数据，不能看到对方数据
					sql.append(" and user_id =(select us.id from user_info us where us.phone='"+phone+"'"
									+ " and us.role_id in ("
										+ "select ro.id from role_info ro where (ro.biz_range= "+user.getRoleBizRange()+")))");
				}
				if(user.getRoleLevel()<=1 || user.getRoleLevel()==4){
					//业务员或者管理员或者超管(如果是业务员，已经判断过是否是自己)
					sql.append(" and user_id =(select us.id from user_info us where us.phone='"+phone+"')");
				}*/

				sql.append(" and user_id =(select us.id from user_info us where us.phone='"+phone+"')");
			}

		}else{
			String userId = user.getId();
			if(map.containsKey("userId")){
				if(!userId.equals(map.get("userId").toString().trim()) && user.getRoleLevel()==4){
					throw new Exception("业务员只能查看自己的订单数据！");
				}else{
					userId = (String) map.get("userId");
				/*	if(user.getRoleLevel() ==3){
						//三级管理员，只能看本业务范围的本区域的数据
						sql.append(" and user_id =(select us.id from user_info us where us.id='"+userId+"'"
								+ " and us.role_id in ("
									+ "select ro.id from role_info ro where (ro.biz_range= "+user.getRoleBizRange()+"))"
								+ " and us.region like '"+user.getRegion()+"%')");
					}

					if(user.getRoleLevel()==2){
						//二级管理员,sql.append只是为了限制不同的二级管理员（蜂蜜或者男科）不能看到对方数据
						sql.append(" and user_id =(select us.id from user_info us where us.id='"+userId+"'"
										+ " and us.role_id in ("
											+ "select ro.id from role_info ro where (ro.biz_range= "+user.getRoleBizRange()+")))");
					}*/
					/*if(user.getRoleLevel()<=1 || user.getRoleLevel()==4){*/
					//业务员或者管理员或者超管(如果是业务员，已经判断过是否是自己)
					sql.append(" and user_id='"+userId+"'");
					/*}*/

				}
			}else{
				sql.append(" and user_id ='"+userId+"'");
			}


		}
		sql.append(" and order_nature is not null ");
		sql.append(" group by order_nature,express_state ");
		sql.append(getOrderBySQL(" order by order_code,deliver_date"));
		List<OrderVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<OrderVO>(OrderVO.class));
		if (list == null || list.size() <= 0) {
			return null;
		}

		Map<String, Object> data2 = new HashMap<>();
		data2.put("orderNature", "合计");
		// 求和
		int sumOrderNum = list.stream().mapToInt(OrderVO::getOrderState).sum();
		data2.put("orderNum", sumOrderNum);
		data2.put("depositAmout",
				list.stream().map(p -> p.getDepositAmout() != null ? p.getDepositAmout() : new BigDecimal(0.00))
						.reduce(new BigDecimal(0.00), BigDecimal::add));
		data2.put("collectionAmout",
				list.stream().map(p -> p.getCollectionAmout() != null ? p.getCollectionAmout() : new BigDecimal(0.00))
						.reduce(new BigDecimal(0.00), BigDecimal::add));
		data2.put("totalAmount",
				list.stream().map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
						.reduce(new BigDecimal(0.00), BigDecimal::add));

		NumberFormat percent = NumberFormat.getPercentInstance();
		percent.setMaximumFractionDigits(2);

		// 筛选已签收的数据
		List<OrderVO> sumSignList = list.stream().filter(a -> a.getExpressState() == 5).collect(Collectors.toList());
		int sumSignNum = sumSignList.stream().mapToInt(OrderVO::getOrderState).sum();
		if (sumSignNum > 0) {
			data2.put("signNum", sumSignNum);
			data2.put("signAmount",
					sumSignList.stream()
							.map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
							.reduce(new BigDecimal(0.00), BigDecimal::add));
			BigDecimal singRate = new BigDecimal(sumSignNum).divide(new BigDecimal(sumOrderNum), 4,
					RoundingMode.HALF_UP);
			data2.put("signRate", percent.format(singRate.doubleValue()));
		} else {
			data2.put("signNum", 0);
			data2.put("signAmount", 0);
			data2.put("signRate", percent.format(0));
		}
		// 筛选已退回的数据
		List<OrderVO> sumRejectedList = list.stream().filter(a -> a.getExpressState() == 4).collect(Collectors.toList());
		int sumRejectedNum = sumRejectedList.stream().mapToInt(OrderVO::getOrderState).sum();
		percent.setMaximumFractionDigits(2);
		if (sumRejectedNum > 0) {
			data2.put("rejectedNum", sumRejectedNum);
			data2.put("rejectedAmount",
					sumRejectedList.stream()
							.map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
							.reduce(new BigDecimal(0.00), BigDecimal::add));

		} else {
			data2.put("rejectedNum", 0);
			data2.put("rejectedAmount", 0);
		}

		// 按订单性质分组
		Map<String, List<OrderVO>> partSortMap = new LinkedHashMap<>();
		Map<String, List<OrderVO>> partCollect = list.stream().collect(Collectors.groupingBy(OrderVO::getOrderNature));
		partCollect.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.forEachOrdered(e -> partSortMap.put(e.getKey(), e.getValue()));
		for (Map.Entry<String, List<OrderVO>> partEntry : partSortMap.entrySet()) {
			if (partEntry.getValue() != null && !partEntry.getValue().isEmpty()) {
				List<OrderVO> partList = partEntry.getValue();
				if (partList != null && !partList.isEmpty()) {
					Map<String, Object> data4 = new HashMap<>();
					data4.put("orderNature", partList.get(0).getOrderNature());
					int partOrderNum = partList.stream().mapToInt(OrderVO::getOrderState).sum();
					data4.put("orderNum", partOrderNum);
					data4.put("depositAmout",
							partList.stream()
									.map(p -> p.getDepositAmout() != null ? p.getDepositAmout() : new BigDecimal(0.00))
									.reduce(new BigDecimal(0.00), BigDecimal::add));
					data4.put("collectionAmout",
							partList.stream().map(
									p -> p.getCollectionAmout() != null ? p.getCollectionAmout() : new BigDecimal(0.00))
									.reduce(new BigDecimal(0.00), BigDecimal::add));
					data4.put("totalAmount",
							partList.stream()
									.map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
									.reduce(new BigDecimal(0.00), BigDecimal::add));
					// 筛选已签收的数据
					List<OrderVO> partSignList = partList.stream().filter(a -> a.getExpressState() == 5)
							.collect(Collectors.toList());
					if (partSignList != null && !partSignList.isEmpty()) {
						int partSignNum = partSignList.stream().mapToInt(OrderVO::getOrderState).sum();
						if (partSignNum > 0) {
							data4.put("signNum", partSignNum);
							data4.put("signAmount",
									partSignList.stream().map(
											p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
											.reduce(new BigDecimal(0.00), BigDecimal::add));
							BigDecimal singRate = new BigDecimal(partSignNum).divide(new BigDecimal(partOrderNum), 4,
									RoundingMode.HALF_UP);
							data4.put("signRate", percent.format(singRate.doubleValue()));
						} else {
							data4.put("signNum", 0);
							data4.put("signAmount", 0);
							data4.put("signRate", percent.format(0));
						}
					} else {
						data4.put("signNum", 0);
						data4.put("signAmount", 0);
						data4.put("signRate", percent.format(0));
					}

					// 筛选已退回的数据
					List<OrderVO> partRejectedList = partList.stream().filter(a -> a.getExpressState() == 4).collect(Collectors.toList());
					if(partRejectedList != null && !partRejectedList.isEmpty()){
						int partRejectedNum = partRejectedList.stream().mapToInt(OrderVO::getOrderState).sum();
						if (partRejectedNum > 0) {
							data4.put("rejectedNum", partRejectedNum);
							data4.put("rejectedAmount",
									partRejectedList.stream()
											.map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : new BigDecimal(0.00))
											.reduce(new BigDecimal(0.00), BigDecimal::add));

						} else {
							data4.put("rejectedNum", 0);
							data4.put("rejectedAmount", 0);
						}
					}else{
						data4.put("rejectedNum", 0);
						data4.put("rejectedAmount", 0);
					}

					reDatas.add(data4);
				}

			}

		}
		reDatas.add(data2);
		return reDatas;

	}

	@Override
	public Object regionOrderNature(Map<String, Object> map) throws Exception {
		if(!map.containsKey("orderNature") || map.get("orderNature") == null){
			throw new Exception("订单性质不能为空！");
		}
		List<Map<String, Object>> reDatas = new ArrayList<>();
		reDatas = regionOrder(map);
		if(CollectionUtils.isEmpty(reDatas)) {
			throw new Exception("没有查询到数据！");
		}
		for(int i=0;i<reDatas.size();i++){
			String value = (String) reDatas.get(i).get("orderNature");
			if(value.equals("合计")){
				reDatas.remove(i);
			}
		}
		return reDatas;

	}

	@Transactional
	public Object  updateOrders(Map<String,Object> map) throws Exception {
		/*Map<String,String> map1 = new HashMap<>();
		map1.put("2019-02-12", "2019-02-15");
		map1.put("2019-02-16", "2019-02-19");
		map1.put("2019-02-20", "2019-02-23");
		map1.put("2019-02-24", "2019-02-27");
		map1.put("2019-02-28", "2019-03-03");
		map1.put("2019-03-04", "2019-03-06");
		long start = System.currentTimeMillis();
		 System.out.println("beginTIme:"+start);
		 for (Map.Entry<String, String> entry : map1.entrySet()) {
			  System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			  doUpateCostRatioOrder(entry.getKey(),entry.getValue());
			}*/
		/*map1.forEach((k, v) -> {
			taskExecutor.execute(new Runnable() {
			    @Override
			    public void run() {
			        	try {
							doUpateCostRatioOrder(k,v);
						} catch (Exception e) {
							e.printStackTrace();
						}

			    }
			});
		});*/
	/*	 while (true){
	            int count = taskExecutor.getActiveCount();
	            //System.out.println("Active Threads : " + count);
	            if(count==0){
	                //taskExecutor.shutdown();
	                long end = System.currentTimeMillis();
	                System.out.println("totalTime:"+(end-start)/1000 +"s");
	                break; //所有线程任务执行完
	            }
		 }*/

		doUpateCostRatioOrder((String)map.get("beginDate"),(String)map.get("endDate"));
		return null;
	}


	public void doUpateCostRatioOrder(String beginDate, String endDate) throws Exception {
		String sql = "select * from order_info where warehouse='001' and create_time between '" + beginDate
				+ " 00:00:00' and '" + endDate + " 23:59:59'";
		List<OrderVO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<OrderVO>(OrderVO.class));
		List<String> upSqls = new ArrayList<>();
		List<Object[]> batchArgs = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(list)) {
			queryOrderProducts(list);
			String upSql = "update order_info set cost_ratio =?,is_over_cost=?,cost_amount=? where id=?";
			for (OrderVO order : list) {
				order.setCostRatio(calculateCostRatio(order));
				order.setIsOverCost(isOverCost(order) ? 0 : 1);
				String sql1 = "update order_info set cost_ratio='" + order.getCostRatio() + "',is_over_cost ="
						+ order.getIsOverCost() + " ,cost_amount='" + order.getCostAmount() + "' where id='"
						+ order.getId() + "'";
				upSqls.add(sql1);
			}
			/*for(int i=0;i<list.size();i++){
				Object[] array = new Object[4];
				array[0] = list.get(i).getCostRatio();
				array[1] = list.get(i).getIsOverCost();
				array[2] = list.get(i).getCostAmount();
				array[3] = list.get(i).getId();
				batchArgs.add(array);
			}*/
			if (CollectionUtils.isNotEmpty(upSqls)) {
				/*if (CollectionUtils.isNotEmpty(batchArgs)) {*/
				int[] re= jdbcTemplate.batchUpdate(upSqls.toArray(new String[upSqls.size()]));
				//int[] re= jdbcTemplate.batchUpdate(upSql, batchArgs);
				System.out.println(beginDate+"============="+upSqls.size()+""+upSqls.toString());
			}
		}
	}

}
