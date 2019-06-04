package com.firm.order.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanMap;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.firm.order.utils.BeanHelper.ReflectionInfo;

public class BeanHelper {
	private final static Logger logger = LoggerFactory.getLogger(BeanHelper.class);

	/**
	 * 根据属性名获取属性值
	 */
	public static Object getFieldValue(String fieldName, Object o) {
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getter = "get" + firstLetter + fieldName.substring(1);
			Method method = o.getClass().getMethod(getter, new Class[] {});
			Object value = method.invoke(o, new Object[] {});
			return value;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 获取属性名数组
	 */
	public static String[] getFiledName(Object o) {
		Field[] fields = o.getClass().getDeclaredFields();
		String[] fieldNames = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			System.out.println(fields[i].getType());
			fieldNames[i] = fields[i].getName();
		}
		return fieldNames;
	}

	/**
	 * 获取属性类型(type)，属性名(name)，属性值(value)的map组成的list
	 */
	public static List<Map<String, Object>> getFiledsInfo(Object o) {
		Field[] fields = o.getClass().getDeclaredFields();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> infoMap = null;
		for (int i = 0; i < fields.length; i++) {
			infoMap = new HashMap<String, Object>();
			infoMap.put("type", fields[i].getType().toString());
			infoMap.put("name", fields[i].getName());
			infoMap.put("value", getFieldValue(fields[i].getName(), o));
			list.add(infoMap);
		}
		return list;
	}

	/**
	 * 获取对象的所有属性值，返回一个对象数组
	 */
	public static Object[] getFiledValues(Object o) {
		String[] fieldNames = getFiledName(o);
		Object[] value = new Object[fieldNames.length];
		for (int i = 0; i < fieldNames.length; i++) {
			value[i] = getFieldValue(fieldNames[i], o);
		}
		return value;
	}

	/**
	 * 将对象装换为map
	 *
	 * @param bean
	 * @return
	 */
	public static <T> Map<String, Object> beanToMap(T bean) {
		Map<String, Object> map = new HashMap<>();
		if (bean != null) {
			BeanMap beanMap = BeanMap.create(bean);
			for (Object key : beanMap.keySet()) {
				map.put(key + "", beanMap.get(key));
			}
		}
		return map;
	}

	/**
	 * 将map装换为javabean对象
	 *
	 * @param map
	 * @param bean
	 * @return
	 */
	public static <T> T mapToBean(Map<String, Object> map, T bean) {
		BeanMap beanMap = BeanMap.create(bean);
		beanMap.putAll(map);
		return bean;
	}

