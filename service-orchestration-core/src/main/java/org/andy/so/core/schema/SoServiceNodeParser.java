package org.andy.so.core.schema;

import org.andy.so.core.schema.node.SoMerchantNode;
import org.andy.so.core.schema.node.SoServiceNode;
import org.w3c.dom.Node;

/**
 * <h2>XML 服务节点解析</h2>
 *
 * @author: andy
 */
public interface SoServiceNodeParser {

    /**
     * <h3>节点是否能使用该解析器</h3>
     *
     * @param node xml 节点
     * @return true 该节点能使用该解析器解析
     */
    boolean isMatchedServiceParser(Node node);

    /**
     * <h3>将 node 节点解析成服务配置的 java SoApiConfig 对象，结构示例：</h3>
     * <pre>
     * &lt;so:api attrName="attrValue"&gt;
     *     &lt;so:req&gt;
     *          &lt;so:property attrName="attrValue"/&gt;
     *     &lt;/so:req&gt;
     *     &lt;so:resp&gt;
     *          &lt;so:property attrName="attrValue"/&gt;
     *     &lt;/so:resp&gt;
     *     &lt;so:header&gt;
     *          &lt;so:property attrName="attrValue"/&gt;
     *     &lt;/so:header&gt;
     *     &lt;so:cookie&gt;
     *          &lt;so:property attrName="attrValue"/&gt;
     *     &lt;/so:cookie&gt;
     * &lt;/so:api&gt;
     * &lt;so:api attrName="attrValue"&gt;
     * &lt;/so:api&gt;
     * </pre>
     *
     * @param node xml 节点
     * @return {@link SoMerchantNode} 配置元素 apiConfigList
     */
    SoServiceNode parseServiceNode(Node node);
}
