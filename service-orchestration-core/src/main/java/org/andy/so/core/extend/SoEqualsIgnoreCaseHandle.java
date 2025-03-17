package org.andy.so.core.extend;

import org.andy.so.core.SoConditionHandle;
import org.andy.so.core.schema.enums.SoConditionTypeEnum;
import org.andy.so.core.util.SoClassUtil;
import org.andy.so.core.util.SoStringUtil;

/**
 * <h2>忽略大小写的 equals 实现</h2>
 *
 * @author: andy
 */
public class SoEqualsIgnoreCaseHandle implements SoConditionHandle {
    @Override
    public String getHandleName() {
        return SoConditionTypeEnum.EQUAL_IGNORE_CASE.name();
    }

    @Override
    public <P> Boolean compare(P sourceData, P[] conditionData, Class<P> dataType) {
        if (SoClassUtil.isBlack(sourceData)) {
            return conditionData == null || SoStringUtil.isBlank(String.valueOf(conditionData[0]));
        }
        if (conditionData == null || conditionData.length == 0) {
            return false;
        }
        return String.valueOf(sourceData).equalsIgnoreCase(String.valueOf(conditionData[0]));
    }
}