	/**
	 * 将List<T>转换为List<Map<String, Object>>
	 *
	 * @param objList
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> List<Map<String, Object>> objectsToMaps(List<T> objList) {
		List<Map<String, Object>> list = new ArrayList<>();
		if (objList != null && objList.size() > 0) {
			Map<String, Object> map = null;
			T bean = null;
			for (int i = 0, size = objList.size(); i < size; i++) {
				bean = objList.get(i);
				map = beanToMap(bean);
				list.add(map);
			}
		}
		return list;
	}

	/**
	 * 将List<Map<String,Object>>转换为List<T>
	 *
	 * @param maps
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T> List<T> mapsToObjects(List<Map<String, Object>> maps, Class<T> clazz)
			throws InstantiationException, IllegalAccessException {
		List<T> list = new ArrayList<>();
		if (maps != null && maps.size() > 0) {
			Map<String, Object> map = null;
			T bean = null;
			for (int i = 0, size = maps.size(); i < size; i++) {
				map = maps.get(i);
				bean = clazz.newInstance();
				mapToBean(map, bean);
				list.add(bean);
			}
		}
		return list;
	}

	protected static final Object[] NULL_ARGUMENTS = new Object[0];
	private static Map<String, BeanHelper.ReflectionInfo> cache = new ConcurrentHashMap<String, ReflectionInfo>();
	private static BeanHelper bhelp = new BeanHelper();

	public static BeanHelper getInstance() {
		return bhelp;
	}

	public static List<String> getPropertys(Object bean) {
		return Arrays.asList(getInstance().getPropertiesAry(bean));
	}

	public String[] getPropertiesAry(Object bean) {
		BeanHelper.ReflectionInfo reflectionInfo = null;
		reflectionInfo = this.cachedReflectionInfo(bean.getClass());
		HashSet propertys = new HashSet();
		Iterator arg3 = reflectionInfo.writeMap.keySet().iterator();

		while (arg3.hasNext()) {
			String key = (String) arg3.next();
			if (reflectionInfo.writeMap.get(key) != null) {
				propertys.add(key);
			}
		}

		return (String[]) propertys.toArray(new String[0]);
	}

	public static Object getProperty(Object bean, String propertyName) {
		try {
			Method e = getInstance().getMethod(bean, propertyName, false);
			return propertyName != null && e == null ? null : (e == null ? null : e.invoke(bean, NULL_ARGUMENTS));
		} catch (Exception arg3) {
			String errStr = "Failed to get property: " + propertyName;
			throw new RuntimeException(errStr, arg3);
		}
	}

	public static Object[] getPropertyValues(Object bean, String[] propertys) {
		Object[] result = new Object[propertys.length];

		try {
			Method[] e = getInstance().getMethods(bean, propertys, false);

			for (int arg5 = 0; arg5 < propertys.length; ++arg5) {
				if (propertys[arg5] != null && e[arg5] != null) {
					result[arg5] = e[arg5].invoke(bean, NULL_ARGUMENTS);
				} else {
					result[arg5] = null;
				}
			}

			return result;
		} catch (Exception arg4) {
			String errStr = "Failed to get getPropertys from " + bean.getClass();
			throw new RuntimeException(errStr, arg4);
		}
	}

	public static Method getMethod(Object bean, String propertyName) {
		return getInstance().getMethod(bean, propertyName, true);
	}

	public static Method getGetMethod(Object bean, String propertyName) {
		return getInstance().getMethod(bean, propertyName, false);
	}

	public static Method getSetMethod(Object bean, String propertyName) {
		return getInstance().getMethod(bean, propertyName, true);
	}

	public static Method[] getMethods(Object bean, String[] propertys) {
		return getInstance().getMethods(bean, propertys, true);
	}

	private Method[] getMethods(Object bean, String[] propertys, boolean isSetMethod) {
		Method[] methods = new Method[propertys.length];
		BeanHelper.ReflectionInfo reflectionInfo = null;
		reflectionInfo = this.cachedReflectionInfo(bean.getClass());

		for (int i = 0; i < propertys.length; ++i) {
			Method method = null;
			if (isSetMethod) {
				method = reflectionInfo.getWriteMethod(propertys[i]);
			} else {
				method = reflectionInfo.getReadMethod(propertys[i]);
			}

			methods[i] = method;
		}

		return methods;
	}

	private Method getMethod(Object bean, String propertyName, boolean isSetMethod) {
		Method method = null;
		BeanHelper.ReflectionInfo reflectionInfo = null;
		reflectionInfo = this.cachedReflectionInfo(bean.getClass());
		if (isSetMethod) {
			method = reflectionInfo.getWriteMethod(propertyName);
		} else {
			method = reflectionInfo.getReadMethod(propertyName);
		}

		return method;
	}

	private BeanHelper.ReflectionInfo cachedReflectionInfo(Class<?> beanCls) {
		return this.cacheReflectionInfo(beanCls, (List) null);
	}

	private BeanHelper.ReflectionInfo cacheReflectionInfo(Class<?> beanCls, List<PropDescriptor> pdescriptor) {
		String key = beanCls.getName();
		BeanHelper.ReflectionInfo reflectionInfo = (BeanHelper.ReflectionInfo) cache.get(key);
		if (reflectionInfo == null) {
			reflectionInfo = (BeanHelper.ReflectionInfo) cache.get(key);
			if (reflectionInfo == null) {
				reflectionInfo = new BeanHelper.ReflectionInfo();
				Object propDesc = new ArrayList();
				if (pdescriptor != null) {
					((List) propDesc).addAll(pdescriptor);
				} else {
					propDesc = this.getPropertyDescriptors(beanCls);
				}

				Iterator arg5 = ((List) propDesc).iterator();

				while (arg5.hasNext()) {
					PropDescriptor pd = (PropDescriptor) arg5.next();
					Method readMethod = pd.getReadMethod(beanCls);
					Method writeMethod = pd.getWriteMethod(beanCls);
					if (readMethod != null) {
						reflectionInfo.readMap.put(pd.getName().toLowerCase(), readMethod);
					}

					if (writeMethod != null) {
						reflectionInfo.writeMap.put(pd.getName().toLowerCase(), writeMethod);
					}
				}

				cache.put(key, reflectionInfo);
			}
		}

		return reflectionInfo;
	}

	public static void invokeMethod(Object bean, Method method, Object value) {
		try {
			if (method != null) {
				Object[] e = new Object[] { value };
				method.invoke(bean, e);
			}
		} catch (Exception arg4) {
			String errStr = "Failed to set property: " + method.getName();
			throw new RuntimeException(errStr, arg4);
		}
	}

	public static Timestamp toTimestamp(String str) throws  java.text.ParseException {
		SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long millionSeconds = 0L;

		millionSeconds = dateFmt.parse(str).getTime();

		Timestamp timestamp = new Timestamp(millionSeconds);
		return timestamp;
	}

	public static void setProperty(Object bean, String propertyName, Object value) {
		String errStr;
		try {
			Method e = getInstance().getMethod(bean, propertyName, true);
			if (propertyName == null || e != null) {
				if (e != null) {
					Class[] errStr1 = e.getParameterTypes();
					if ("ts".equals(propertyName) && value instanceof String) {
						e.invoke(bean, new Object[] { toTimestamp(value.toString()) });
					}

					if (!errStr1[0].isAssignableFrom(Boolean.TYPE) && !errStr1[0].isAssignableFrom(Boolean.class)) {
						if (!errStr1[0].isAssignableFrom(Integer.TYPE) && !errStr1[0].isAssignableFrom(Integer.class)) {
							if (errStr1[0].isAssignableFrom(String.class)) {
								if (value != null) {
									if (value instanceof BigDecimal) {
										e.invoke(bean, new Object[] { value.toString() });
									} else {
										e.invoke(bean, new Object[] { value });
									}
								}
							} else {
								e.invoke(bean, new Object[] { value });
							}
						} else if (value != null) {
							boolean intValue1 = true;
							int intValue2;
							if (value instanceof Boolean) {
								if (((Boolean) value).booleanValue()) {
									intValue2 = 1;
								} else {
									intValue2 = 0;
								}
							} else if (value instanceof Integer) {
								intValue2 = ((Integer) value).intValue();
							} else if (value instanceof Short) {
								intValue2 = ((Short) value).intValue();
							} else {
								if (!(value instanceof BigInteger)) {
									throw new RuntimeException(
											"未知类型:" + bean + "属性" + propertyName + " with value " + value);
								}

								intValue2 = ((BigInteger) value).intValue();
							}

							e.invoke(bean, new Object[] { Integer.valueOf(intValue2) });
						}
					} else if (value != null) {
						if (value instanceof BigInteger) {
							BigInteger intValue = (BigInteger) value;
							boolean booleanValue = intValue.intValue() == 1;
							e.invoke(bean, new Object[] { Boolean.valueOf(booleanValue) });
						} else {
							e.invoke(bean, new Object[] { value });
						}
					}

				}
			}
		} catch (IllegalArgumentException arg6) {
			errStr = "Failed to set property: " + propertyName + " at bean: " + bean.getClass().getName()
					+ " with value:" + value + " type:" + (value == null ? "null" : value.getClass().getName());
			throw new IllegalArgumentException(errStr, arg6);
		} catch (Exception arg7) {
			errStr = "Failed to set property: " + propertyName + " at bean: " + bean.getClass().getName()
					+ " with value:" + value;
			throw new RuntimeException(errStr, arg7);
		}
	}

	public Method[] getAllGetMethod(Class<?> beanCls, String[] fieldNames) {
		Method[] methods = null;
		BeanHelper.ReflectionInfo reflectionInfo = null;
		ArrayList al = new ArrayList();
		reflectionInfo = this.cachedReflectionInfo(beanCls);
		String[] arg5 = fieldNames;
		int arg6 = fieldNames.length;

		for (int arg7 = 0; arg7 < arg6; ++arg7) {
			String str = arg5[arg7];
			al.add(reflectionInfo.getReadMethod(str));
		}

		methods = (Method[]) al.toArray(new Method[al.size()]);
		return methods;
	}

	private List<PropDescriptor> getPropertyDescriptors(Class<?> clazz) {
		ArrayList descList = new ArrayList();
		new ArrayList();
		ArrayList propsList = new ArrayList();
		Class propType = null;
		Method[] superClazz = clazz.getDeclaredMethods();
		int arg6 = superClazz.length;

		for (int arg7 = 0; arg7 < arg6; ++arg7) {
			Method method = superClazz[arg7];
			if (method.getName().length() >= 4 && method.getName().charAt(3) >= 65
					&& method.getName().charAt(3) <= 90) {
				if (method.getName().startsWith("set")) {
					if (method.getParameterTypes().length != 1 || method.getReturnType() != Void.TYPE) {
						continue;
					}

					propType = method.getParameterTypes()[0];
				} else {
					if (!method.getName().startsWith("get") || method.getParameterTypes().length != 0) {
						continue;
					}

					propType = method.getReturnType();
				}

				String propname = method.getName().substring(3, 4).toLowerCase();
				if (method.getName().length() > 4) {
					propname = propname + method.getName().substring(4);
				}

				if (!propname.equals("class") && !propsList.contains(propname)) {
					propsList.add(propname);
					descList.add(new PropDescriptor(clazz, propType, propname));
				}
			}
		}

		Class arg10 = clazz.getSuperclass();
		if (arg10 != null) {
			List superDescList = this.getPropertyDescriptors(arg10);
			descList.addAll(superDescList);
			if (!this.isBeanCached(arg10)) {
				this.cacheReflectionInfo(arg10, superDescList);
			}
		}

		return descList;
	}

	private boolean isBeanCached(Class<?> bean) {
		String key = bean.getName();
		BeanHelper.ReflectionInfo cMethod = (BeanHelper.ReflectionInfo) cache.get(key);
		if (cMethod == null) {
			cMethod = (BeanHelper.ReflectionInfo) cache.get(key);
			if (cMethod == null) {
				return false;
			}
		}

		return true;
	}

	static class ReflectionInfo {
		Map<String, Method> readMap = new HashMap();
		Map<String, Method> writeMap = new HashMap();

		Method getReadMethod(String prop) {
			return prop == null ? null : (Method) this.readMap.get(prop.toLowerCase());
		}

		Method getWriteMethod(String prop) {
			return prop == null ? null : (Method) this.writeMap.get(prop.toLowerCase());
		}
	}

}
