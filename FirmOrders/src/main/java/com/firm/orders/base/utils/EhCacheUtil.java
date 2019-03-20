package com.firm.orders.base.utils;
import java.net.URL;
import com.firm.orders.user.vo.UserVO;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
 
/**
 * Created by xxx on 2017/7/29.
 */
public class EhCacheUtil {
    //ehcache.xml 保存在src/main/resources/
    private static final String path = "/ehcache.xml";
 
    private URL url;
 
    private CacheManager manager;
 
    private static EhCacheUtil ehCache;
 
    private EhCacheUtil(String path) {
        url = getClass().getResource(path);
        manager = CacheManager.create(url);
    }
 
    public static EhCacheUtil getInstance() {
        if (ehCache == null) {
            ehCache = new EhCacheUtil(path);
        }
        return ehCache;
    }
 
    public void put(String cacheName, String key, Object value) {
        Cache cache = manager.getCache(cacheName);
        Element element = new Element(key, value);
        cache.put(element);
    }
 
    public Object get(String cacheName, String key) {
        Cache cache = manager.getCache(cacheName);
        Element element = cache.get(key);
        return element == null ? null : element.getObjectValue();
    }
 
    public Cache get(String cacheName) {
        return manager.getCache(cacheName);
    }
 
    public void remove(String cacheName, String key) {
        Cache cache = manager.getCache(cacheName);
        cache.remove(key);
    }
    
    public static void main(String[] args) {
    	 //string测试
        EhCacheUtil.getInstance().put("orderCodeCache","userEch","test001");
        String val = (String) EhCacheUtil.getInstance().get("orderCodeCache", "userEch");
        
        //object测试
        UserVO user = new  UserVO();
        user.setId("11111111");
        user.setUserName("jack");
        EhCacheUtil.getInstance().put("orderCodeCache","userJack",user);
 
        UserVO user2=(UserVO) EhCacheUtil.getInstance().get("orderCodeCache", "userJack");
        String str="1";
        System.out.print(user2.getUserName());

	}

}