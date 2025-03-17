package org.andy.so.core.help;

import org.andy.so.core.schema.enums.SoServiceNodeChildTypeEnum;
import org.andy.so.core.schema.node.SoPropertyNode;
import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoExecNodeServiceData;

import java.util.Map;

/**
 * <h2>数据转换帮助类，通过 JsonPath 方式处理数据转换</h2>
 *
 * @author: andy
 */
public interface SoParamConvertHelper {
    /**
     * 根目录标识
     */
    String ROOT_START_CHAR = "$";
    /**
     * 数组类型起始符
     */
    String ARRAY_START_CHAR = "[";
    /**
     * 数组类型截止符
     */
    String ARRAY_END_CHAR = "]";
    /**
     * 在数组中则表示添加数据
     */
    String ARRAY_ANY_CHAR = "*";
    /**
     * 两个点 ".." 则表示遍历
     */
    String ARRAY_ITERATOR_CHAR = "..";


    /**
     * <h3>从配置数据中转换字段</h3>
     *
     * @param apiConfig         api 配置
     * @param currentApiData    当前 api 数据
     * @param defaultData       默认数据源
     * @param apiServiceDataMap 全局数据源
     * @param apiChildNodeType  子节点类型
     */
    void convertPropFromConf(SoServiceNode apiConfig,
                             SoExecNodeServiceData currentApiData,
                             SoExecNodeServiceData defaultData,
                             Map<String, SoExecNodeServiceData> apiServiceDataMap,
                             SoServiceNodeChildTypeEnum apiChildNodeType);

    /**
     * <h3>从 source 中找到 value 值</h3>
     *
     * @param node              字段映射节点
     * @param defaultData       默认数据源
     * @param apiServiceDataMap 全局数据源
     * @param apiChildNodeType  子节点类型
     * @return 从数据源中找到的值
     */
    Object findValueFromSource(SoPropertyNode node, SoExecNodeServiceData defaultData,
                               Map<String, SoExecNodeServiceData> apiServiceDataMap,
                               SoServiceNodeChildTypeEnum apiChildNodeType);
}
