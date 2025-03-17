package org.andy.so.core.schema;

import org.andy.so.core.schema.node.SoMockNode;
import org.w3c.dom.Node;

/**
 * <h2>mock xml 节点解析器定义</h2>
 *
 * @author: andy
 */
public interface SoMockNodeParser {

    /**
     * <h3>解析 mock 节点属性并构建 {@link SoMockNode} 对象</h3>
     *
     * @param node mock xml 节点
     * @return MockNode 节点数据对象
     */
    SoMockNode parseMockNode(Node node);
}
