package org.andy.so.core.service;

import org.andy.so.core.schema.SoSchemaConstant;
import org.andy.so.core.util.SoStringUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <h2>执行服务节点单元数据对象</h2>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public class SoExecNodeServiceData {
    /**
     * 接口的 ID，父级接口则使用 requestPath
     */
    private String apiId;
    /**
     * http 请求对象
     */
    private HttpServletRequest httpServletRequest;
    /**
     * http 响应对象
     */
    private HttpServletResponse httpServletResponse;
    /**
     * 请求数据报文
     */
    private Object reqBody;
    /**
     * 响应数据报文
     */
    private Object respBody;
    /**
     * 请求头
     */
    private Map<String, String> httpHeaders;
    /**
     * 请求 cookie
     */
    private List<String> httpCookies;

    /**
     * 构造方法
     *
     * @param apiId               接口唯一标识
     * @param httpServletRequest  http 请求对象
     * @param httpServletResponse http 响应对象
     * @param reqBody             请求报文
     * @param respBody            响应报文
     * @param httpHeaders         http 头信息
     * @param httpCookies         http cookie 信息
     */
    public SoExecNodeServiceData(String apiId,
                                 HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse,
                                 Object reqBody,
                                 Object respBody,
                                 Map<String, String> httpHeaders,
                                 List<String> httpCookies) {
        this.apiId = apiId;
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
        this.reqBody = reqBody;
        this.respBody = respBody;
        this.httpHeaders = httpHeaders;
        this.httpCookies = httpCookies;

    }

    /**
     * 无参构造方法
     */
    public SoExecNodeServiceData() {

    }

    /**
     * 获取 header 值<p>
     * 取值顺序：httpServletRequest 》 httpHeaders 》 httpServletResponse
     *
     * @param key header name
     * @return value
     */
    public Object getHeaderValue(String key) {
        key = deletePrefixPath(key);
        if (SoStringUtil.isEmpty(key)) {
            return null;
        }
        Object value = null;
        if (httpServletRequest != null) {
            value = httpServletRequest.getHeader(key);
        }
        if (value != null) {
            return value;
        }
        if (httpHeaders != null) {
            value = httpHeaders.get(key);
        }
        if (value != null) {
            return value;
        }
        if (httpServletResponse != null) {
            value = httpServletResponse.getHeader(key);
        }
        return value;
    }

    /**
     * 取 cookie 值， <b>当前只是简单的按照字符串拼接的方式处理，如果后续有需要，则要改成标准的 Cookie 对象属性处理</b>
     *
     * @param key cookie key
     * @return cookie value
     */
    public Object getCookieValue(String key) {
        key = deletePrefixPath(key);
        if (SoStringUtil.isEmpty(key)) {
            return null;
        }
        Object value = null;
        if (httpServletRequest != null && httpServletRequest.getCookies() != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if (key.equals(cookie.getName())) {
                    value = cookie.getValue();
                    break;
                }
            }
        }
        if (value != null) {
            return value;
        }
        if (httpCookies != null) {
            String keyPre = key + SoSchemaConstant.KEY_VALUE_CONNECTOR;
            for (String cs : httpCookies) {
                if (cs.startsWith(keyPre)) {
                    value = cs.split(keyPre)[1];
                    break;
                }
            }
        }
        return value;
    }

    /**
     * 替换 JSON path 前缀
     *
     * @param key header or cookie key
     * @return 替换 $. 前缀
     */
    private String deletePrefixPath(String key) {
        String path = "$.";
        if (key != null && key.startsWith(path)) {
            return key.replace(path, "");
        }
        return key;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    public Object getReqBody() {
        return reqBody;
    }

    public void setReqBody(Object reqBody) {
        this.reqBody = reqBody;
    }

    public Object getRespBody() {
        return respBody;
    }

    public void setRespBody(Object respBody) {
        this.respBody = respBody;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public List<String> getHttpCookies() {
        return httpCookies;
    }

    public void setHttpCookies(List<String> httpCookies) {
        this.httpCookies = httpCookies;
    }

    @Override
    public String toString() {
        return "SoExecNodeServiceData{" +
                "apiId='" + apiId + '\'' +
                ", reqBody=" + reqBody +
                ", respBody=" + respBody +
                ", httpHeaders=" + httpHeaders +
                ", httpCookies=" + httpCookies +
                '}';
    }
}
