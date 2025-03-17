package org.andy.so.core.schema;

import org.andy.so.core.schema.enums.SoServiceNodeTypeEnum;

/**
 * <h2>服务节点类型</h2>
 * 通过接口规范服务类型的定义，避免单纯的 String 类型所带来的管理模糊和混乱的问题
 *
 * @author: andy
 * @see SoServiceNodeTypeEnum
 */
public interface SoServiceNodeType {

    /**
     * <h3>获取服务节点的类型</h3>
     * 框架默认的服务类型：{@link SoServiceNodeTypeEnum}
     *
     * @return String 标识
     */
    String getServiceNodeType();
}
