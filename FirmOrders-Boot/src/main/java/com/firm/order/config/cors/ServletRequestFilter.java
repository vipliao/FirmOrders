package com.firm.order.config.cors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.FilterConfig;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/*@Order(2)
@WebFilter(filterName = "servletRequestFilter",urlPatterns = "/*")*/
public class ServletRequestFilter implements Filter {

    //@Value("${forbidSuffix}")
    private String suffix;


    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if(filterSuffix(request, response).equals("404")){
            throw new ServletException("无效的请求地址");
        }
        chain.doFilter(new ServletRequestWrapper((HttpServletRequest)request), response);
    }

    @Override
    public void destroy() {
    }

    private String filterSuffix(ServletRequest request,ServletResponse response) throws IOException{
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String url = req.getRequestURI();
        String forbidSuffix = "pl,cgi,signature,listprint,bat,sh,dll,exe,txt,jsp,php,bash_history,ini,xml,inc,log,trc,lzma,ar,zip,ear,gz,war,rar,ace,lha,lzh,tar,arj,arc,tgz,wim,ARC,backup,";
        List<String> forbidSuffixs = Arrays.asList(forbidSuffix.split(","));
        int index = url.lastIndexOf(".");

//		List<String> suffixs = Arrays.asList(suffix.split(","));

        if (index > 0) {
            String suffix = url.substring(index + 1);
            if (forbidSuffixs.contains(suffix)) {
//				res.setContentType("application/json");
//				res.setHeader("Pragma", "No-cache");
//				res.setHeader("Cache-Control", "no-cache");
//				res.setCharacterEncoding("UTF-8");
//				res.setStatus(404);
//				res.getWriter().write(JSON.toJSONString(R.error("无效的请求地址")));
                return "404";
            }
        }
        return "200";
    }
}
