package com.firm.order.config.aouth.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.firm.order.utils.AES;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutAuthorizationFilter extends LogoutFilter {
	private static final Logger log = LoggerFactory.getLogger(LogoutAuthorizationFilter.class);

	@Override
	protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

		Subject subject = this.getSubject(request, response);
		if (this.isPostOnlyLogout()
				&& !WebUtils.toHttp(request).getMethod().toUpperCase(Locale.ENGLISH).equals("POST")) {
			
			return this.onLogoutRequestNotAPost(request, response);
		} else {
			String redirectUrl = this.getRedirectUrl(request, response, subject);
			String reMsg = "";
			try {
				String requestSissionId = (String) request.getAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID);
				String curruntSissionId = (String) SecurityUtils.getSubject().getSession().getId();
				if (requestSissionId == null || requestSissionId.equals("")) {
					reMsg = "{\"success\":false,\"backMsg\":\"缺少认证信息，请求无效!\",\"auth\":false}";
				} else if (!requestSissionId.equals(curruntSissionId)) {
					reMsg = "{\"success\":false,\"backMsg\":\"认证不通过，退出失败!\",\"auth\":false}";
				}
				if(reMsg ==null || reMsg.equals("")){
					subject.logout();
					reMsg="{\"success\":true,\"backMsg\":\"用户退出成功!\"}";
				}
			} catch (SessionException arg5) {
				log.debug("Encountered session exception during logout.  This can generally safely be ignored.", arg5);
			}

			
			
			HttpServletResponse httpServletResponse = (HttpServletResponse) response;
			// 设置编码格式，header的content-type也要设置，否则浏览器不会以utf8解析，还是乱码。设置application/json可以让js不需要eval即可使用对象
			response.setCharacterEncoding("UTF-8");
			httpServletResponse.setHeader("Content-type", "application/json;charset=UTF-8");
			PrintWriter out;
			try {
				out = httpServletResponse.getWriter();
				out.println(AES.encrypt(reMsg));
				out.flush();
				out.close();
			} catch (IOException e1) {
				log.info(e1.getMessage());
			}
			//this.issueRedirect(request, httpServletResponse, redirectUrl);
			return false;
		}
	}
	
	
}
