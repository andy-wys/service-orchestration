package org.andy.so.core.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.andy.so.core.SoApplicationContextAware;
import org.andy.so.core.SoError;
import org.andy.so.core.SoMerchantFactory;
import org.andy.so.core.entity.SoResp;
import org.andy.so.core.error.SoErrorEnum;
import org.andy.so.core.error.SoServiceErrorEnum;
import org.andy.so.core.error.SoServiceException;
import org.andy.so.core.help.SoConditionHandleHelper;
import org.andy.so.core.help.SoDataHandleHelper;
import org.andy.so.core.help.SoParamConvertHelper;
import org.andy.so.core.schema.SoServiceNodeType;
import org.andy.so.core.schema.enums.SoServiceExecBlockedTypeEnum;
import org.andy.so.core.schema.enums.SoServiceNodeChildTypeEnum;
import org.andy.so.core.schema.enums.SoServiceNodeTypeEnum;
import org.andy.so.core.schema.node.SoMerchantNode;
import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoExecNodeServiceData;
import org.andy.so.core.service.SoServiceNodeExecutor;
import org.andy.so.core.service.SoServiceNodeHandler;
import org.andy.so.core.util.SoObjectUtil;
import org.andy.so.core.util.SoStopWatch;
import org.andy.so.core.util.SoStringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Map;

/**
 * <h2>默认 XML 服务执行过程实现</h2>
 *
 * @author: andy
 */
public class SoNodeServiceDefaultHandler implements SoServiceNodeHandler {
    private final Log log = LogFactory.getLog(getClass());
    /**
     * 数据处理器工厂帮助类，查找并执行数据处理器
     */
    SoDataHandleHelper soDataHandleHelper;
    /**
     * 条件处理器帮助类，查找并执行条件处理器
     */
    SoConditionHandleHelper soConditionHandleHelper;
    /**
     * 查找并执行参数转换
     */
    SoParamConvertHelper soParamConvertHelper;
    /**
     * 配置工厂
     */
    SoMerchantFactory merchantFactory;

    public SoNodeServiceDefaultHandler(SoDataHandleHelper soDataHandleHelper,
                                       SoConditionHandleHelper soConditionHandleHelper,
                                       SoParamConvertHelper soParamConvertHelper,
                                       SoMerchantFactory merchantFactory) {
        this.soDataHandleHelper = soDataHandleHelper;
        this.soConditionHandleHelper = soConditionHandleHelper;
        this.soParamConvertHelper = soParamConvertHelper;
        this.merchantFactory = merchantFactory;
    }

    @Override
    public void registerExecNodeService(SoServiceNodeType nodeType, SoServiceNodeExecutor<?> execNodeService) {
        if (SoObjectUtil.anyNull(merchantFactory, nodeType, execNodeService)) {
            return;
        }
        merchantFactory.registerServiceNodeExecutor(nodeType, execNodeService);
    }

    @Override
    public Object handleMerchantReqParam(SoMerchantNode soMerchantNode, String paramBody, HttpServletRequest request, HttpServletResponse response) {
        return soDataHandleHelper.doConvert(soMerchantNode.getReqDataHandle(), paramBody, request, response);
    }

    @Override
    public Object handleMerchantRespData(SoMerchantNode soMerchantNode, Object resp,
                                         Map<String, SoExecNodeServiceData> apiServiceDataMap,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (resp instanceof Throwable) {
            if (resp instanceof SoError) {
                resp = SoResp.buildError((SoError) resp);
            } else {
                resp = SoResp.buildError(SoErrorEnum.REQ_PARAM_ERROR);
            }
            return soDataHandleHelper.doConvert(soMerchantNode.getRespDataHandle(), resp, request, response);
        }
        // resp 如果不为空，则说明是执行出现了异常
        if (resp == null) {
            resp = buildRespResult(apiServiceDataMap.get(soMerchantNode.getId()), response);
        }
        // 执行响应结果处理器
        resp = soDataHandleHelper.doConvert(soMerchantNode.getRespDataHandle(), resp, request, response);
        return resp;
    }

    /**
     * <h2>当前只考虑串行执行方式，并行方式后续有需求再优化</h2>
     *
     * @param soMerchantNode    标准接口配置
     * @param apiServiceDataMap 全局数据存储
     * @param param             标准请求参数
     * @param request           HttpServletRequest
     * @param response          HttpServletResponse
     * @return 执行结果
     */
    @Override
    public Object dispatchServiceGroup(SoMerchantNode soMerchantNode,
                                       Map<String, SoExecNodeServiceData> apiServiceDataMap,
                                       JSONObject param, HttpServletRequest request,
                                       HttpServletResponse response) {
        Object resp = null;
        for (SoServiceNode conf : soMerchantNode.getApiConfigList()) {
            try {
                sendRemoteApi(conf, apiServiceDataMap.get(soMerchantNode.getId()), apiServiceDataMap);
            } catch (Exception e) {
                log.error(conf.getNodeType() + "服务 [nodeId = " + conf.getId() + ", serviceId = " + conf.getServiceId() + ", method = " + conf.getMethod() + "] 执行异常：", e);
                if (SoServiceExecBlockedTypeEnum.RETURN == conf.getBlocked()) {
                    break;
                } else if (SoServiceExecBlockedTypeEnum.EXCEPTION == conf.getBlocked()) {
                    if (e instanceof SoError) {
                        resp = SoResp.buildError((SoError) e);
                    } else {
                        resp = SoResp.build(SoServiceErrorEnum.REMOTE_REQUEST_ERROR.getCode(),
                                SoServiceErrorEnum.REMOTE_REQUEST_ERROR.getMessage() + ": " + e.getMessage(),
                                null);
                    }
                    break;
                }
                // else continue
            }
        }
        return resp;
    }

