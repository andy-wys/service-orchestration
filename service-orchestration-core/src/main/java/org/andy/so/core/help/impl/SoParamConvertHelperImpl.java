package org.andy.so.core.help.impl;

import com.alibaba.fastjson2.*;
import org.andy.so.core.help.SoDataHandleHelper;
import org.andy.so.core.help.SoParamConvertHelper;
import org.andy.so.core.schema.enums.SoPropNodeTypeEnum;
import org.andy.so.core.schema.enums.SoServiceNodeChildTypeEnum;
import org.andy.so.core.schema.enums.SoSourceFromTypeEnum;
import org.andy.so.core.schema.node.SoPropertyNode;
import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoExecNodeServiceData;
import org.andy.so.core.util.SoStringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * <h2>数据转换帮助类，通过 JsonPath 方式处理数据转换</h2>
 *
 * @author: andy
 */
@SuppressWarnings("all")
public class SoParamConvertHelperImpl implements SoParamConvertHelper {
    private final Log log = LogFactory.getLog(getClass());
    /**
     * 数据处理器
     */
    SoDataHandleHelper dataHandleHelper;

    public SoParamConvertHelperImpl(SoDataHandleHelper dataHandleHelper) {
        this.dataHandleHelper = dataHandleHelper;
    }

    /**
     * 从配置数据中转换字段
     *
     * @param apiConfig         api 配置
     * @param currentApiData    当前 api 数据
     * @param defaultData       默认数据源
     * @param apiServiceDataMap 全局数据源
     * @param apiChildNodeType  子节点类型
     */
    @Override
    public void convertPropFromConf(SoServiceNode apiConfig,
                                    SoExecNodeServiceData currentApiData,
                                    SoExecNodeServiceData defaultData,
                                    Map<String, SoExecNodeServiceData> apiServiceDataMap,
                                    SoServiceNodeChildTypeEnum apiChildNodeType) {
        // 如果字段配置为 null，则直接使用默认的请求数据，相当于透传
        if (apiConfig.getPropMap() == null || CollectionUtils.isEmpty(apiConfig.getPropMap().get(apiChildNodeType.name()))) {
            if (apiChildNodeType == SoServiceNodeChildTypeEnum.REQ) {
                currentApiData.setReqBody(defaultData.getReqBody());
            } else if (apiChildNodeType == SoServiceNodeChildTypeEnum.RESP) {
                currentApiData.setRespBody(defaultData.getRespBody());
            }
            return;
        }

        List<SoPropertyNode> propList = apiConfig.getPropMap().get(apiChildNodeType.name());
        Object value;
        for (SoPropertyNode c : propList) {
            // 先找到 value 值
            value = findValueFromSource(c, defaultData, apiServiceDataMap, apiChildNodeType);
            if (value == null) {
                value = c.getDefaultValue();
            }
            // 再对 value 执行 handle
            value = dataHandleHelper.doConvert(
                    c.getDataHandle(),
                    value,
                    defaultData.getHttpServletRequest(),
                    defaultData.getHttpServletResponse(),
                    currentApiData
            );
            if (apiChildNodeType == SoServiceNodeChildTypeEnum.REQ) {
                setTargetPropValue(c, value, currentApiData, apiChildNodeType);
            } else if (apiChildNodeType == SoServiceNodeChildTypeEnum.RESP) {
                setTargetPropValue(c, value, currentApiData, apiChildNodeType);
            } else {
                setTargetPropValue(c, value, defaultData, apiChildNodeType);
            }
        }
    }

