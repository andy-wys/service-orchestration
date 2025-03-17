package org.andy.so.core.schema.node;

import org.andy.so.core.schema.enums.SoPropNodeTypeEnum;
import org.andy.so.core.schema.enums.SoSourceFromTypeEnum;
import org.andy.so.core.schema.enums.SoUnionTypeEnum;

/**
 * <h2>不同协议子节点属性配置</h2>
 *
 * @author: andy
 */
public class SoPropertyNode extends SoAbstractNode {
    /**
     * 属性节点的类型
     */
    private SoPropNodeTypeEnum propNodeType;
    /**
     * 源字段
     */
    private String sourceKey;
    /**
     * 目标字段
     */
    private String targetKey;
    /**
     * 默认值
     */
    private String defaultValue;
    /**
     * 数据处理
     */
    private String dataHandle;
    /**
     * 源数据来源
     */
    private SoSourceFromTypeEnum sourceFrom;
    /**
     * 源数据引用的接口
     */
    private String refApiId;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 数据连接类型
     */
    private SoUnionTypeEnum unionType;

    public SoPropNodeTypeEnum getPropNodeType() {
        return propNodeType;
    }

    public void setPropNodeType(SoPropNodeTypeEnum propNodeType) {
        this.propNodeType = propNodeType;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDataHandle() {
        return dataHandle;
    }

    public void setDataHandle(String dataHandle) {
        this.dataHandle = dataHandle;
    }

    public SoSourceFromTypeEnum getSourceFrom() {
        return sourceFrom;
    }

    public void setSourceFrom(SoSourceFromTypeEnum sourceFrom) {
        this.sourceFrom = sourceFrom;
    }

    public String getRefApiId() {
        return refApiId;
    }

    public void setRefApiId(String refApiId) {
        this.refApiId = refApiId;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public SoUnionTypeEnum getUnionType() {
        return unionType;
    }

    public void setUnionType(SoUnionTypeEnum unionType) {
        this.unionType = unionType;
    }

    @Override
    public String toString() {
        return "PropertyNode{" +
                "id='" + id + '\'' +
                ", propNodeType=" + propNodeType +
                ", sourceKey='" + sourceKey + '\'' +
                ", targetKey='" + targetKey + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", dataHandle='" + dataHandle + '\'' +
                ", sourceFrom=" + sourceFrom +
                ", refApiId='" + refApiId + '\'' +
                ", dataType='" + dataType + '\'' +
                ", unionType=" + unionType +
                '}';
    }
}
