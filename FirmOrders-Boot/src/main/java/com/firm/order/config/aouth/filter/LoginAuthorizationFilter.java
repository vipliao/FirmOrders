package com.firm.order.config.aouth.filter;

import com.firm.order.utils.EncryptHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class LoginAuthorizationFilter extends FormAuthenticationFilter{

    
	/**
	 * 用户登录校验失败回调方法，也可以自己重写校验方法isAccessAllowed
	 */
    @Override
    protected boolean onAccessDenied(ServletRequest request,
            ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        //设置编码格式，header的content-type也要设置，否则浏览器不会以utf8解析，还是乱码。设置application/json可以让js不需要eval即可使用对象
        String requestSissionId = (String) request.getAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID);
        String curruntSissionId = (String) SecurityUtils.getSubject().getSession().getId();
        String errorMsg="";
        if(requestSissionId == null || requestSissionId.equals("")){
        	errorMsg ="{\"success\":false,\"backMsg\":\"缺少认证信息，请求无效!\",\"auth\":false}";
        }else if(!requestSissionId.equals(curruntSissionId)){
        	errorMsg ="{\"success\":false,\"backMsg\":\"认证失效，请重新登录!\",\"auth\":false}";
        }
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setHeader("Content-type","application/json;charset=UTF-8");
        PrintWriter out;
        try {
            out = httpServletResponse.getWriter();
            out.println(EncryptHelper.encrypt(errorMsg));
            out.flush();
            out.close();
        } catch (IOException e1) {
            log.info(e1.getMessage());
        }
        return false;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token,
            AuthenticationException e, ServletRequest request,
            ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setHeader("Content-type",
                "application/json;charset=UTF-8");
        PrintWriter out;
        try {
            out = httpServletResponse.getWriter();
            out.println("{\"success\":false,\"backMsg\":\"系统错误!\"}");
            out.flush();
            out.close();
        } catch (IOException e1) {
            log.info(e1.getMessage());
        }
        return false;
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token,
            Subject subject, ServletRequest request, ServletResponse response)
            throws Exception {
        return super.onLoginSuccess(token, subject, request, response);
    }

}
