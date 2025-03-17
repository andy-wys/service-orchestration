package org.andy.so.core;

import com.alibaba.fastjson2.JSON;
import org.andy.so.core.schema.SoSchemaConstant;
import org.andy.so.core.util.SoClassUtil;
import org.andy.so.core.util.SoStringUtil;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * <h2>条件数据处理器，返回值为布尔类型</h2>
 *
 * @author: andy
 */
public interface SoConditionHandle extends SoDataHandle<Object, Boolean> {
    /**
     * <h2>比较并返回条件执行结果</h2>
     *
     * @param sourceData    要比较的源数据
     * @param conditionData 要比较的目标条件值
     * @param dataType      比较的数据类型
     * @param <P>           参数泛型
     * @return 匹配结果：{@code true} 条件满足，{@code false} 条件不满足
     */
    <P> Boolean compare(P sourceData, P[] conditionData, Class<P> dataType);

    /**
     * <h2>转换处理方法</h2>
     *
     * @param sourceData 原数据
     * @param params     参数数组 [0]: 要比较的数据；[1]: 数据类型
     * @return 处理后的结果，必须是 {@code Boolean} 类型
     */
    @Override
    @SuppressWarnings("all")
    default Boolean doConvert(Object sourceData, Object... params) {
        Class dataType = null;
        String compareStr = null;
        if (params != null) {
            if (params.length > 0 && params[0] != null) {
                compareStr = String.valueOf(params[0]);
            }
            if (params.length > 1 && params[1] != null) {
                try {
                    dataType = Class.forName(String.valueOf(params[1]));
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
        if (dataType == null) {
            dataType = String.class;
        }

        if (SoStringUtil.isBlank(compareStr)) {
            return compare(JSON.to(dataType, sourceData), null, dataType);
        }

        List<Object> list = new LinkedList<>();
        for (String str : compareStr.split(SoSchemaConstant.SPLIT_CHAR_CONFIG)) {
            list.add(JSON.to(dataType, str.trim()));
        }
        return compare(JSON.to(dataType, sourceData), list.toArray(), dataType);
    }

    /**
     * <h2>计算两个元素，如果是数值则做大小比较，否则执行 equals 方法</h2>
     *
     * <pre>sourceData &gt; targetData : 1</pre>
     * <pre>sourceData = targetData : 0</pre>
     * <pre>sourceData &lt; targetData : -1</pre>
     *
     * @param sourceData 源数据
     * @param targetData 目标值
     * @param dataType   数据类型
     * @param <P>        要比较的两个数值的 java 类型
     * @return 计算结果：-1,0,1
     */
    default <P> int evaluate(P sourceData, P targetData, Class<P> dataType) {
        if (sourceData == targetData || String.valueOf(sourceData).trim().equals(String.valueOf(targetData).trim())) {
            return 0;
        }
        if (SoClassUtil.isBlack(sourceData)) {
            return -1;
        } else if (SoClassUtil.isBlack(targetData)) {
            return 1;
        }
        boolean isNumeric = SoClassUtil.isNumberType(dataType) ||
                (SoClassUtil.isNumeric(String.valueOf(sourceData).trim())
                        && SoClassUtil.isNumeric(String.valueOf(targetData).trim()));
        if (isNumeric) {
            BigDecimal b1 = new BigDecimal(String.valueOf(sourceData).trim());
            BigDecimal b2 = new BigDecimal(String.valueOf(targetData).trim());
            return b1.compareTo(b2);
        }
        return sourceData.equals(targetData) ? 0 : -1;
    }
}
