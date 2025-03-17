package org.andy.so.core.schema;

import org.andy.so.core.SoEnvironmentAware;
import org.andy.so.core.SoMerchantFactory;
import org.andy.so.core.schema.enums.*;
import org.andy.so.core.schema.node.SoMerchantNode;
import org.andy.so.core.schema.node.SoMockNode;
import org.andy.so.core.schema.node.SoPropertyNode;
import org.andy.so.core.schema.node.SoServiceNode;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <h2>标准接口配置自定义 spring bean 转换</h2>
 * XML 解析，解析器可以通过反射动态化，但此处使用 getter setter 的方式，原因如下：
 * <ul>
 *     <li>配置属性和层级必须要固定化，不能乱写，否则就在启动时直接解析报错</li>
 *     <li>通过 get 和 set 的方式更直观一些，便于更改</li>
 *     <li>一级节点限制为 merchant，如果层级不对，则直接不用解析了</li>
 *     <li>这里是在项目启动时加载，所以不需要考虑线程安全问题</li>
 * </ul>
 *
 * @author: andy
 */

public class SoMerchantBeanDefinitionParser extends AbstractSingleBeanDefinitionParser implements SoServiceNodeParser, SoMockNodeParser {
    /**
     * <h3>生成 ID，结构为：merchant_{merchantCode}_{requestPath}</h3>
     *
     * @param element       merchant 节点
     * @param definition    bean definition
     * @param parserContext 上下文
     * @return ID
     * @throws BeanDefinitionStoreException bean 定义异常
     */
    @Override
    @NonNull
    public String resolveId(Element element, @NonNull AbstractBeanDefinition definition, @NonNull ParserContext parserContext)
            throws BeanDefinitionStoreException {
        return element.getTagName()
                + "_"
                + SoEnvironmentAware.replacePlaceholderValue(element.getAttribute("code"))
                + "_"
                + SoEnvironmentAware.replacePlaceholderValue(element.getAttribute("requestPath"));
    }

    @Override
    protected Class<?> getBeanClass(@NonNull Element element) {
        return SoMerchantNode.class;
    }

