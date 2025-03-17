package org.andy.so.core.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.andy.so.core.SoMerchantFactory;
import org.andy.so.core.entity.SoResp;
import org.andy.so.core.error.SoCheckErrorEnum;
import org.andy.so.core.schema.node.SoMerchantNode;
import org.andy.so.core.service.SoExecNodeServiceData;
import org.andy.so.core.service.SoMerchantServiceHandler;
import org.andy.so.core.service.SoServiceNodeHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * <h2>xml 配置的标准接口服务执行实现流程</h2>
 * 该实现是整个标准接口执行的入口
 *
 * @author: andy
 */
public class SoMerchantServiceDefaultHandler implements SoMerchantServiceHandler {
    private final Log log = LogFactory.getLog(getClass());
    /**
     * 商户标准接口配置工厂类，接口配置从该接口中查找
     */
    SoMerchantFactory merchantFactory;
    /**
     * xml 配置服务执行对象
     */
    SoServiceNodeHandler execXmlService;

    /**
     * 通过构造函数来注入 {@link SoMerchantFactory} 和 {@link SoServiceNodeHandler}
     *
     * @param merchantFactory 标准接口工厂类
     * @param execXmlService  xml 配置执行类
     */
    public SoMerchantServiceDefaultHandler(SoMerchantFactory merchantFactory, SoServiceNodeHandler execXmlService) {
        this.merchantFactory = merchantFactory;
        this.execXmlService = execXmlService;
    }

    /**
     * <h2>查找 XML 配置并执行 {@link SoServiceNodeHandler} 定义的执行流程</h2>
     *
     * <b>执行流程主要包括：</b>
     * <ul>
     *     <li>查找要执行的节点配置</li>
     *     <li>执行标准请求数据处理器</li>
     *     <li>创建全局数据保存字典</li>
     *     <li>执行节点分发逻辑并执行 {@link SoServiceNodeHandler#dispatchServiceGroup}</li>
     *     <li>执行标准响应数据处理器</li>
     * </ul>
     *
     * @param request      HttpServletRequest
     * @param response     HttpServletResponse
     * @param merchantCode 商户编码
     * @param apiPath      接口路径
     * @param paramBody    请求参数
     * @return 执行结果
     */
    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response, String merchantCode, String apiPath, String paramBody) {
        log.info("网关接收到请求[merchantCode = " + merchantCode + ", apiPath = " + apiPath + "]，请求参数：" + paramBody);
        SoMerchantNode merchantNode = merchantFactory.getMerchantNode(merchantCode, apiPath);

        if (merchantNode == null || CollectionUtils.isEmpty(merchantNode.getApiConfigList())) {
            return SoResp.buildError(SoCheckErrorEnum.UNDEFINED_SERVICE_ERROR);
        }
        // 先执行请求数据处理器
        Object data;
        try {
            data = execXmlService.handleMerchantReqParam(merchantNode, paramBody, request, response);
        } catch (Exception e) {
            Object resp = execXmlService.handleMerchantRespData(merchantNode, e, null, request, response);
            log.info("网关执行 [merchantCode = " + merchantCode
                    + ", apiPath = " + apiPath
                    + "] 的请求参数处理器出现异常，响应报文：" + JSON.toJSONString(resp), e);
            return resp;
        }

        // 将请求数据转换成 JSON Object
        JSONObject param = parseParamToJsonObject(data, merchantCode, apiPath);

        // 创建字典用于保存所有接口的数据
        Map<String, SoExecNodeServiceData> apiServiceDataMap = new HashMap<>(merchantNode.getApiConfigList().size() + 1, 1.0f);
        // 将标准的请求作为一个全局默认的数据存取
        SoExecNodeServiceData globalApiData = new SoExecNodeServiceData(merchantNode.getId(), request, response, param, new JSONObject(), null, null);
        // 将标准接口的数据先放到列表中
        apiServiceDataMap.put(merchantNode.getId(), globalApiData);

        Object resp = execXmlService.dispatchServiceGroup(merchantNode, apiServiceDataMap, param, request, response);

        resp = execXmlService.handleMerchantRespData(merchantNode, resp, apiServiceDataMap, request, response);
        log.info("网关执行 [merchantCode = " + merchantCode
                + ", apiPath = " + apiPath + "] 完毕，响应报文：" + JSON.toJSONString(resp));

        return resp;
    }

    /**
     * 将请求数据转换成 JSON Object
     *
     * @param data         请求数据
     * @param merchantCode 商户编码
     * @param apiPath      api path
     * @return JSON Object
     */
    private JSONObject parseParamToJsonObject(Object data, String merchantCode, String apiPath) {
        JSONObject param;
        if (data == null) {
            param = new JSONObject();
        } else if (data instanceof JSONObject) {
            param = (JSONObject) data;
        } else {
            param = JSON.parseObject(data.toString());
        }
        param.put("merchantCode", merchantCode);
        param.put("apiPath", apiPath);
        return param;
    }
}
