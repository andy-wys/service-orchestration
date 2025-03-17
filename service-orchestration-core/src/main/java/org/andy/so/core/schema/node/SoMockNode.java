package org.andy.so.core.schema.node;

import org.andy.so.core.schema.enums.SoMockTypeEnum;

/**
 * <h2>mock 数据节点</h2>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public class SoMockNode {
    /**
     * mock 类型
     */
    private SoMockTypeEnum type;
    /**
     * mock 开关
     */
    private boolean enable;
    /**
     * 数据文件名称
     */
    private String fileName;
    /**
     * 数据处理器
     *
     * @since 1.4.0
     */
    private String dataHandle;

    public SoMockTypeEnum getType() {
        return type;
    }

    public void setType(SoMockTypeEnum type) {
        this.type = type;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDataHandle() {
        return dataHandle;
    }

    public void setDataHandle(String dataHandle) {
        this.dataHandle = dataHandle;
    }

    @Override
    public String toString() {
        return "MockNode{" +
                "type=" + type +
                ", enable=" + enable +
                ", fileName='" + fileName + '\'' +
                ", dataHandle='" + dataHandle + '\'' +
                '}';
    }
}
