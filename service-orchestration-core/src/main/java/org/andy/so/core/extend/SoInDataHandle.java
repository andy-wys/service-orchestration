package org.andy.so.core.extend;

import org.andy.so.core.SoConditionHandle;
import org.andy.so.core.schema.enums.SoConditionTypeEnum;
import org.andy.so.core.util.SoClassUtil;

/**
 * <h2>是否包含逻辑处理：X in (...)</h2>
 *
 * @author: andy
 */
public class SoInDataHandle implements SoConditionHandle {
    @Override
    public String getHandleName() {
        return SoConditionTypeEnum.IN.name();
    }

    @Override
    public <P> Boolean compare(P sourceData, P[] conditionData, Class<P> dataType) {
        if (SoClassUtil.isBlack(sourceData)) {
            return conditionData == null || conditionData.length == 0;
        }
        if (conditionData == null || conditionData.length == 0) {
            return false;
        }
        // 源数据和比较的数据都不为 null，只要有一个匹配则返回 TRUE
        int compareResult;
        for (P data : conditionData) {
            compareResult = evaluate(sourceData, data, dataType);
            if (compareResult == 0) {
                return true;
            }
        }
        return false;
    }
}
