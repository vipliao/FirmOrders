package com.firm.order.config.aouth.session;

import java.io.Serializable;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.firm.order.utils.SymmetricEncoder;

/**
 * Shiro SessionManager
 * 
 * @author LIAO
 *
 */
public class FirmWebSessionManager extends DefaultWebSessionManager {
	private static final Logger log = LoggerFactory.getLogger(DefaultWebSessionManager.class);

	@Value("${httpheader.authorization}")
	private String authorization;
	@Value("${encrypt.encodeRules}")
	private String encodeRules;
	
	/**
	 * 重写获取sessionId的方法调用当前Manager的获取方法
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
		try{
			// 从请求头中获取token
			String enToken = WebUtils.toHttp(request).getHeader(authorization);
			// 判断是否有值
			if (StringUtils.isNoneBlank(enToken)) {
				//解密
				String token = SymmetricEncoder.AESDncode(encodeRules, enToken).split("//")[1];
				// 设置当前session状态
				request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, "url");
				request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID,token);
				request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
				return token;
			}
			// 若header获取不到token则尝试从cookie中获取
			//return super.getSessionId(request, response);
		}catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}

	
}
