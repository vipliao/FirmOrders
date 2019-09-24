package com.firm.order.config.aouth.filter;

import com.alibaba.fastjson.JSONObject;
import com.firm.order.modules.base.encrypt.EncryptHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class PermsAuthorizationFilter extends PermissionsAuthorizationFilter
{


	@Override
	protected boolean onAccessDenied(ServletRequest arg0, ServletResponse arg1) throws IOException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) arg0;
		HttpServletResponse httpServletResponse = (HttpServletResponse) arg1;

		httpServletResponse.setCharacterEncoding("UTF-8");
		httpServletResponse.setHeader("Content-type", "application/json;charset=UTF-8");
		PrintWriter out;
		try {
			out = httpServletResponse.getWriter();
			out.println(EncryptHelper.encrypt(JSONObject.parse("{\"code\":-1,\"msg\":\"登录用户无权执行该操作！\"}")));
			out.flush();
			out.close();
		} catch (IOException e1) {
			log.info(e1.getMessage());
		}
		return false;
	}
}