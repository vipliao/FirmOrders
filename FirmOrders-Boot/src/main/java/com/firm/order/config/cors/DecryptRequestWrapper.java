package com.firm.order.config.cors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.firm.order.utils.AES;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 解密request封装
 */
public class DecryptRequestWrapper extends HttpServletRequestWrapper {

    private static final String APPLICATION_JSON = "application/json";
    /**
     * 所有参数的Map集合
     */
    private Map<String, String[]> parameterMap;
    /**
     * 输入流
     */
    private InputStream inputStream;

    public DecryptRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String contentType = request.getHeader("Content-Type");
        String method = request.getMethod();
        String content = null;
        if (method.toUpperCase().equals("GET")) {
            content = request.getParameter("p");
        } else if (method.toUpperCase().equals("POST")) {
            byte[] data = IOUtils.toByteArray(request.getInputStream());
            content = new String(data);
        }
        // 解密
        if (StringUtils.isNotBlank(content)) {
            content = AES.decrypt(content);
            if (null != contentType && contentType.contains(APPLICATION_JSON)) {
                if (this.inputStream == null) {
                    this.inputStream = new DecryptInputStream(new ByteArrayInputStream(content.getBytes("UTF-8")));
                }
            }
            parameterMap = buildParams(content);
        }

    }

    private Map<String, String[]> buildParams(String src) {
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
