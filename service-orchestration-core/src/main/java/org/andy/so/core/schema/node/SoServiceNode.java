package org.andy.so.core.schema.node;


import org.andy.so.core.schema.SoServiceNodeType;
import org.andy.so.core.schema.enums.SoApiCharsetEnum;
import org.andy.so.core.schema.enums.SoApiContentTypeEnum;
import org.andy.so.core.schema.enums.SoApiMethodEnum;
import org.andy.so.core.schema.enums.SoServiceExecBlockedTypeEnum;
import org.andy.so.core.service.SoServiceNodeExecutor;
import org.andy.so.core.util.SoStringUtil;

import java.util.List;
import java.util.Map;

/**
 * <h2>接口映射配置节点</h2>
 *
 * @author: andy
 */
public class SoServiceNode extends SoAbstractNode {
    /**
     * 节点类型
     */
    private SoServiceNodeType nodeType;
    /**
     * 接口请求地址
     */
    private String serviceId;
    /**
     * 请求顺序
     */
    private int order;
    /**
     * 请求超时时间
     */
    private int timeout;
    /**
     * 阻断类型
     *
     * @see SoServiceExecBlockedTypeEnum
     */
    private SoServiceExecBlockedTypeEnum blocked;
    /**
     * 请求方式，默认 POST
     *
     * @see SoApiMethodEnum
     */
    private String method;
    /**
     * 默认 application/json
     *
     * @see SoApiContentTypeEnum
     */
    private SoApiContentTypeEnum contentType = SoApiContentTypeEnum.JSON;
    /**
     * 字符编码，默认 UTF-8
     *
     * @see SoApiCharsetEnum
     */
    private SoApiCharsetEnum charset = SoApiCharsetEnum.UTF8;
    /**
     * 请求参数映射关系
     * propName -> {targetKey -> source 字段列表}
     */
    private Map<String, List<SoPropertyNode>> propMap;
    /**
     * 接口数据处理
     */
    private Map<String, String> handleMap;
    /**
     * mock 数据
     */
    private SoMockNode mockNode;
    /**
     * 指定当前节点执行的实现类，该实现类必须实现 {@link SoServiceNodeExecutor} 接口
     * <p>
     * <b>这里是为了给使用者提供一个自定义实现的扩展方法，如果用户不指定，则会根据节点类型查找默认的实现</b>
     * </p>
     *
     * @since 1.4.0
     */
    private String executor;

    /**
     * 获取当前 api 的处理类
     *
     * @param key api 子节点（req,resp,header,cookie）
     * @return handle 类型
     */
    public String getApiHandle(String key) {
        if (SoStringUtil.isBlank(key) || handleMap == null) {
            return null;
        }
        return handleMap.get(key.toLowerCase());
    }

    public SoServiceNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(SoServiceNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public SoServiceExecBlockedTypeEnum getBlocked() {
        return blocked;
    }

    public void setBlocked(SoServiceExecBlockedTypeEnum blocked) {
        this.blocked = blocked;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public SoApiContentTypeEnum getContentType() {
        return contentType;
    }

    public void setContentType(SoApiContentTypeEnum contentType) {
        this.contentType = contentType;
    }

    public SoApiCharsetEnum getCharset() {
        return charset;
    }

    public void setCharset(SoApiCharsetEnum charset) {
        this.charset = charset;
    }

    public Map<String, List<SoPropertyNode>> getPropMap() {
        return propMap;
    }

    public void setPropMap(Map<String, List<SoPropertyNode>> propMap) {
        this.propMap = propMap;
    }

    public Map<String, String> getHandleMap() {
        return handleMap;
    }

    public void setHandleMap(Map<String, String> handleMap) {
        this.handleMap = handleMap;
    }

    public SoMockNode getMockNode() {
        return mockNode;
    }

    public void setMockNode(SoMockNode mockNode) {
        this.mockNode = mockNode;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    @Override
    public String toString() {
        return "ServiceNode{" +
                "id='" + id + '\'' +
                ", nodeType=" + (nodeType == null ? null : nodeType.getServiceNodeType()) +
                ", serviceId='" + serviceId + '\'' +
                ", order=" + order +
                ", timeout=" + timeout +
                ", blocked=" + blocked +
                ", method='" + method + '\'' +
                ", contentType=" + contentType +
                ", charset=" + charset +
                ", propMap=" + propMap +
                ", handleMap=" + handleMap +
                ", mockNode=" + mockNode +
                ", executor='" + executor + '\'' +
                '}';
    }
}
