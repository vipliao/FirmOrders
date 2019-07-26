package com.firm.order.config.cors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.firm.order.utils.EncryptHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.util.Assert;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 解密request封装
 */
@Slf4j
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
            content = EncryptHelper.decrypt(content);
            if(StringUtils.isNotBlank(content)){
                JSONObject deData2obj = JSONObject.parseObject(content);
                if(deData2obj != null && deData2obj.containsKey("X-Auth-Token")){
                    String token = deData2obj.get("X-Auth-Token").toString();
                    if(StringUtils.isNotBlank(token)){
                        reflectSetparam(request,"X-Auth-Token",token);
                        content = deData2obj.fluentRemove("X-Auth-Token").toJSONString();
                    }
                }
                if (null != contentType && contentType.contains(APPLICATION_JSON)) {
                    if (this.inputStream == null) {
                        this.inputStream = new DecryptInputStream(new ByteArrayInputStream(content.getBytes("UTF-8")));
                    }
                }
                parameterMap = buildParams(content);
            }

        }

    }

    private Map<String, String[]> buildParams(String src) {
        Map<String, String[]> map = new HashMap<>();
        Map<String, String> params = JSONObject.parseObject(src, new TypeReference<Map<String, String>>() {});
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


    /**
     * 修改header信息，key-value键值对儿加入到header中(利用反射)
     * @param request
     * @param key
     * @param value
     */
    private void reflectSetparam(HttpServletRequest request,String key,String value){
        Class<? extends HttpServletRequest> requestClass = request.getClass();
        log.debug("request实现类="+requestClass.getName());
        try {
            Field request1 = requestClass.getDeclaredField("request");
            request1.setAccessible(true);
            Object o = request1.get(request);
            Field coyoteRequest = o.getClass().getDeclaredField("coyoteRequest");
            coyoteRequest.setAccessible(true);
            Object o1 = coyoteRequest.get(o);
            log.debug("coyoteRequest实现类="+o1.getClass().getName());
            Field headers = o1.getClass().getDeclaredField("headers");
            headers.setAccessible(true);
            MimeHeaders o2 = (MimeHeaders)headers.get(o1);
            o2.addValue(key).setString(value);
        } catch (Exception e) {
            log.error("修改header信息方法",e);
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
