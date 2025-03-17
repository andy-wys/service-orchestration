package org.andy.so.core.extend;

import org.andy.so.core.SoConditionHandle;
import org.andy.so.core.schema.enums.SoConditionTypeEnum;
import org.andy.so.core.util.SoClassUtil;

/**
 * <h2>两个数值间的比较，闭区间：between ... and ...</h2>
 *
 * @author: andy
 */
public class SoBetweenDataHandle implements SoConditionHandle {
    @Override
    public String getHandleName() {
        return SoConditionTypeEnum.BETWEEN.name();
    }

    @Override
    public <P> Boolean compare(P sourceData, P[] conditionData, Class<P> dataType) {
        // 比较的数据数量
        int compareArrayLength = 2;
        if (SoClassUtil.isBlack(sourceData) || conditionData == null || conditionData.length != compareArrayLength) {
            return false;
        }

        int start = evaluate(sourceData, conditionData[0], dataType);
        if (start < 0) {
            return false;
        }
        int end = evaluate(sourceData, conditionData[1], dataType);
        return end < 1;
    }
}
