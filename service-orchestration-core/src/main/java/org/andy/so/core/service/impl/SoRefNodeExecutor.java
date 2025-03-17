package org.andy.so.core.service.impl;

import com.alibaba.fastjson2.JSON;
import org.andy.so.core.SoExecLocalApi;
import org.andy.so.core.error.SoServiceErrorEnum;
import org.andy.so.core.error.SoServiceException;
import org.andy.so.core.schema.SoServiceNodeType;
import org.andy.so.core.schema.enums.SoServiceNodeTypeEnum;
import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoExecNodeServiceData;
import org.andy.so.core.service.SoServiceNodeExecutor;
import org.andy.so.core.util.SoStringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * <h2>执行引用类型实现类</h2>
 *
 * @author: andy
 */
public class SoRefNodeExecutor implements SoServiceNodeExecutor<String> {
    @Override
    public boolean isMatchedExecutor(SoServiceNodeType serviceNodeType) {
        return SoServiceNodeTypeEnum.REF == serviceNodeType;
    }

    @Override
    public String execute(SoServiceNode apiConfig, HttpServletRequest request, HttpServletResponse response, SoExecNodeServiceData currentApiData) {
        if (apiConfig == null || SoStringUtil.isBlank(apiConfig.getServiceId())) {
            throw new SoServiceException(SoServiceErrorEnum.NOT_FOUND_REF_ERROR);
        }
        String reqBody = null;
        if (currentApiData != null) {
            if (currentApiData.getReqBody() != null) {
                reqBody = JSON.toJSONString(currentApiData.getReqBody());
            }
            Map<String, String> header = currentApiData.getHttpHeaders();
            if (request != null && header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
            }
        }

        Object result = SoExecLocalApi.execute(request, response, apiConfig.getMethod(), apiConfig.getServiceId(), reqBody);
        if (result == null) {
            return null;
        } else if (result instanceof String) {
            return (String) result;
        }
        return JSON.toJSONString(result);
    }
}