    /**
     * 从 source 中找到 value 值
     *
     * @param node              字段映射节点
     * @param defaultData       默认数据源
     * @param apiServiceDataMap 全局数据源
     * @param apiChildNodeType  子节点类型
     * @return 从数据源中找到的值
     */
    @Override
    public Object findValueFromSource(SoPropertyNode node, SoExecNodeServiceData defaultData,
                                      Map<String, SoExecNodeServiceData> apiServiceDataMap,
                                      SoServiceNodeChildTypeEnum apiChildNodeType) {
        if (SoStringUtil.isBlank(node.getSourceKey())) {
            return null;
        }
        // 引用的接口 id 为空，则从默认数据中取值
        SoExecNodeServiceData apiSourceData = SoStringUtil.isBlank(node.getRefApiId()) ? defaultData : apiServiceDataMap.get(node.getRefApiId());
        if (apiSourceData == null) {
            return null;
        }

        SoSourceFromTypeEnum sourceFromType = node.getSourceFrom();
        // 从 header 中取值
        if (SoSourceFromTypeEnum.HEADER == sourceFromType) {
            return apiSourceData.getHeaderValue(node.getSourceKey());
        }
        // 从 cookie 中取值
        else if (SoSourceFromTypeEnum.COOKIE == sourceFromType) {
            return apiSourceData.getCookieValue(node.getSourceKey());
        }
        // 从请求 REQ 或响应 RESP 中取
        if (sourceFromType == null) {
            if (apiChildNodeType == SoServiceNodeChildTypeEnum.RESP) {
                sourceFromType = SoSourceFromTypeEnum.RESP;
            } else {
                sourceFromType = SoSourceFromTypeEnum.REQ;
            }
        }
        Object sourceData = SoSourceFromTypeEnum.RESP == sourceFromType ? apiSourceData.getRespBody() : apiSourceData.getReqBody();
        if (sourceData == null || ROOT_START_CHAR.equals(node.getSourceKey())) {
            return sourceData;
        }
        try {
            return JSONPath.extract(JSON.toJSONString(sourceData), node.getSourceKey());
        } catch (JSONException e) {
            log.error("从 [" + JSON.toJSONString(sourceData)
                    + "] 中解析 [" + node.getSourceKey() + "] 时出现异常，将返回 null 值，请关注。", e);
        }
        return null;
    }

    /**
     * <h2>给目标字段赋值</h2>
     *
     * @param node             属性节点
     * @param value            目标值
     * @param currentApiData   当前数据源
     * @param apiChildNodeType api 子节点类型
     */
    private void setTargetPropValue(SoPropertyNode node, Object value, SoExecNodeServiceData currentApiData, SoServiceNodeChildTypeEnum apiChildNodeType) {
        if (SoStringUtil.isEmpty(node.getTargetKey())) {
            return;
        }
        // 设置 cookie
        if (node.getPropNodeType() == SoPropNodeTypeEnum.COOKIE) {
            if (value == null) {
                return;
            }
            if (currentApiData.getHttpCookies() == null) {
                currentApiData.setHttpCookies(new ArrayList<>());
            }
            currentApiData.getHttpCookies().add(node.getTargetKey() + "=" + value);
        } else if (node.getPropNodeType() == SoPropNodeTypeEnum.HEADER) {
            if (value == null) {
                return;
            }
            if (currentApiData.getHttpHeaders() == null) {
                currentApiData.setHttpHeaders(new HashMap<String, String>());
            }
            currentApiData.getHttpHeaders().put(node.getTargetKey(), String.valueOf(value));
        } else if (apiChildNodeType == SoServiceNodeChildTypeEnum.RESP) {
            Object respNode = parsePathValue(node.getTargetKey(), value, currentApiData.getRespBody());
            currentApiData.setRespBody(respNode);
        } else if (apiChildNodeType == SoServiceNodeChildTypeEnum.REQ) {
            Object reqNode = parsePathValue(node.getTargetKey(), value, currentApiData.getReqBody());
            currentApiData.setReqBody(reqNode);
        }
    }

    /**
     * <h2>按 jsonPath 将 value 值设置到 parentNode</h2>
     *
     * @param jsonPath   JSON Path
     * @param value      最终要设置的值
     * @param parentData 父级数据
     * @return 父节点
     * @since 1.2.0
     */
    private Object parsePathValue(String jsonPath, Object value, Object parentData) {
        // 如果路径为空，父对象数据为数组则作为元素添加，否则重置父对象数据
        if (SoStringUtil.isBlank(jsonPath)) {
            if (parentData instanceof Collection) {
                Collection collection = (Collection) parentData;
                collection.add(value);
            } else {
                parentData = value;
            }
            return parentData;
        }
        // “$” 则表示直接赋值
        else if (ROOT_START_CHAR.equals(jsonPath)) {
            parentData = value;
            return parentData;
        }

        int arrayStartIndex = jsonPath.indexOf(ARRAY_START_CHAR);
        int arrayEndIndex = arrayStartIndex == -1 ? -1 : jsonPath.indexOf(ARRAY_END_CHAR, arrayStartIndex);
        // 路径中没有数组类型，则直接按路径赋值即可
        if (parentData == null) {
            parentData = new JSONObject();
        }
        if (arrayStartIndex == -1 || arrayEndIndex == -1) {
            JSONPath.of(jsonPath).set(parentData, value);
            return parentData;
        }

        return parseArrayPathValue(jsonPath, arrayStartIndex, arrayEndIndex, value, parentData);
    }

