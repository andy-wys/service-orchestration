package org.andy.so.core.extend;

import com.alibaba.fastjson2.JSON;
import org.andy.so.core.SoDataHandle;
import org.andy.so.core.error.SoCheckErrorEnum;
import org.andy.so.core.error.SoCheckException;
import org.andy.so.core.util.SoParseDataUtil;
import org.andy.so.core.util.SoStringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <h2>转换数据类型处理器</h2>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public class SoParseDataTypeHandle implements SoDataHandle<Object, Object> {
    /**
     * 类型转换
     */
    public enum ParseType {
        /**
         * 转换成 String
         */
        TO_STRING,
        /**
         * 转换成 BigDecimal
         */
        TO_NUMBER,
        /**
         * 转换成 Boolean
         */
        TO_BOOLEAN,
        /**
         * 转换成 Integer
         */
        TO_INTEGER,
        /**
         * 转换成 Integer，向上取整
         */
        TO_INTEGER_CEIL,
        /**
         * 转换成 Integer，向下取整
         */
        TO_INTEGER_FLOOR,
        /**
         * 转换成 Integer，四舍五入
         */
        TO_INTEGER_ROUND,
        /**
         * 转换成金额表示 #.##
         */
        TO_MONEY,
        /**
         * 转换成金额表示，向上取整 #.##
         */
        TO_MONEY_CEIL,
        /**
         * 转换成金额表示，向下取整 #.##
         */
        TO_MONEY_FLOOR,
        /**
         * 转换成金额表示，四舍五入 #.##
         */
        TO_MONEY_ROUND,
        /**
         * 日期对象
         */
        TO_DATE,
        /**
         * 日期时间对象
         */
        TO_DATETIME,
        /**
         * 年月日格式，yyyy-MM-dd
         */
        TO_YYYY_MM_DD,
        /**
         * 年月日格式，yyyyMMdd
         */
        TO_YYYYMMDD,
        /**
         * 年月日 时分秒，yyyy-MM-dd HH:mm:ss
         */
        TO_YYYY_MM_DD_HH_MM_SS,
        /**
         * 年月日 时分秒，yyyyMMdd HHmmss
         */
        TO_YYYYMMDD_HHMMSS,
        /**
         * 年月日 时分秒，yyyyMMddHHmmss
         */
        TO_YYYYMMDDHHMMSS,
        /**
         * 时分秒，HHmmss
         */
        TO_HHMMSS,
        /**
         * 时分秒，HH:mm:ss
         */
        TO_HH_MM_SS,
    }

    private final static Log log = LogFactory.getLog(SoParseDataTypeHandle.class);
    /**
     * @see this#getHandleName()
     */
    private final String handleName;
    /**
     * 指定转换的目标类型
     */
    private Class<?> targetType;

    /**
     * @param parseType 转换类型
     */
    public SoParseDataTypeHandle(ParseType parseType) {
        if (parseType == null) {
            log.error(String.format(SoCheckErrorEnum.INIT_DATA_HANDLE_ERROR.getMessage(),
                    "初始化参数 ParseType 不能为 null"));
            throw new SoCheckException(SoCheckErrorEnum.INIT_DATA_HANDLE_ERROR);
        }
        this.handleName = parseType.name();
    }

    /**
     * @param targetType 目标数据类型
     */
    public SoParseDataTypeHandle(String handleName, Class<?> targetType) {
        if (SoStringUtil.isBlank(handleName) || targetType == null) {
            log.error(String.format(SoCheckErrorEnum.INIT_DATA_HANDLE_ERROR.getMessage(),
                    "初始化参数不能为 null，handleName = " + handleName + ", targetType = " + targetType));
            throw new SoCheckException(SoCheckErrorEnum.INIT_DATA_HANDLE_ERROR);
        }
        this.handleName = handleName;
        this.targetType = targetType;
    }

    @Override
    public String getHandleName() {
        return this.handleName;
    }

    @Override
    public Object doConvert(Object sourceData, Object... params) {
        if (sourceData == null) {
            return null;
        }
        if (targetType != null) {
            if (targetType.isInstance(sourceData)) {
                return sourceData;
            }
            try {
                if (sourceData instanceof String) {
                    return JSON.parseObject((String) sourceData, targetType);
                }
                return JSON.parseObject(JSON.toJSONString(sourceData), targetType);
            } catch (Exception e) {
                log.error("无法将数据 [" + sourceData + "] 转换成 [" + targetType.getName() + "] 对象类型", e);
            }
        }

        // targetType is null
        try {
            return convertByHandleName(sourceData, handleName.toUpperCase());
        } catch (Exception e) {
            log.error("执行 [" + handleName + "] 失败，将返回 null，源数据为 [" + sourceData + "]");
        }
        return null;
    }

    /**
     * 根据名称转换
     *
     * @param sourceData 源数据
     * @return handleName 对应的数据类型
     */
    private Object convertByHandleName(Object sourceData, String handleName) {
        // 字符串
        if (ParseType.TO_STRING.name().equals(handleName)) {
            return SoParseDataUtil.toStringAndTrimLeadWhitespace(sourceData);
        }
        // 向下取整
        else if (ParseType.TO_INTEGER.name().equals(handleName)
                || ParseType.TO_INTEGER_FLOOR.name().equals(handleName)) {
            return SoParseDataUtil.toInteger(sourceData, RoundingMode.FLOOR);
        }
        // 向上取整
        else if (ParseType.TO_INTEGER_CEIL.name().equals(handleName)) {
            return SoParseDataUtil.toInteger(sourceData, RoundingMode.CEILING);
        }
        // 四舍五入
        else if (ParseType.TO_INTEGER_ROUND.name().equals(handleName)) {
            return SoParseDataUtil.toInteger(sourceData, RoundingMode.HALF_UP);
        }
        // 转数字
        else if (ParseType.TO_NUMBER.name().equals(handleName)) {
            return new BigDecimal(SoParseDataUtil.toStringAndTrimLeadWhitespace(sourceData));
        }
        // 布尔类型
        else if (ParseType.TO_BOOLEAN.name().equals(handleName)) {
            return SoParseDataUtil.toBoolean(sourceData);
        }
        // 金额，两位小数，向下取整
        else if (ParseType.TO_MONEY.name().equals(handleName)
                || ParseType.TO_MONEY_FLOOR.name().equals(handleName)) {
            return SoParseDataUtil.toMoney(sourceData, RoundingMode.FLOOR);
        }
        // 金额，两位小数，向上取整
        else if (ParseType.TO_MONEY_CEIL.name().equals(handleName)) {
            return SoParseDataUtil.toMoney(sourceData, RoundingMode.CEILING);
        }
        // 金额，两位小数，四舍五入
        else if (ParseType.TO_MONEY_ROUND.name().equals(handleName)) {
            return SoParseDataUtil.toMoney(sourceData, RoundingMode.HALF_UP);
        }
        // 日期
        else if (ParseType.TO_DATE.name().equals(handleName)) {
            return SoParseDataUtil.toDate(sourceData);
        }
        // 日期时间
        else if (ParseType.TO_DATETIME.name().equals(handleName)) {
            return SoParseDataUtil.toDateTime(sourceData);
        }
        // 年月日格式，yyyy-MM-dd
        else if (ParseType.TO_YYYY_MM_DD.name().equals(handleName)) {
            return SoParseDataUtil.toDateStr(sourceData, SoParseDataUtil.YYYY_MM_DD);
        }
        // 年月日格式，yyyyMMdd
        else if (ParseType.TO_YYYYMMDD.name().equals(handleName)) {
            return SoParseDataUtil.toDateStr(sourceData, SoParseDataUtil.YYYYMMDD);
        }
        // 年月日 时分秒，yyyy-MM-dd HH:mm:ss
        else if (ParseType.TO_YYYY_MM_DD_HH_MM_SS.name().equals(handleName)) {
            return SoParseDataUtil.toDateStr(sourceData, SoParseDataUtil.YYYY_MM_DD_HH_MM_SS);
        }
        // 年月日 时分秒，yyyyMMdd HHmmss
        else if (ParseType.TO_YYYYMMDD_HHMMSS.name().equals(handleName)) {
            return SoParseDataUtil.toDateStr(sourceData, SoParseDataUtil.YYYYMMDD_HHMMSS);
        }
        // 年月日 时分秒，yyyyMMddHHmmss
        else if (ParseType.TO_YYYYMMDDHHMMSS.name().equals(handleName)) {
            return SoParseDataUtil.toDateStr(sourceData, SoParseDataUtil.YYYYMMDDHHMMSS);
        }
        // 时分秒，HHmmss
        else if (ParseType.TO_HHMMSS.name().equals(handleName)) {
            return SoParseDataUtil.toDateStr(sourceData, SoParseDataUtil.HHMMSS);
        }
        // 时分秒，HH:mm:ss
        else if (ParseType.TO_HH_MM_SS.name().equals(handleName)) {
            return SoParseDataUtil.toDateStr(sourceData, SoParseDataUtil.HH_MM_SS);
        }
        return null;
    }
}
