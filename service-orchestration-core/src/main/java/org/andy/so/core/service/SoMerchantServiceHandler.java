package org.andy.so.core.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h2>处理 merchant 配置的标准接口服务</h2>
 * 该服务是整个标准接口执行的入口
 *
 * @author: andy
 */
public interface SoMerchantServiceHandler {

    /**
     * <h3>查找 XML 配置并执行 {@link SoServiceNodeHandler} 定义的执行流程</h3>
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
    Object handle(HttpServletRequest request,
                  HttpServletResponse response,
                  String merchantCode,
                  String apiPath,
                  String paramBody);
}
