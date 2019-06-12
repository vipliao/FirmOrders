package com.firm.order.config.cors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.firm.order.config.decrypt.AES;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class CorsConfig implements Filter {

    @Value("${httpheader.authorization}")
    private String authorization;

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
        httpResponse.setHeader("Access-Control-Allow-Headers", "*");
        //httpResponse.setHeader("Access-Control-Expose-Headers","Content-Disposition");
        if (httpRequest.getMethod().toUpperCase().equals("OPTIONS")) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(new DecryptRequest(httpRequest), response);
        }

    }

    /**
     * 解密request封装
     *
     * @author ldy
     */
    private class DecryptRequest extends HttpServletRequestWrapper {

        private static final String APPLICATION_JSON = "application/json";
        /**
         * 所有参数的Map集合
         */
        private Map<String, String[]> parameterMap;
        /**
         * 输入流
         */
        private InputStream inputStream;

        public DecryptRequest(HttpServletRequest request) throws IOException {
            super(request);
            String contentType = request.getHeader("Content-Type");
            String method = request.getMethod();
            String params = null;
            if (method.toUpperCase().equals("GET")) {
                params = request.getParameter("p");
            } else if (method.toUpperCase().equals("POST")) {
                byte[] data = IOUtils.toByteArray(request.getInputStream());
                params = new String(data);
            }
            // 解密
            if (StringUtils.isNotBlank(params)) {
                params = AES.decrypt(params);
                if (null != contentType && contentType.contains(APPLICATION_JSON)) {
                    if (this.inputStream == null) {
                        this.inputStream = new DecryptInputStream(new ByteArrayInputStream(params.getBytes()));
                    }
                }
                parameterMap = buildParams(params);
            }

        }

        private Map<String, String[]> buildParams(String src) throws UnsupportedEncodingException {
            Map<String, String[]> map = new HashMap<>();
            Map<String, String> params = JSONObject.parseObject(src, new TypeReference<Map<String, String>>() {
            });
            for (String key : params.keySet()) {
                map.put(key, new String[]{params.get(key)});
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


    private class DecryptInputStream extends ServletInputStream {

        private final InputStream sourceStream;

        /**
         * Create a DelegatingServletInputStream for the given source stream.
         *
         * @param sourceStream the source stream (never {@code null})
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
