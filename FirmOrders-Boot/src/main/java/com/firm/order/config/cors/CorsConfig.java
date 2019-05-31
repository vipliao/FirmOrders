package com.firm.order.config.cors;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig implements Filter {


	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.setHeader("Access-Control-Allow-Origin", "*");
		httpResponse.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE");
		httpResponse.setHeader("Access-Control-Max-Age", "3600");
		httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
		httpResponse.setHeader("Access-Control-Allow-Headers",
				"x-requested-with,Access-Control,Content-Type,x-auth-token");
		//httpResponse.setHeader("Access-Control-Expose-Headers","Content-Disposition");

		chain.doFilter(request, response);

	}

	@Override
	public void destroy() {
	}
}
