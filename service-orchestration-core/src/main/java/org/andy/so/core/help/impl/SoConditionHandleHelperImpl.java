package org.andy.so.core.help.impl;

import org.andy.so.core.error.SoCheckErrorEnum;
import org.andy.so.core.error.SoCheckException;
import org.andy.so.core.help.SoConditionHandleHelper;
import org.andy.so.core.help.SoDataHandleHelper;
import org.andy.so.core.help.SoParamConvertHelper;
import org.andy.so.core.schema.enums.SoServiceNodeChildTypeEnum;
import org.andy.so.core.schema.enums.SoUnionTypeEnum;
import org.andy.so.core.schema.node.SoPropertyNode;
import org.andy.so.core.schema.node.SoServiceNode;
import org.andy.so.core.service.SoExecNodeServiceData;

import java.util.List;
import java.util.Map;

/**
 * <h2>条件处理帮助类</h2>
 *
 * @author: andy
 */
public class SoConditionHandleHelperImpl implements SoConditionHandleHelper {
    /**
     * <h3>参数转换帮助类，会真正执行参数节点的解析和执行</h3>
     */
    SoParamConvertHelper paramConvertHelper;
    /**
     * <h3>数据处理器帮助类，会查找并执行对应的数据处理器</h3>
     */
    SoDataHandleHelper dataHandleHelper;

    /**
     * 通过构造方法注入参数转换类 SoParamConvertHelper 和数据处理器 SoDataHandleHelper
     *
     * @param paramConvertHelper 参数转换帮助类
     * @param dataHandleHelper   数据处理器帮助类
     */
    public SoConditionHandleHelperImpl(SoParamConvertHelper paramConvertHelper, SoDataHandleHelper dataHandleHelper) {
        if (paramConvertHelper == null || dataHandleHelper == null) {
            throw new SoCheckException(SoCheckErrorEnum.INIT_SERVICE_ERROR, null, "条件处理器",
                    "参数转换类 SoParamConvertHelper 或数据处理器 SoDataHandleHelper 不能为 null");
        }
        this.paramConvertHelper = paramConvertHelper;
        this.dataHandleHelper = dataHandleHelper;
    }

    /**
     * 执行条件处理器
     *
     * @param apiConfig         condition 节点
     * @param defaultData       默认数据
     * @param apiServiceDataMap 全量数据
     * @return 条件是否通过 {@code true} 通过，{@code false} 则未通过
     */
    @Override
    public Boolean handleCondition(SoServiceNode apiConfig,
                                   SoExecNodeServiceData defaultData,
                                   Map<String, SoExecNodeServiceData> apiServiceDataMap) {
        if (apiConfig == null || apiConfig.getPropMap() == null) {
            return true;
        }

        List<SoPropertyNode> conditionList = apiConfig.getPropMap().get(SoServiceNodeChildTypeEnum.CONDITION.name());
        if (conditionList == null || conditionList.isEmpty()) {
            return true;
        }
        boolean result = true;
        Object value;
        for (SoPropertyNode node : conditionList) {
            if (!result && node.getUnionType() == SoUnionTypeEnum.AND) {
                continue;
            }
            value = paramConvertHelper.findValueFromSource(node, defaultData, apiServiceDataMap, SoServiceNodeChildTypeEnum.CONDITION);
            value = dataHandleHelper.doConvert(node.getDataHandle(), value, node.getDefaultValue(), node.getDataType());
            boolean nodeCondition;
            if (value instanceof Boolean) {
                nodeCondition = (Boolean) value;
            } else {
                nodeCondition = Boolean.parseBoolean(String.valueOf(value));
            }
            if (node.getUnionType() == SoUnionTypeEnum.AND) {
                result = result && nodeCondition;
            } else {
                result = result || nodeCondition;
            }
        }
        return result;
    }
}
