package org.andy.so.core.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.andy.so.core.SoError;
import org.andy.so.core.error.SoCheckErrorEnum;
import org.andy.so.core.error.SoServiceErrorEnum;
import org.andy.so.core.error.SoServiceException;
import org.andy.so.core.schema.SoServiceNodeType;
import org.andy.so.core.schema.enums.SoApiCharsetEnum;
import org.andy.so.core.schema.enums.SoApiContentTypeEnum;
import org.andy.so.core.schema.enums.SoApiMethodEnum;
import org.andy.so.core.schema.enums.SoServiceNodeTypeEnum;
import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoExecNodeServiceData;
import org.andy.so.core.service.SoServiceNodeExecutor;
import org.andy.so.core.trace.SoTraceConstant;
import org.andy.so.core.trace.SoTraceHelper;
import org.andy.so.core.util.SoStringUtil;
import org.andy.so.core.util.SoUriUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <h2>执行 Http 请求实现类</h2>
 *
 * @author: andy
 */
public class SoHttpNodeExecutor implements SoServiceNodeExecutor<String> {
    /**
     * spring rest 执行类
     */
    RestTemplate restTemplate;

    public SoHttpNodeExecutor() {
        this.restTemplate = buildDefaultRestTemplate();
    }

    @SuppressWarnings("unused")
    public SoHttpNodeExecutor(RestTemplate restTemplate) {
        if (restTemplate == null) {
            throw new SoServiceException(SoCheckErrorEnum.INIT_SERVICE_ERROR, null,
                    SoHttpNodeExecutor.class.getSimpleName(), "RestTemplate 不能为 null");
        }
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isMatchedExecutor(SoServiceNodeType serviceNodeType) {
        return SoServiceNodeTypeEnum.API == serviceNodeType;
    }

    /**
     * 发送网络请求
     *
     * @param apiConfig      接口配置
     * @param currentApiData 请求参数
     * @return 响应实体
     */
    @Override
    public String execute(SoServiceNode apiConfig, HttpServletRequest request, HttpServletResponse response, SoExecNodeServiceData currentApiData) {
        if (SoStringUtil.isEmpty(apiConfig.getServiceId())) {
            return null;
        }
        // 超时时间
        if (restTemplate.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
            HttpComponentsClientHttpRequestFactory rf = ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory());
            rf.setReadTimeout(apiConfig.getTimeout());
        }
        // header && cookie 赋值
        HttpHeaders httpHeaders = new HttpHeaders();

        Map<String, String> headers = currentApiData.getHttpHeaders();
        if (headers != null) {
            if (SoStringUtil.isEmpty(headers.get(SoTraceConstant.KEY_TRACE_ID))) {
                httpHeaders.add(SoTraceConstant.KEY_TRACE_ID, SoTraceHelper.get(SoTraceConstant.KEY_TRACE_ID));
            }
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }

        if (currentApiData.getHttpCookies() != null) {
            List<String> cookies = httpHeaders.get(HttpHeaders.SET_COOKIE);
            if (cookies == null) {
                httpHeaders.put(HttpHeaders.COOKIE, currentApiData.getHttpCookies());
            } else {
                cookies.addAll(currentApiData.getHttpCookies());
                httpHeaders.put(HttpHeaders.COOKIE, cookies);
            }
        }

        // 编码
        String charset = apiConfig.getCharset().getCode();
        if (SoStringUtil.isEmpty(charset)) {
            charset = SoApiCharsetEnum.UTF8.getCode();
        }

        ResponseEntity<String> responseEntity;
        SoApiContentTypeEnum apiContentType = apiConfig.getContentType();

        httpHeaders.setContentType(new MediaType(apiContentType.getType(), apiContentType.getSubtype()));
        httpHeaders.setAcceptCharset(Collections.singletonList(Charset.forName(charset)));

        if (SoApiMethodEnum.GET == SoApiMethodEnum.of(apiConfig.getMethod())) {
            String url = SoUriUtil.convertUrlParam(apiConfig.getServiceId(), currentApiData.getReqBody(), charset);
            responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(httpHeaders), String.class);
        } else {
            Object reqBody = currentApiData.getReqBody();
            if (apiConfig.getContentType() == SoApiContentTypeEnum.JSON) {
                if (reqBody instanceof JSONObject || reqBody instanceof JSONArray) {
                    reqBody = JSON.toJSONString(reqBody);
                } else if (reqBody != null && !(reqBody instanceof String)) {
                    reqBody = JSON.toJSONString(reqBody);
                }
            } else {
                reqBody = SoUriUtil.convertUrlParam(reqBody, charset);
            }
            HttpEntity<Object> httpEntity = new HttpEntity<>(reqBody, httpHeaders);
            responseEntity = restTemplate.postForEntity(apiConfig.getServiceId(), httpEntity, String.class);
        }

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new SoServiceException(new SoError() {
                @Override
                public String getCode() {
                    return SoServiceErrorEnum.REMOTE_REQUEST_ERROR.getCode();
                }

                @Override
                public String getMessage() {
                    return SoServiceErrorEnum.REMOTE_REQUEST_ERROR.getMessage() + ": " + responseEntity.getStatusCode();
                }
            });
        }
        return responseEntity.getBody();
    }


    /**
     * 构造默认的 RestTemplate
     *
     * @return RestTemplate
     */
    private RestTemplate buildDefaultRestTemplate() {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        // 连接池最大连接数
        connectionManager.setMaxTotal(500);
        connectionManager.setDefaultMaxPerRoute(100);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(15_000)
                .setConnectTimeout(10_000)
                .setConnectionRequestTimeout(1_000)
                .build();
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
