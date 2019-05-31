package com.firm.orders.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.firm.orders.base.annotation.CryptoType;
import com.firm.orders.base.utils.AES;


/**
 * 请求参数解密过滤器<br>
 * 需要解密的方法，在对应方法或者类上添加注解@CryptoType(CryptoType.CRYPTO)<br>
 * 前端加密会把参数通过AES方式加密成base64，“encrypt”： 加密后内容<br>
 * FIXME 当前只做请求参数加密，返回值未做加密
 * 
 * @author ldy
 *
 */
@WebFilter(urlPatterns = { "/*" }, filterName = "requestDecryptFilter")
public class RequestDecryptFilter extends OncePerRequestFilter implements ApplicationContextAware {

	private static Logger logger = LoggerFactory.getLogger(RequestDecryptFilter.class);

	/** 方法映射集 */
	private List<HandlerMapping> handlerMappings;

	/** AES加解密 */
	//protected static AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, "1234567812345678".getBytes(),"1234567812345678".getBytes());

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			Object handler = getHandler(request).getHandler();
			if (handler instanceof HandlerMethod) {
				HandlerMethod method = (HandlerMethod) handler;
				// 方法上注解
				CryptoType type = method.getMethodAnnotation(CryptoType.class);
				if (null == type) {
					// 类上是否有注解
					type = method.getBeanType().getAnnotation(CryptoType.class);
					if (null == type) {
						filterChain.doFilter(request, response);
						return;
					}
				}
				if (type.value() != CryptoType.Type.CRYPTO) {
					// 不是解密跳过
					filterChain.doFilter(request, response);
					return;
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			filterChain.doFilter(request, response);
			return;
		}

		try {
			// 调用自定义request解析参数
			filterChain.doFilter(new DecryptRequest(request), response);
		} catch (IOException e) {
			// 异常处理
			logger.debug("Decrypt fail");
			response.setContentType("application/json; charset=UTF-8");
			response.getWriter().write(JSON.toJSONString("验签失败"));
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context,
				HandlerMapping.class, true, false);
		if (!matchingBeans.isEmpty()) {
			this.handlerMappings = new ArrayList<>(matchingBeans.values());
			// We keep HandlerMappings in sorted order.
			AnnotationAwareOrderComparator.sort(this.handlerMappings);
		}
	}

	/**
	 * 获取访问目标方法
	 * 
	 * @param request
	 * @return HandlerExecutionChain
	 * @throws Exception
	 */
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		if (this.handlerMappings != null) {
			for (HandlerMapping hm : this.handlerMappings) {
				if (logger.isTraceEnabled()) {
					logger.trace("Testing handler map [" + hm + "] in DispatcherServlet with name ''");
				}
				HandlerExecutionChain handler = hm.getHandler(request);
				if (handler != null) {
					return handler;
				}
			}
		}
		return null;
	}

	/**
	 * 解密request封装
	 * 
	 * @author ldy
	 *
	 */
	private class DecryptRequest extends HttpServletRequestWrapper {

		private static final String APPLICATION_JSON = "application/json";
		/** 所有参数的Map集合 */
		private Map<String, String[]> parameterMap;
		/** 输入流 */
		private InputStream inputStream;

		public DecryptRequest(HttpServletRequest request) throws IOException {
			super(request);
			String contentType = request.getHeader("Content-Type");
			logger.debug("DecryptRequest -> contentType:{}", contentType);
			String encrypt = null;
			if (null != contentType && contentType.contains(APPLICATION_JSON)) {
				// json
				ServletInputStream io = request.getInputStream();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int length;
				while ((length = io.read(buffer)) != -1) {
					os.write(buffer, 0, length);
				}
				byte[] bytes = os.toByteArray();
				encrypt = (String) JSON.parseObject(new String(bytes)).get("encrypt");
			} else {
				// url
				encrypt = request.getParameter("encrypt");
			}
			logger.debug("DecryptRequest -> encrypt:{}", encrypt);
			// 解密
			String params = decrypt(encrypt);

			if (null != contentType && contentType.contains(APPLICATION_JSON)) {
				if (this.inputStream == null) {
					this.inputStream = new DecryptInputStream(new ByteArrayInputStream(params.getBytes()));
				}
			}
			parameterMap = buildParams(params);
		}

		private String decrypt(String encrypt) throws IOException {
			try {
				// 解密
				return new String(AES.decrypt(encrypt,"1234567890ABCDEF".getBytes("utf-8")));
			} catch (Exception e) {
				logger.error("", e);
				throw new IOException(e.getMessage());
			}
		}

		private Map<String, String[]> buildParams(String src) throws UnsupportedEncodingException {
			Map<String, String[]> map = new HashMap<>();
			Map<String, String> params = JSONObject.parseObject(src, new TypeReference<Map<String, String>>() {
			});
			for (String key : params.keySet()) {
				map.put(key, new String[] { params.get(key) });
			}
			return map;
		}

		@Override
		public String getParameter(String name) {
			String[] values = getParameterMap().get(name);
			if (values != null) {
				return (values.length > 0 ? values[0] : null);
			}
			return super.getParameter(name);
		}

		@Override
		public String[] getParameterValues(String name) {
			String[] values = getParameterMap().get(name);
			if (values != null) {
				return values;
			}
			return super.getParameterValues(name);
		}

		@Override
		public Enumeration<String> getParameterNames() {
			Map<String, String[]> multipartParameters = getParameterMap();
			if (multipartParameters.isEmpty()) {
				return super.getParameterNames();
			}

			Set<String> paramNames = new LinkedHashSet<String>();
			Enumeration<String> paramEnum = super.getParameterNames();
			while (paramEnum.hasMoreElements()) {
				paramNames.add(paramEnum.nextElement());
			}
			paramNames.addAll(multipartParameters.keySet());
			return Collections.enumeration(paramNames);
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return null == parameterMap ? super.getParameterMap() : parameterMap;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			return this.inputStream == null ? super.getInputStream() : (ServletInputStream) this.inputStream;
		}
	}

	/**
	 * 自定义ServletInputStream
	 * 
	 * @author ldy
	 *
	 */
	private class DecryptInputStream extends ServletInputStream {

		private final InputStream sourceStream;

		/**
		 * Create a DelegatingServletInputStream for the given source stream.
		 * 
		 * @param sourceStream
		 *            the source stream (never {@code null})
		 */
		public DecryptInputStream(InputStream sourceStream) {
			Assert.notNull(sourceStream, "Source InputStream must not be null");
			this.sourceStream = sourceStream;
		}

		@Override
		public int read() throws IOException {
			return this.sourceStream.read();
		}

		@Override
		public void close() throws IOException {
			super.close();
			this.sourceStream.close();
		}

		@Override
		public boolean isFinished() {
			return false;
		}

		@Override
		public boolean isReady() {
			return false;
		}

		@Override
		public void setReadListener(ReadListener readListener) {

		}
	}

}