    /**
     * <h3>so:merchant 节点的入口配置，XML 结构如下：</h3>
     * <pre>
     * &lt;so:merchant attrName="attrValue"&gt;
     *     &lt;so:api attrName="attrValue"&gt;
     *         &lt;so:req&gt;
     *              &lt;so:property attrName="attrValue"/&gt;
     *         &lt;/so:req&gt;
     *         &lt;so:resp&gt;
     *              &lt;so:property attrName="attrValue"/&gt;
     *         &lt;/so:resp&gt;
     *         &lt;so:header&gt;
     *              &lt;so:property attrName="attrValue"/&gt;
     *         &lt;/so:header&gt;
     *         &lt;so:cookie&gt;
     *              &lt;so:property attrName="attrValue"/&gt;
     *         &lt;/so:cookie&gt;
     *     &lt;/so:api&gt;
     *     &lt;so:api attrName="attrValue"&gt;
     *     &lt;/so:api&gt;
     * &lt;/so:merchant&gt;
     * </pre>
     *
     * @param element merchant 节点
     * @param builder BeanDefinitionBuilder
     */
    @Override
    protected void doParse(@NonNull Element element, @NonNull BeanDefinitionBuilder builder) {
        addBuilderAttributes(element, builder,
                new String[]{"id", "code", "name", "requestPath", "reqDataHandle", "respDataHandle", "regRequestMapping"});

        // 找到 <merchant> 节点中的节点列表，循环创建 SoApiConfig 对象
        NodeList nodeList = element.getChildNodes();
        if (nodeList.getLength() == 0) {
            return;
        }
        List<SoServiceNode> apiConfigList = new LinkedList<>();
        Node node;
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++) {
            node = nodeList.item(i);
            SoServiceNodeTypeEnum nodeTypeEnum = SoServiceNodeTypeEnum.of(node.getLocalName());
            if (SoServiceNodeTypeEnum.MOCK == nodeTypeEnum) {
                SoMockNode mockNode = parseMockNode(node);
                // mock 开启的时候才加入，以此减少内存的使用
                if (mockNode.isEnable()) {
                    builder.addPropertyValue("mockNode", mockNode);
                }
            } else if (this.isMatchedServiceParser(node)) {
                apiConfigList.add(parseServiceNode(node));
            } else {
                SoServiceNodeParser soServiceNodeParser = SoMerchantFactory.getServiceNodeParser(node);
                if (soServiceNodeParser == null) {
                    continue;
                }
                SoServiceNode serviceNode = soServiceNodeParser.parseServiceNode(node);
                if (serviceNode != null) {
                    apiConfigList.add(serviceNode);
                }
            }
        }
        builder.addPropertyValue("apiConfigList", apiConfigList);
    }

    @Override
    public boolean isMatchedServiceParser(Node node) {
        return node != null
                && SoSchemaConstant.NAME_SPACE.equals(node.getNamespaceURI())
                && SoServiceNodeTypeEnum.of(node.getLocalName()) != null;
    }

    @Override
    public SoServiceNode parseServiceNode(Node apiNode) {
        SoServiceNode apiConfig = new SoServiceNode();
        apiConfig.setNodeType(SoServiceNodeTypeEnum.of(apiNode.getLocalName()));
        // api 节点属性解析
        parseApiAttr(apiNode.getAttributes(), apiConfig);
        // api 子节点解析
        NodeList childNodes = apiNode.getChildNodes();

        int childLen = childNodes.getLength();
        for (int i = 0; i < childLen; i++) {
            SoServiceNodeChildTypeEnum apiPropTypeEnum = SoServiceNodeChildTypeEnum.of(childNodes.item(i).getLocalName());
            if (SoServiceNodeChildTypeEnum.MOCK == apiPropTypeEnum) {
                SoMockNode mockNode = parseMockNode(childNodes.item(i));
                if (mockNode.isEnable()) {
                    apiConfig.setMockNode(mockNode);
                }
            } else if (apiPropTypeEnum != null) {
                parseApiChildNode(childNodes.item(i), apiConfig, apiPropTypeEnum);
            }
        }
        return apiConfig;
    }

    @Override
    public SoMockNode parseMockNode(Node node) {
        NamedNodeMap namedNodeMap = node.getAttributes();
        if (namedNodeMap.getLength() == 0) {
            return null;
        }
        SoMockNode mockNode = new SoMockNode();
        int len = namedNodeMap.getLength();
        Node attrNode;
        for (int i = 0; i < len; i++) {
            attrNode = namedNodeMap.item(i);
            String nodeValue = SoEnvironmentAware.replacePlaceholderValue(attrNode.getNodeValue());
            switch (attrNode.getLocalName()) {
                case "enable":
                    mockNode.setEnable(Boolean.parseBoolean(nodeValue));
                    break;
                case "type":
                    mockNode.setType(SoMockTypeEnum.of(nodeValue));
                    break;
                case "file":
                    mockNode.setFileName(nodeValue);
                    break;
                case "dataHandle":
                    mockNode.setDataHandle(nodeValue);
                    break;
                default:
                    break;
            }
        }
        return mockNode;
    }

    /**
     * bean 属性赋值
     *
     * @param element   节点
     * @param builder   bean definition builder
     * @param attrArray 属性字段
     */
    private void addBuilderAttributes(Element element, BeanDefinitionBuilder builder, String[] attrArray) {
        for (String attr : attrArray) {
            builder.addPropertyValue(attr, SoEnvironmentAware.replacePlaceholderValue(element.getAttribute(attr)));
        }
    }


    /**
     * api 节点的属性解析，
     * 这里可以使用反射赋值，此处先用 getter setter 实现
     *
     * @param namedNodeMap api 节点的属性集合
     * @param apiConfig    目标赋值对象
     */
    private void parseApiAttr(NamedNodeMap namedNodeMap, SoServiceNode apiConfig) {
        if (apiConfig == null || namedNodeMap == null || namedNodeMap.getLength() == 0) {
            return;
        }
        int len = namedNodeMap.getLength();
        Node attrNode;
        for (int i = 0; i < len; i++) {
            attrNode = namedNodeMap.item(i);
            String nodeValue = SoEnvironmentAware.replacePlaceholderValue(attrNode.getNodeValue());
            if (!StringUtils.hasLength(nodeValue)) {
                continue;
            }
            String nodeLocalName = attrNode.getLocalName();
            switch (nodeLocalName) {
                case "url":
                case "consumerId":
                case "beanId":
                case "requestPath":
                    apiConfig.setServiceId(nodeValue);
                    break;
                case "order":
                    apiConfig.setOrder(Integer.parseInt(nodeValue));
                    break;
                case "timeout":
                    apiConfig.setTimeout(Integer.parseInt(nodeValue));
                    break;
                case "blocked":
                    apiConfig.setBlocked(SoServiceExecBlockedTypeEnum.of(nodeValue));
                    break;
                case "method":
                case "merchantCode":
                    apiConfig.setMethod(nodeValue);
                    break;
                case "contentType":
                    apiConfig.setContentType(SoApiContentTypeEnum.of(nodeValue));
                    break;
                case "charset":
                    apiConfig.setCharset(SoApiCharsetEnum.of(nodeValue));
                    break;
                case "id":
                    apiConfig.setId(nodeValue);
                    break;
                // @since 1.4.0
                case "executor":
                    apiConfig.setExecutor(nodeValue);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 解析 api 子节点：req、resp、header、cookie，结构如下：
     * <pre>
     * &lt;so:propName&gt;
     *      &lt;so:property attrName="attrValue"/&gt;
     * &lt;/so:propName&gt;
     * </pre>
     *
     * @param node            api 子节点列表
     * @param apiConfig       要赋值的目标对象
     * @param apiPropTypeEnum 节点类型
     */
    private void parseApiChildNode(Node node, SoServiceNode apiConfig, SoServiceNodeChildTypeEnum apiPropTypeEnum) {
        String localName = apiPropTypeEnum.name();
        // 解析 req 级别的属性
        parseApiChildNodeAttr(node, apiConfig);

        // 解析 req 级别下的子节点
        NodeList propChildNodes = node.getChildNodes();
        if (propChildNodes.getLength() == 0) {
            return;
        }

        // 初始化 api 配置字段集合
        List<SoPropertyNode> propConfMap = initAndGetPropMap(apiConfig, localName);

        int propLen = propChildNodes.getLength();
        Node propNode;
        for (int i = 0; i < propLen; i++) {
            propNode = propChildNodes.item(i);

            // 初始化字段对象
            SoPropertyNode propConfig = initApiPropConfig(propNode, apiPropTypeEnum);

            // 将字段对象设置到集合中
            if (propConfig == null) {
                continue;
            }
            propConfMap.add(propConfig);
        }
    }

    /**
     * 解析 api 子节点的属性
     *
     * @param node      api 子节点(req,resp,header,cookie)
     * @param apiConfig api 节点配置
     */
    private void parseApiChildNodeAttr(Node node, SoServiceNode apiConfig) {
        // 放 handle 的映射类
        Map<String, String> handleMap = apiConfig.getHandleMap();
        if (handleMap == null) {
            handleMap = new HashMap<>(8);
            apiConfig.setHandleMap(handleMap);
        }
        NamedNodeMap namedNodeMap = node.getAttributes();
        if (namedNodeMap.getLength() == 0) {
            return;
        }
        int len = namedNodeMap.getLength();
        Node attrNode;
        String attrName;
        for (int i = 0; i < len; i++) {
            attrNode = namedNodeMap.item(i);
            attrName = attrNode.getLocalName();
            if ("dataHandle".equals(attrName)) {
                handleMap.put(node.getLocalName(), SoEnvironmentAware.replacePlaceholderValue(attrNode.getNodeValue()));
            }
        }
    }

    /**
     * 初始化并获取属性映射集合
     *
     * @param apiConfig api 实例
     * @param localName 字段名称 == localName
     */
    private List<SoPropertyNode> initAndGetPropMap(SoServiceNode apiConfig, String localName) {
        Map<String, List<SoPropertyNode>> propMap = apiConfig.getPropMap();
        // key = REQ/RESP
        if (propMap == null) {
            propMap = new HashMap<>(4);
            apiConfig.setPropMap(propMap);
        }
        return propMap.computeIfAbsent(localName, k -> new LinkedList<>());
    }

    /**
     * 初始化 property 节点对象
     *
     * @param node            节点对象
     * @param apiPropTypeEnum 属性节点类型
     * @return SoPropertyNode
     */
    private SoPropertyNode initApiPropConfig(Node node, SoServiceNodeChildTypeEnum apiPropTypeEnum) {
        SoPropNodeTypeEnum propNodeType = SoPropNodeTypeEnum.of(node.getLocalName());
        if (propNodeType == null) {
            return null;
        }
        NamedNodeMap namedNodeMap = node.getAttributes();
        if (namedNodeMap.getLength() == 0) {
            return null;
        }
        SoPropertyNode propConfig = new SoPropertyNode();
        propConfig.setPropNodeType(propNodeType);
        int len = namedNodeMap.getLength();
        Node attrNode;
        String attrName;
        String attrValue;
        for (int i = 0; i < len; i++) {
            attrNode = namedNodeMap.item(i);
            attrName = attrNode.getLocalName();
            attrValue = SoEnvironmentAware.replacePlaceholderValue(attrNode.getNodeValue());
            setApiPropConfigValue(propConfig, attrName, attrValue, apiPropTypeEnum);
        }
        return propConfig;
    }

    /**
     * 对 propConfig 对象赋值，这里可以使用反射
     *
     * @param propConfig      要赋值的实例
     * @param attrName        属性名称
     * @param attrValue       属性值
     * @param apiPropTypeEnum 服务节点类型
     */
    private void setApiPropConfigValue(SoPropertyNode propConfig, String attrName, String attrValue, SoServiceNodeChildTypeEnum apiPropTypeEnum) {
        switch (attrName) {
            case "sourceKey":
                propConfig.setSourceKey(attrValue);
                break;
            case "targetKey":
                propConfig.setTargetKey(attrValue);
                break;
            case "defaultValue":
            case "compareValue":
                propConfig.setDefaultValue(attrValue);
                break;
            case "dataHandle":
            case "compareHandle":
                if (StringUtils.hasLength(attrValue)) {
                    propConfig.setDataHandle(attrValue);
                }
                break;
            case "compareType":
                if (!StringUtils.hasLength(propConfig.getDataHandle())) {
                    propConfig.setDataHandle(attrValue);
                }
                break;
            case "sourceFrom":
                // 数据来源可以通过 XSD 来设置默认值，但是由于 XSD 做了抽象，如果单独设置默认值则重复的配置较多，所以此处通过代码方式设置
                SoSourceFromTypeEnum sourceFromTypeEnum = SoSourceFromTypeEnum.of(attrValue);
                if (sourceFromTypeEnum == null) {
                    if (apiPropTypeEnum == SoServiceNodeChildTypeEnum.RESP) {
                        sourceFromTypeEnum = SoSourceFromTypeEnum.RESP;
                    } else {
                        sourceFromTypeEnum = SoSourceFromTypeEnum.REQ;
                    }
                }
                propConfig.setSourceFrom(sourceFromTypeEnum);
                break;
            case "refApiId":
                propConfig.setRefApiId(attrValue);
                break;
            case "dataType":
                propConfig.setDataType(attrValue);
                break;
            case "union":
                propConfig.setUnionType(SoUnionTypeEnum.of(attrValue));
                break;
            default:
                break;
        }
    }
}
