package org.andy.so.core.extend;

import org.andy.so.core.SoConditionHandle;
import org.andy.so.core.schema.enums.SoConditionTypeEnum;

/**
 * <h2>比较两个值，这里主要关注的是内容，而不是类型</h2>
 *
 * @author: andy
 */
public class SoCompareDataHandle implements SoConditionHandle {
    /**
     * 条件类型，不同类型对应不同的条件执行操作符
     */
    private final SoConditionTypeEnum conditionType;

    /**
     * 根据条件类型构造对象
     *
     * @param conditionType 条件类型
     */
    public SoCompareDataHandle(SoConditionTypeEnum conditionType) {
        this.conditionType = conditionType;
    }

    @Override
    public String getHandleName() {
        return conditionType.name();
    }

    @Override
    public <P> Boolean compare(P sourceData, P[] conditionData, Class<P> dataType) {
        int compareResult;
        if (conditionData == null || conditionData.length == 0) {
            compareResult = evaluate(sourceData, null, dataType);
        } else {
            compareResult = evaluate(sourceData, conditionData[0], dataType);
        }
        return genResult(compareResult);
    }

    /**
     * 根据条件执行结果构造最终条件是否成立结果
     *
     * @param compareResult 数据比较结果
     * @return {@code true}：条件成立；{@code false}：条件不成立
     */
    private Boolean genResult(int compareResult) {
        switch (conditionType) {
            case GREATER_THAN:
                return compareResult == 1;
            case GREATER_EQUAL:
                return compareResult >= 0;
            case LESS_THAN:
                return compareResult < 0;
            case LESS_EQUAL:
                return compareResult <= 0;
            case EQUAL:
            default:
                return compareResult == 0;
        }
    }
}