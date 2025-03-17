package org.andy.so.core.service.impl;

import com.alibaba.fastjson2.JSONObject;
import org.andy.so.core.error.SoServiceErrorEnum;
import org.andy.so.core.error.SoServiceException;
import org.andy.so.core.schema.SoServiceNodeType;
import org.andy.so.core.schema.enums.SoServiceNodeTypeEnum;
import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoExecNodeServiceData;
import org.andy.so.core.service.SoServiceNodeExecutor;
import org.andy.so.core.util.SoStringUtil;
import org.andy.so.core.util.SoUriUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <h2>执行重定向实现类</h2>
 *
 * @author: andy
 */
public class SoRedirectNodeExecutor implements SoServiceNodeExecutor<String> {
    @Override
    public boolean isMatchedExecutor(SoServiceNodeType serviceNodeType) {
        return SoServiceNodeTypeEnum.REDIRECT == serviceNodeType;
    }

    /**
     * 重定向
     *
     * @param apiConfig      节点配置
     * @param response       servlet
     * @param currentApiData 当前节点数据
     */
    @Override
    public String execute(SoServiceNode apiConfig, HttpServletRequest request, HttpServletResponse response, SoExecNodeServiceData currentApiData) {
        // 重定向
        String redirectUrl = apiConfig.getServiceId();
        if (SoStringUtil.isEmpty(redirectUrl) && currentApiData.getReqBody() instanceof JSONObject) {
            redirectUrl = ((JSONObject) currentApiData.getReqBody()).getString("url");
        }
        if (SoStringUtil.isEmpty(redirectUrl)) {
            throw new SoServiceException(SoServiceErrorEnum.REMOTE_REDIRECT_URL);
        }
        redirectUrl = SoUriUtil.convertUrlParam(redirectUrl, currentApiData.getReqBody(), apiConfig.getCharset().getCode());
        // header
        if (currentApiData.getHttpHeaders() != null) {
            currentApiData.getHttpHeaders().forEach((k, v) -> response.addHeader(k, String.valueOf(v)));
        }
        // cookie
        if (currentApiData.getHttpCookies() != null) {
            currentApiData.getHttpCookies().forEach(c -> {
                int index = c.indexOf("=");
                if (index < 1) {
                    return;
                }
                response.addCookie(new Cookie(c.substring(0, index), c.substring(index + 1)));
            });
        }
        try {
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw new SoServiceException(SoServiceErrorEnum.REMOTE_REDIRECT_ERROR, e);
        }
        return null;
    }
}
