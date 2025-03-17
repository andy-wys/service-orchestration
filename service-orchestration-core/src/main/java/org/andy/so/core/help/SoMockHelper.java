package org.andy.so.core.help;

import com.alibaba.fastjson2.JSONObject;
import org.andy.so.core.schema.enums.SoMockTypeEnum;
import org.andy.so.core.schema.node.SoMockNode;
import org.andy.so.core.service.SoExecNodeServiceData;

import javax.servlet.http.HttpServletResponse;

/**
 * <h2>mock 数据帮助类</h2>
 *
 * @author: andy
 */
public interface SoMockHelper {

    /**
     * <h3>是否 mock 响应数据</h3>
     *
     * @param mockNode mock 节点配置
     * @return true / false
     */
    boolean isMockRespEnable(SoMockNode mockNode);

    /**
     * <h3>是否 mock 请求数据</h3>
     *
     * @param mockNode mock 节点配置
     * @return true / false
     */
    boolean isMockReqEnable(SoMockNode mockNode);

    /**
     * <h3>mock 开关</h3>
     *
     * @param mockNode mock 节点配置
     * @param typeEnum mock 类型
     * @return true 则 mock 生效
     */
    boolean isMockEnable(SoMockNode mockNode, SoMockTypeEnum typeEnum);

    /**
     * <h3>转换 mock 的响应数据</h3>
     *
     * @param mockNode mock 配置节点
     * @param response HttpServletResponse
     * @return response data
     */
    Object parseMockResp(SoMockNode mockNode, HttpServletResponse response);

    /**
     * <h3>转换 mock 的执行条件</h3>
     *
     * @param mockNode mock 配置节点
     * @return true 允许执行，false 不执行
     */
    boolean parseMockCondition(SoMockNode mockNode);

    /**
     * <h3>解析 mock 的请求数据</h3>
     *
     * @param mockNode mock 配置
     * @return mock 的请求数据
     */
    Object parseMockReq(SoMockNode mockNode);

    /**
     * <h3>解析 mock 的 http 头并设置到 ApiServiceData 中</h3>
     *
     * @param mockNode       mock 配置
     * @param apiServiceData 目标值
     */
    void parseMockHeader(SoMockNode mockNode, SoExecNodeServiceData apiServiceData);

    /**
     * <h3>读取 mock 数据</h3>
     *
     * @param mockNode mock 配置
     * @return JSON object
     */
    JSONObject getMockFile(SoMockNode mockNode);
}
