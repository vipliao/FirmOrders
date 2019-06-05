package com.firm.order.config.aouth.config;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import  org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.firm.order.config.aouth.filter.LoginAuthorizationFilter;
import com.firm.order.config.aouth.filter.LogoutAuthorizationFilter;
import com.firm.order.config.aouth.filter.PermsAuthorizationFilter;
import com.firm.order.config.aouth.realm.ShiroRealm;
import com.firm.order.config.aouth.session.FirmWebSessionManager;

@Configuration
public class ShiroConfig {
	
	@Value("${session.sessionTimeout}")
	private long sessionTimeout;
	@Value("${ehcache.file}")
	private String ehcacheFileName;
	@Value("${session.sessionTimeoutClean}")
	private long sessionTimeoutClean;
	@Value("${session.id}")
	private String sessionIdName;

    @Bean
    public ShiroRealm shiroRealm() {
    	ShiroRealm shiroRealm = new ShiroRealm();
        return shiroRealm;
    }

   

    //Filter工厂，设置对应的过滤条件和跳转条件
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        
        Map<String,String> map = new HashMap<String, String>();
        map.put("/assessory/**","anon");
        map.put("/user/login","anon");
        map.put("/logout","logout");
        map.put("/**","authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);

        //登录
        shiroFilterFactoryBean.setLoginUrl("/user/login");
        //首页
        shiroFilterFactoryBean.setSuccessUrl("");
        //错误页面，认证不通过跳转
        shiroFilterFactoryBean.setUnauthorizedUrl("");
        
        Map<String,Filter> filtersMap = new HashMap<>();
        filtersMap.put("authc", new LoginAuthorizationFilter());
        filtersMap.put("perms", new PermsAuthorizationFilter());
        filtersMap.put("logout", new LogoutAuthorizationFilter());
        shiroFilterFactoryBean.setFilters(filtersMap);
        return shiroFilterFactoryBean;
    }
    
/*    @Bean
    public SessionValidationScheduler sessionValidationScheduler() {
    	QuartzSessionValidationScheduler sessionValidationScheduler = new QuartzSessionValidationScheduler();
    	sessionValidationScheduler.setSessionValidationInterval(sessionTimeoutClean);
    	sessionValidationScheduler.setSessionManager((ValidatingSessionManager) sessionManager());
    	return sessionValidationScheduler;
    }
    */
    @Bean
    public CachingSessionDAO sessionDao() {
    	EnterpriseCacheSessionDAO dao = new EnterpriseCacheSessionDAO();
    	dao.setActiveSessionsCacheName("shiro-activeSessionCache");
    	dao.setSessionIdGenerator(sessionIdGenerator());
		return dao;
    	
    }
    @Bean
    public Cookie sessionIdCookie() {
    	SimpleCookie cookie = new SimpleCookie();
    	cookie.setName(sessionIdName);
    	cookie.setMaxAge(259200);
    	return cookie;
    }
    
    @Bean
    public SessionManager sessionManager() {
    	FirmWebSessionManager webSessionManager = new FirmWebSessionManager();
    	webSessionManager.setGlobalSessionTimeout(sessionTimeout);
    	webSessionManager.setDeleteInvalidSessions(true);
    	webSessionManager.setSessionValidationSchedulerEnabled(true);
    	//webSessionManager.setSessionValidationScheduler(sessionValidationScheduler());
    	webSessionManager.setSessionDAO(sessionDao());
    	webSessionManager.setSessionIdCookie(sessionIdCookie());
    	webSessionManager.setSessionIdCookieEnabled(true);
    	return webSessionManager;
    }
    
    @Bean
    public SessionIdGenerator sessionIdGenerator() {
		return new JavaUuidSessionIdGenerator();
    	
    }
    
    @Bean
    public CacheManager cacheManager() {
    	EhCacheManager cacheManager = new EhCacheManager();
    	cacheManager.setCacheManagerConfigFile("classpath:"+ehcacheFileName);
		return cacheManager;
    	
    }
    
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(shiroRealm());
        securityManager.setSessionManager(sessionManager());
        securityManager.setCacheManager(cacheManager());
        return securityManager;
    }
    

    //加入注解的使用，不加入这个注解不生效
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
    
    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
    	
    }
    
    @Bean
    @DependsOn(value = {"lifecycleBeanPostProcessor"})
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
    	DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
    	advisorAutoProxyCreator.setProxyTargetClass(true);
    	return advisorAutoProxyCreator;
    }
    
}
