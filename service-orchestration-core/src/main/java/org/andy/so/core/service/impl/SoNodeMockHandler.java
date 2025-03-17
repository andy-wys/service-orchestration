package org.andy.so.core.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.andy.so.core.SoMerchantFactory;
import org.andy.so.core.help.SoConditionHandleHelper;
import org.andy.so.core.help.SoDataHandleHelper;
import org.andy.so.core.help.SoMockHelper;
import org.andy.so.core.help.SoParamConvertHelper;
import org.andy.so.core.schema.node.SoMerchantNode;
import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoExecNodeServiceData;
import org.andy.so.core.util.SoJsonUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * <h2>mock 数据执行实现</h2>
 *
 * @author: andy
 */
public class SoNodeMockHandler extends SoNodeServiceDefaultHandler {
    /**
     * mock 数据帮助类
     */
    SoMockHelper soMockHelper;

    public SoNodeMockHandler(SoDataHandleHelper soDataHandleHelper,
                             SoConditionHandleHelper soConditionHandleHelper,
                             SoParamConvertHelper soParamConvertHelper,
                             SoMerchantFactory merchantFactory,
                             SoMockHelper soMockHelper) {
        super(soDataHandleHelper, soConditionHandleHelper, soParamConvertHelper, merchantFactory);
        this.soMockHelper = soMockHelper;
    }

    @Override
    public Object handleMerchantReqParam(SoMerchantNode soMerchantNode, String paramBody, HttpServletRequest request, HttpServletResponse response) {
        // mock 请求数据
        if (soMockHelper.isMockReqEnable(soMerchantNode.getMockNode())) {
            Object param = soMockHelper.parseMockReq(soMerchantNode.getMockNode());
            String dataHandle = soMerchantNode.getMockNode().getDataHandle();
            param = soDataHandleHelper.doConvert(dataHandle, param, request, response);
            if (param instanceof String) {
                paramBody = (String) param;
            } else {
                paramBody = JSON.toJSONString(param);
            }
        }
        return super.handleMerchantReqParam(soMerchantNode, paramBody, request, response);
    }

    @Override
    public Object dispatchServiceGroup(SoMerchantNode soMerchantNode, Map<String, SoExecNodeServiceData> apiServiceDataMap, JSONObject param, HttpServletRequest request, HttpServletResponse response) {
        if (soMockHelper.isMockRespEnable(soMerchantNode.getMockNode())) {
            Object result = soMockHelper.parseMockResp(soMerchantNode.getMockNode(), response);
            String dataHandle = soMerchantNode.getMockNode().getDataHandle();
            return soDataHandleHelper.doConvert(dataHandle, result, request, response);
        } else if (soMockHelper.isMockReqEnable(soMerchantNode.getMockNode())) {
            // 如果 mock 请求数据，则需要解析出 http header 和 cookie
            soMockHelper.parseMockHeader(soMerchantNode.getMockNode(), apiServiceDataMap.get(soMerchantNode.getId()));
        }
        return super.dispatchServiceGroup(soMerchantNode, apiServiceDataMap, param, request, response);
    }

    @Override
    public boolean verifyApiExecCondition(SoServiceNode apiConfig, SoExecNodeServiceData defaultApiData, Map<String, SoExecNodeServiceData> apiServiceDataMap) {
        if (soMockHelper.isMockEnable(apiConfig.getMockNode(), null)) {
            return soMockHelper.parseMockCondition(apiConfig.getMockNode());
        }
        return super.verifyApiExecCondition(apiConfig, defaultApiData, apiServiceDataMap);
    }

    @Override
    public void buildServiceReqParam(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData, SoExecNodeServiceData defaultData, Map<String, SoExecNodeServiceData> apiServiceDataMap) {
        // 如果该服务 mock 的是响应数据，则请求的逻辑不再执行
        if (soMockHelper.isMockRespEnable(apiConfig.getMockNode())) {
            return;
        }
        if (soMockHelper.isMockReqEnable(apiConfig.getMockNode())) {
            Object reqData = soMockHelper.parseMockReq(apiConfig.getMockNode());
            String dataHandle = apiConfig.getMockNode().getDataHandle();
            reqData = soDataHandleHelper.doConvert(dataHandle, reqData);

            currentApiData.setReqBody(reqData);
            soMockHelper.parseMockHeader(apiConfig.getMockNode(), currentApiData);
        } else {
            super.buildServiceReqParam(apiConfig, currentApiData, defaultData, apiServiceDataMap);
        }
    }

    @Override
    public void doServiceReqHandle(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData) {
        // 如果该服务在 mock 数据，则配置的 data handle 不再执行
        if (soMockHelper.isMockRespEnable(apiConfig.getMockNode())) {
            return;
        } else if (soMockHelper.isMockReqEnable(apiConfig.getMockNode())) {
            return;
        }
        super.doServiceReqHandle(apiConfig, currentApiData);
    }

    @Override
    public void execService(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData, SoExecNodeServiceData defaultApiData) {
        // 如果是 mock 的响应数据，则服务调用不再执行
        if (soMockHelper.isMockRespEnable(apiConfig.getMockNode())) {
            return;
        }
        super.execService(apiConfig, currentApiData, defaultApiData);
    }

    @Override
    public void buildServiceRespData(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData, SoExecNodeServiceData defaultApiData, Map<String, SoExecNodeServiceData> apiServiceDataMap) {
        if (soMockHelper.isMockRespEnable(apiConfig.getMockNode())) {
            // 将当前执行的结果合并到全局标准输出数据中
            Object result = SoJsonUtil.merge(currentApiData.getRespBody(), defaultApiData.getRespBody());
            defaultApiData.setRespBody(result);
        } else {
            super.buildServiceRespData(apiConfig, currentApiData, defaultApiData, apiServiceDataMap);
        }
    }

    @Override
    public void doServiceRespHandle(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData) {
        // 如果该服务在 mock 响应数据
        if (soMockHelper.isMockRespEnable(apiConfig.getMockNode())) {
            Object respData = soMockHelper.parseMockResp(apiConfig.getMockNode(), currentApiData.getHttpServletResponse());
            String dataHandle = apiConfig.getMockNode().getDataHandle();
            respData = soDataHandleHelper.doConvert(dataHandle, respData);

            currentApiData.setRespBody(respData);
            soMockHelper.parseMockHeader(apiConfig.getMockNode(), currentApiData);
            return;
        }
        super.doServiceRespHandle(apiConfig, currentApiData);
    }
}
