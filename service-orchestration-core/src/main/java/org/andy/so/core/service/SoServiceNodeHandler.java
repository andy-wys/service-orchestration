package org.andy.so.core.service;

import com.alibaba.fastjson2.JSONObject;
import org.andy.so.core.schema.SoServiceNodeType;
import org.andy.so.core.schema.node.SoMerchantNode;
import org.andy.so.core.schema.node.SoServiceNode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * <h2>定义标准接口配置执行流程</h2>
 *
 * @author: andy
 */
public interface SoServiceNodeHandler {
    /**
     * <h3>为服务节点注册服务执行器，多次注册会被覆盖</h3>
     *
     * @param nodeType        节点类型
     * @param execNodeService 节点执行器
     */
    void registerExecNodeService(SoServiceNodeType nodeType, SoServiceNodeExecutor<?> execNodeService);

    /**
     * <h3>处理标准接口请求参数</h3>
     *
     * @param soMerchantNode 标准接口配置
     * @param paramBody      请求参数
     * @param request        HttpServletRequest
     * @param response       HttpServletResponse
     * @return 处理后的请求数据
     */
    Object handleMerchantReqParam(SoMerchantNode soMerchantNode, String paramBody, HttpServletRequest request, HttpServletResponse response);

    /**
     * <h3>处理标准接口响应数据</h3>
     *
     * @param soMerchantNode    标准接口配置
     * @param resp              响应数据，也可能是异常情况
     * @param apiServiceDataMap 原始数据
     * @param request           HttpServletRequest
     * @param response          HttpServletResponse
     * @return 最终返回的响应报文
     */
    Object handleMerchantRespData(SoMerchantNode soMerchantNode, Object resp, Map<String, SoExecNodeServiceData> apiServiceDataMap, HttpServletRequest request, HttpServletResponse response);

    /**
     * <h3>分发执行 merchant 配置服务节点</h3>
     *
     * @param soMerchantNode    标准接口配置
     * @param apiServiceDataMap 全局数据存储
     * @param param             标准请求参数
     * @param request           HttpServletRequest
     * @param response          HttpServletResponse
     * @return 所有服务执行结果
     */
    Object dispatchServiceGroup(SoMerchantNode soMerchantNode, Map<String, SoExecNodeServiceData> apiServiceDataMap, JSONObject param, HttpServletRequest request, HttpServletResponse response);

    /**
     * <h3>校验 {@link SoServiceNode} 是否需要执行</h3>
     *
     * @param apiConfig         远程接口配置
     * @param defaultApiData    默认的数据存取来源
     * @param apiServiceDataMap 全部的数据存储
     * @return true 可以执行，false 不执行
     */
    boolean verifyApiExecCondition(SoServiceNode apiConfig, SoExecNodeServiceData defaultApiData, Map<String, SoExecNodeServiceData> apiServiceDataMap);

    /**
     * <h3>构建服务请求参数，并将构建后的数据设置到 currentApiData 中</h3>
     *
     * @param apiConfig         服务配置
     * @param currentApiData    当前节点数据存储对象
     * @param defaultData       默认数据
     * @param apiServiceDataMap 全局数据
     */
    void buildServiceReqParam(SoServiceNode apiConfig,
                              SoExecNodeServiceData currentApiData,
                              SoExecNodeServiceData defaultData,
                              Map<String, SoExecNodeServiceData> apiServiceDataMap);

    /**
     * <h3>执行服务的 request data handle 处理器</h3>
     *
     * @param apiConfig      服务配置
     * @param currentApiData 当前节点数据存储对象
     */
    void doServiceReqHandle(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData);

    /**
     * <h3>执行服务调用</h3>
     *
     * @param apiConfig      服务配置
     * @param currentApiData 当前节点数据存储对象
     * @param defaultData    默认数据
     */
    void execService(SoServiceNode apiConfig,
                     SoExecNodeServiceData currentApiData,
                     SoExecNodeServiceData defaultData);

    /**
     * <h3>执行当前服务的 response data handle 处理器</h3>
     *
     * @param apiConfig      服务配置
     * @param currentApiData 当前节点数据存储对象
     */
    void doServiceRespHandle(SoServiceNode apiConfig, SoExecNodeServiceData currentApiData);

    /**
     * <h3>构建服务响应结果</h3>
     *
     * @param apiConfig         服务配置
     * @param currentApiData    当前节点数据存储对象
     * @param defaultData       默认数据
     * @param apiServiceDataMap 全局数据
     */
    void buildServiceRespData(SoServiceNode apiConfig,
                              SoExecNodeServiceData currentApiData,
                              SoExecNodeServiceData defaultData,
                              Map<String, SoExecNodeServiceData> apiServiceDataMap);

}
