package org.andy.so.core.service;


import org.andy.so.core.schema.SoServiceNodeType;
import org.andy.so.core.schema.node.SoServiceNode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h2>配置节点执行服务接口定义，由不同的节点类型实现类去实现</h2>
 *
 * @author: andy
 */
public interface SoServiceNodeExecutor<T> {
    /**
     * <h3>该执行器是否匹配当前服务节点的类型</h3>
     *
     * @param serviceNodeType 节点类型
     * @return true: 匹配，当前执行器可以执行该类型的节点
     */
    boolean isMatchedExecutor(SoServiceNodeType serviceNodeType);

    /**
     * <h3>执行 API 节点配置</h3>
     *
     * @param apiConfig      节点配置
     * @param request        http request
     * @param response       http response
     * @param currentApiData 当前节点数据
     * @return 执行结果
     */
    T execute(SoServiceNode apiConfig, HttpServletRequest request, HttpServletResponse response, SoExecNodeServiceData currentApiData);
}