    @Override
    public boolean verifyApiExecCondition(SoServiceNode apiConfig, SoExecNodeServiceData defaultApiData, Map<String, SoExecNodeServiceData> apiServiceDataMap) {
        return soConditionHandleHelper.handleCondition(apiConfig, defaultApiData, apiServiceDataMap);
    }

    @Override
    public void buildServiceReqParam(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData, SoExecNodeServiceData defaultData, Map<String, SoExecNodeServiceData> apiServiceDataMap) {
        soParamConvertHelper.convertPropFromConf(apiConfig, currentApiData, defaultData, apiServiceDataMap, SoServiceNodeChildTypeEnum.REQ);
    }

    @Override
    public void doServiceReqHandle(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData) {
        String reqHandle = apiConfig.getApiHandle(SoServiceNodeChildTypeEnum.REQ.name());
        if (SoStringUtil.isNotBlank(reqHandle)) {
            currentApiData.setReqBody(soDataHandleHelper.doConvert(reqHandle, currentApiData.getReqBody()));
        }
    }

    @Override
    public void execService(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData, SoExecNodeServiceData defaultApiData) {
        Object respStrBody;
        // 自定义执行器
        String executorName = apiConfig.getExecutor();
        SoServiceNodeExecutor<?> execNodeService = SoApplicationContextAware.getBean(executorName, SoServiceNodeExecutor.class);
        if (execNodeService != null) {
            if (SoStringUtil.isNotBlank(executorName) && log.isDebugEnabled()) {
                log.debug("找到用户自定义的执行器来执行：executorName = " + executorName + "，executorClass = " + execNodeService.getClass().getName());
            }
        } else {
            execNodeService = merchantFactory.getServiceNodeExecutor(apiConfig.getNodeType());
        }
        if (execNodeService == null) {
            throw new SoServiceException(SoServiceErrorEnum.NOT_FOUND_EXEC_NODE_SERVICE, null, apiConfig.getNodeType());
        }
        respStrBody = execNodeService.execute(apiConfig, defaultApiData.getHttpServletRequest(), defaultApiData.getHttpServletResponse(), currentApiData);

        // 重定向类型直接返回
        if (apiConfig.getNodeType() == SoServiceNodeTypeEnum.REDIRECT) {
            return;
        }
        currentApiData.setRespBody(respStrBody);
    }

    @Override
    public void doServiceRespHandle(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData) {
        String respHandle = apiConfig.getApiHandle(SoServiceNodeChildTypeEnum.RESP.name());
        String respStrBody;
        Object handleResult = currentApiData.getRespBody();
        if (SoStringUtil.isNotBlank(respHandle)) {
            handleResult = soDataHandleHelper.doConvert(respHandle, handleResult);
        }
        if (handleResult == null) {
            respStrBody = null;
        } else if (handleResult instanceof String) {
            respStrBody = (String) handleResult;
        } else {
            respStrBody = JSON.toJSONString(handleResult);
        }

        Object respPostHandleData;
        if (JSON.isValidObject(respStrBody)) {
            respPostHandleData = JSON.parseObject(respStrBody);
        } else if (JSON.isValidArray(respStrBody)) {
            respPostHandleData = JSON.parseArray(respStrBody);
        } else {
            respPostHandleData = respStrBody;
        }

        currentApiData.setRespBody(respPostHandleData);
    }

    @Override
    public void buildServiceRespData(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData, SoExecNodeServiceData defaultApiData, Map<String, SoExecNodeServiceData> apiServiceDataMap) {
        soParamConvertHelper.convertPropFromConf(apiConfig, defaultApiData, currentApiData, apiServiceDataMap, SoServiceNodeChildTypeEnum.RESP);
    }

