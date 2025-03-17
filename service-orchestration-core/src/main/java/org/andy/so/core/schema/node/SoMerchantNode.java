package org.andy.so.core.schema.node;

import org.andy.so.core.SoApiCommonHandler;
import org.andy.so.core.util.SoStringUtil;

import java.util.List;

/**
 * <h2>标准接口配置节点</h2>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public class SoMerchantNode extends SoAbstractNode {
    /**
     * 商户编码
     */
    private String code;
    /**
     * 商户名称
     */
    private String name;
    /**
     * 接口路径
     */
    private String requestPath;
    /**
     * 请求数据处理
     */
    private String reqDataHandle;
    /**
     * 响应数据处理
     */
    private String respDataHandle;
    /**
     * 是否注册到 request mapping 中：
     * <p>
     * TRUE：默认值，即可以通过 {@link SoApiCommonHandler} 实现中的
     * {@code  @SoPost(path = "${so.merchant.api-root-path:}/{merchantCode}/{apiPath}")} 方式调用；
     * </p>
     * <p>
     * FALSE：不对外开放，只允许内部代码调用；
     * </p>
     */
    private boolean regRequestMapping = true;
    /**
     * 接口映射关系
     */
    private List<SoServiceNode> apiConfigList;
    /**
     * mock 数据
     */
    private SoMockNode mockNode;

    @Override
    public String getId() {
        if (SoStringUtil.isNotBlank(id)) {
            return id;
        }
        return getRequestPath();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getReqDataHandle() {
        return reqDataHandle;
    }

    public void setReqDataHandle(String reqDataHandle) {
        this.reqDataHandle = reqDataHandle;
    }

    public String getRespDataHandle() {
        return respDataHandle;
    }

    public void setRespDataHandle(String respDataHandle) {
        this.respDataHandle = respDataHandle;
    }

    public boolean isRegRequestMapping() {
        return regRequestMapping;
    }

    public void setRegRequestMapping(boolean regRequestMapping) {
        this.regRequestMapping = regRequestMapping;
    }

    public List<SoServiceNode> getApiConfigList() {
        return apiConfigList;
    }

    public void setApiConfigList(List<SoServiceNode> apiConfigList) {
        this.apiConfigList = apiConfigList;
    }

    public SoMockNode getMockNode() {
        return mockNode;
    }

    public void setMockNode(SoMockNode mockNode) {
        this.mockNode = mockNode;
    }

    @Override
    public String toString() {
        return "MerchantNode{" +
                "id='" + id + '\'' +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", requestPath='" + requestPath + '\'' +
                ", reqDataHandle='" + reqDataHandle + '\'' +
                ", respDataHandle='" + respDataHandle + '\'' +
                ", regRequestMapping=" + regRequestMapping +
                ", apiConfigList=" + apiConfigList +
                ", mockNode=" + mockNode +
                '}';
    }
}
