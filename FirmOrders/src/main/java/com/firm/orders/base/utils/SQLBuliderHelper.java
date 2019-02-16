package com.firm.orders.base.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.PrimaryKey;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 生产SQL语句方法
 * @author LIAO
 *
 */
public class SQLBuliderHelper {
	/*
	 * 生成创建table语句
	 */
	public static String create(Class<?> clazz) {
		StringBuilder sb = new StringBuilder();
		String tableName = getTableName(clazz);
		if (null == tableName || "".equals(tableName)) {
			return null;
		}
		sb.append("CREATE TABLE ").append(tableName).append("(");
		Map<String, String> columnMap = getColumn(clazz);
		for (Map.Entry<String, String> entry : columnMap.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" ");
			sb.append(entry.getValue());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		return sb.toString();
	}

	/*
	 * 生成插入语句
	 */
	public static String insert(Class<?> clazz) {
		StringBuilder sb = new StringBuilder();
		String tableName = getTableName(clazz);
		sb.append("INSERT INTO ").append(tableName).append(" (");
		Map<String, String> columnMap = getColumn(clazz);
		for (Map.Entry<String, String> entry : columnMap.entrySet()) {
			sb.append(entry.getKey());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(") ").append("VALUES").append("(");
		for (int i = 0; i < columnMap.size(); i++) {
			sb.append("?").append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		return sb.toString();
	}

	public static String delete() {
		return null;
	}

	/*
	 * 获取表名称
	 */
	public static String getTableName(Class<?> clazz) {
		// 判断是否为Table注释类型是方法返回true，否则返回false
		if (clazz.isAnnotationPresent(Table.class)) {
			// 获取注解信息
			Table table = clazz.getAnnotation(Table.class);
			if (!"".equals(table.name())) {
				return table.name();
			}
		}
		return null;
	}

	/*
	 * 获取字段信息的Map
	 */
	public static Map<String, String> getColumn(Class<?> clazz) {
		Map<String, String> columns = new HashMap<String, String>();
		// 通过反射取得所有Field，因为table那里是注解指定的注解，类型，column是对应方法上的所以不能class.isAnnotationPresent直接判断
		Field[] fields = clazz.getDeclaredFields();
		if (fields != null) {
			String columnName, type;
			Column column;
			for (int i$ = 0; i$ < fields.length; i$++) {
				if(fields[i$].getName().equals("serialVersionUID")){
					continue;
				}
				// 判断当前field是否为Column注解
				if (fields[i$].isAnnotationPresent(Column.class)) {
					// 获取注解对象
					column = fields[i$].getAnnotation(Column.class);
					columnName = column.name();
					if ("".equals(columnName)) {
						throw new RuntimeException("未找到对应字段名：" + i$);
					}
					// 根据不同类型生成不同的SQL
					if (int.class.isAssignableFrom(fields[i$].getType())) {
						type = "int";
					} else if (String.class.isAssignableFrom(fields[i$].getType())) {
						type = "String";
					} else if (Date.class.isAssignableFrom(fields[i$].getType())) {
						type = "Date";
					} else if (BigDecimal.class.isAssignableFrom(fields[i$].getType())) {
						type = "BigDecimal";
					}else {
						throw new RuntimeException("不支持数据类型：" + fields[i$].getType().getSimpleName());
					}
					type += (column==null ? " " : " NOT NULL");
					columns.put(columnName, type);
				} else if (fields[i$].isAnnotationPresent(PrimaryKey.class)) {
					PrimaryKey primaryKey = fields[i$].getAnnotation(PrimaryKey.class);
					// 将一个类的成员变量置为private,其实我也不知道啥意思，有知道的指点一下 ，谢谢！
					fields[i$].setAccessible(true);
					columnName = primaryKey.name();
					if ("".equals(columnName)) {
						throw new RuntimeException("未找到对应主键名");
					}
					type = "INT PRIMARY KEY AUTO_INCREMENT";
					columns.put(columnName, type);
				}else if (fields[i$].isAnnotationPresent(Id.class)) {
					if(fields[i$].isAnnotationPresent(Column.class)){
						fields[i$].setAccessible(true);
						columnName = fields[i$].getAnnotation(Column.class).name();
					}else{
						columnName = fields[i$].getName();
					}
					if ("".equals(columnName)) {
						throw new RuntimeException("未找到对应主键名");
					}
					type = "INT PRIMARY KEY AUTO_INCREMENT";
					columns.put(columnName, type);
				}else if (fields[i$].getAnnotations() == null) {
					columnName = fields[i$].getName();
					// 根据不同类型生成不同的SQL
					if (int.class.isAssignableFrom(fields[i$].getType())) {
						type = "int";
					} else if (String.class.isAssignableFrom(fields[i$].getType())) {
						type = "String";
					} else if (Date.class.isAssignableFrom(fields[i$].getType())) {
						type = "Date";
					} else if (BigDecimal.class.isAssignableFrom(fields[i$].getType())) {
						type = "BigDecimal";
					}else {
						throw new RuntimeException("不支持数据类型：" + fields[i$].getType().getSimpleName());
					}
					columns.put(columnName, type);
				}

			}
		}
		return columns;
	}

	public static <T> String insert(T entity) {
		Field[] fields = entity.getClass().getDeclaredFields();
		String sql = insert(entity.getClass());
		Map<String, String> columnMap = getColumn(entity.getClass());
		for (int i$ = 0; i$ < fields.length; i$++) {
			// 获取字段信息名
			String columnValue = fields[i$].getName();
			if(fields[i$].getName().equals("serialVersionUID")){
				continue;
			}
			columnValue = columnValue.substring(0, 1).toUpperCase() + columnValue.substring(1);
			try {
				Method method = entity.getClass().getMethod("get" + columnValue);
				//String value = (String) method.invoke(entity).toString();
				String value=null;
				Object obj = method.invoke(entity);
				String type = columnMap.get(columnValue);
				if(type != null && !type.equals("")){
					if(type.equals("int")){
						value = Integer.toString((int)obj);
					}else if(type.equals("String")){
						value =(String) obj;
					}else if(type.equals("Date")){
						Date date = (Date) obj;
						value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
					}else if(type.equals("BigDecimal")){
						BigDecimal b = (BigDecimal)obj;
						value = ""+b;
					}
				}	
				sql = sql.replaceFirst("\\?", value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sql;
	}
}