    /**
     * <h2>当路径中有数组类型时的情况</h2>
     * 这里主要是为了解决 fastjson2 无法自动创建数组节点的 BUG
     *
     * @param jsonPath        json path
     * @param arrayStartIndex 数组起始下标
     * @param arrayEndIndex   数组结束下标
     * @param value           目标值
     * @param parentData      父节点数据
     * @return 结果数据
     * @since 1.2.0
     */
    private Object parseArrayPathValue(String jsonPath, int arrayStartIndex, int arrayEndIndex, Object value, Object parentData) {
        // 当前数组的路径，不包含符号[]
        String currentArrayPathKey = jsonPath.substring(0, arrayStartIndex);
        // 数组子元素的路径：$.xxx
        String subElementPathKey = subPathKey(jsonPath, arrayEndIndex + 1);

        JSONPath currJsonPathObj = JSONPath.of(currentArrayPathKey);
        // 当前数组对象
        JSONArray currArrayData = getOrNewArrayByPath(currJsonPathObj, parentData);

        // 以下将对当前数组对象 currArrayData 赋值
        // 判断是否有下标配置
        String indexStr = arrayEndIndex - arrayStartIndex == 1 ? null : jsonPath.substring(arrayStartIndex + 1, arrayEndIndex);
        // 数组下标为空则将该数据 value 添加到该数组中
        if (SoStringUtil.isBlank(indexStr)) {
            currArrayData.add(parsePathValue(subElementPathKey, value, null));
            return parentData;
        }
        // $.data.arr[*].xxx ，将 value 中的元素添加到数组中
        else if (ARRAY_ANY_CHAR.equals(indexStr)) {
            if (value instanceof Collection) {
                Collection values = (Collection) value;
                Iterator iterator = values.iterator();
                while (iterator.hasNext()) {
                    currArrayData.add(parsePathValue(subElementPathKey, iterator.next(), null));
                }
            } else {
                currArrayData.add(parsePathValue(subElementPathKey, value, null));
            }
        }
        // $.data.arr[..].xx，表示遍历数组元素
        else if (ARRAY_ITERATOR_CHAR.equals(indexStr)) {
            if (value instanceof Collection) {
                Collection values = (Collection) value;
                Iterator iterator = values.iterator();
                int i = 0;
                int collectionSize = currArrayData.size();
                while (iterator.hasNext()) {
                    if (i < collectionSize) {
                        currArrayData.set(i, parsePathValue(subElementPathKey, iterator.next(), currArrayData.get(i)));
                    } else {
                        currArrayData.set(i, parsePathValue(subElementPathKey, iterator.next(), null));
                    }
                    i++;
                }
            } else {
                // 该值对所有元素生效
                for (int i = 0; i < currArrayData.size(); i++) {
                    currArrayData.set(i, parsePathValue(subElementPathKey, value, currArrayData.get(i)));
                }
            }
        }
        // 为固定的下标元素赋值：$.data.arr[2].xxx
        else {
            int index = Integer.valueOf(indexStr);
            if (index < currArrayData.size()) {
                currArrayData.set(index, parsePathValue(subElementPathKey, value, currArrayData.get(index)));
            } else {
                currArrayData.set(index, parsePathValue(subElementPathKey, value, null));
            }
        }
        return parentData;
    }

    /**
     * 通过 json path 获取数组数据，没有则新建并设置到 parent data 中
     *
     * @param currJsonPathObj JSONPath 对象
     * @param parentData      根数据
     * @return path 对应的数组数据
     * @since 1.2.0
     */
    private JSONArray getOrNewArrayByPath(JSONPath currJsonPathObj, Object parentData) {
        Object currPathData = currJsonPathObj.eval(parentData);
        JSONArray currArrayData;
        // 如果当前数组为空则设置一个空数组，这里主要是为了解决 fastjson2 无法自动创建数组节点的 BUG
        if (currPathData instanceof JSONArray) {
            currArrayData = (JSONArray) currPathData;
        } else if (currPathData instanceof Collection) {
            currArrayData = new JSONArray((Collection) currPathData);
        } else {
            currArrayData = new JSONArray(4);
            currJsonPathObj.set(parentData, currArrayData);
        }
        return currArrayData;
    }

    /**
     * 截取子元素的 path key
     *
     * @param jsonPath   json path
     * @param startIndex 开始截取下标
     * @return 子元素 path，不符合截取条件则返回 null
     */
    private String subPathKey(String jsonPath, int startIndex) {
        if (startIndex > 0 && SoStringUtil.isNotBlank(jsonPath) && startIndex < jsonPath.length()) {
            return ROOT_START_CHAR + jsonPath.substring(startIndex);
        }
        return null;
    }
}