    /**
     * 构建最终的响应报文
     *
     * @param apiServiceData 数据
     * @param response       http response
     * @return 标准出参
     */
    private Object buildRespResult(SoExecNodeServiceData apiServiceData, HttpServletResponse response) {
        if (apiServiceData == null) {
            return SoResp.build(null);
        }
        // 设置 header
        if (apiServiceData.getHttpHeaders() != null) {
            for (Map.Entry<String, String> entry : apiServiceData.getHttpHeaders().entrySet()) {
                if (entry.getValue() != null) {
                    response.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
        // 设置 cookie
        int arrLen = 2;
        if (apiServiceData.getHttpCookies() != null) {
            apiServiceData.getHttpCookies().forEach(s -> {
                String[] strArr = s.split("=");
                if (strArr.length == arrLen) {
                    response.addCookie(new Cookie(strArr[0], strArr[1]));
                }
            });
        }
        return apiServiceData.getRespBody();
    }

    /**
     * 通过配置发送远程接口调用
     *
     * @param apiConfig         远程接口配置
     * @param defaultApiData    默认的数据存取来源
     * @param apiServiceDataMap 全部的数据存储
     */
    private void sendRemoteApi(SoServiceNode apiConfig, SoExecNodeServiceData defaultApiData, Map<String, SoExecNodeServiceData> apiServiceDataMap) {
        if (apiConfig == null) {
            return;
        }
        log.info("准备调用 " + apiConfig.getNodeType() + " 服务[nodeId = " + apiConfig.getId() + ", serviceId = " +
                apiConfig.getServiceId() + "," + " serviceMethod = " + apiConfig.getMethod() + "]，开始组装请求数据...");

        // 校验条件是否可以继续执行
        boolean condition = verifyApiExecCondition(apiConfig, defaultApiData, apiServiceDataMap);
        if (!condition) {
            log.info("条件校验不通过，不再执行该服务: " + apiConfig.getNodeType() + " 服务[nodeId = " + apiConfig.getId() +
                    ", serviceId = " + apiConfig.getServiceId() + ", serviceMethod = " + apiConfig.getMethod() + "]");
            return;
        }

        SoExecNodeServiceData currentApiData = new SoExecNodeServiceData();
        currentApiData.setHttpCookies(new ArrayList<>(0));

        // 如果当前 apiConfig 的 id 不为空，则将当前 api config 加入到数据列中
        if (SoStringUtil.isNotBlank(apiConfig.getId())) {
            currentApiData.setApiId(apiConfig.getId());
            apiServiceDataMap.put(apiConfig.getId(), currentApiData);
        }
        SoStopWatch stopWatch = new SoStopWatch("执行服务 [" + apiConfig.getServiceId() + "]");

        stopWatch.start("执行构建请求报文");
        // step 1: build req param
        buildServiceReqParam(apiConfig, currentApiData, defaultApiData, apiServiceDataMap);
        stopWatch.stop();
        log.info("耗时：[" + stopWatch.getLastTaskTimeMillis() + "]，转换请求参数后数据：" + JSONObject.toJSONString(currentApiData));

        stopWatch.start("执行请求报文处理器");
        // step 2: handle request param body
        doServiceReqHandle(apiConfig, currentApiData);
        stopWatch.stop();
        if (SoStringUtil.isNotBlank(apiConfig.getApiHandle(SoServiceNodeChildTypeEnum.REQ.name()))) {
            log.info("耗时：[" + stopWatch.getLastTaskTimeMillis() +
                    "]，执行 req handle [" + apiConfig.getApiHandle(SoServiceNodeChildTypeEnum.REQ.name()) +
                    "] 后的数据：" + JSONObject.toJSONString(currentApiData.getReqBody()));
        }

        stopWatch.start("调用服务请求");
        // step 3: send network request
        execService(apiConfig, currentApiData, defaultApiData);
        if (apiConfig.getNodeType() == SoServiceNodeTypeEnum.REDIRECT) {
            stopWatch.stop();
            return;
        }
        stopWatch.stop();
        log.info("耗时：[" + stopWatch.getLastTaskTimeMillis()
                + "]，调用 " + apiConfig.getNodeType()
                + " 服务 " + apiConfig.getServiceId()
                + " 结束，响应报文：" + currentApiData.getRespBody());


        stopWatch.start("执行响应处理器");
        // step 4: handle response body
        doServiceRespHandle(apiConfig, currentApiData);
        stopWatch.stop();
        if (SoStringUtil.isNotBlank(apiConfig.getApiHandle(SoServiceNodeChildTypeEnum.RESP.name()))) {
            log.info("耗时：[" + stopWatch.getLastTaskTimeMillis()
                    + "]，执行 resp handle [" + apiConfig.getApiHandle(SoServiceNodeChildTypeEnum.RESP.name())
                    + "] 后的数据：" + currentApiData.getRespBody());
        }

        // step 5: build response body
        stopWatch.start("构建响应报文");
        buildServiceRespData(apiConfig, currentApiData, defaultApiData, apiServiceDataMap);
        stopWatch.stop();

        log.info("耗时：[" + stopWatch.getLastTaskTimeMillis() + "]，" + apiConfig.getNodeType()
                + " 服务 [nodeId = " + apiConfig.getId()
                + ", serviceId = " + apiConfig.getServiceId()
                + ", method = " + apiConfig.getMethod() + "]，执行完毕...");

        log.debug(stopWatch.prettyPrintMillis());
    }
}
