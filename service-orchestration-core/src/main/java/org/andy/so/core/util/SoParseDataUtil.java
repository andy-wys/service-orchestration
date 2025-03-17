package org.andy.so.core.util;

import com.alibaba.fastjson2.JSON;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <h2>数据转换</h2>
 *
 * @author: andy
 */
public final class SoParseDataUtil {
    public final static String YYYY_MM_DD = "yyyy-MM-dd";
    public final static String YYYYMMDD = "yyyyMMdd";

    public final static String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss:SSS";
    public final static String YYYYMMDD_HHMMSSSSS = "yyyyMMdd HHmmssSSS";
    public final static String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
    public final static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public final static String YYYYMMDD_HHMMSS = "yyyyMMdd HHmmss";
    public final static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public final static String HHMMSS = "HHmmss";
    public final static String HH_MM_SS = "HH:mm:ss";

    /**
     * <h3>obj 转 String，并去掉前面的空格</h3>
     *
     * @param sourceData 源数据
     * @return json String
     */
    public static String toStringAndTrimLeadWhitespace(Object sourceData) {
        String sourceDataStr;
        if (null == sourceData) {
            return null;
        } else if (sourceData instanceof CharSequence) {
            sourceDataStr = ((CharSequence) sourceData).toString();
        } else {
            sourceDataStr = JSON.toJSONString(sourceData);
        }
        return SoStringUtil.trimLeadingWhitespace(sourceDataStr);
    }

    /**
     * <h3>obj 转 int</h3>
     *
     * @param sourceData   源数据
     * @param roundingMode 进位模式
     * @return int
     */
    public static Integer toInteger(Object sourceData, RoundingMode roundingMode) {
        if (null == sourceData) {
            return null;
        }
        String sourceDataStr = toStringAndTrimLeadWhitespace(sourceData);
        if (SoStringUtil.isBlank(sourceDataStr)) {
            return null;
        }
        return new BigDecimal(sourceDataStr).setScale(0, roundingMode).intValue();
    }

    /**
     * <h3> obj 转 boolean</h3>
     *
     * @param sourceData 源数据
     * @return int
     */
    public static Boolean toBoolean(Object sourceData) {
        String sourceDataStr = toStringAndTrimLeadWhitespace(sourceData);
        return "true".equalsIgnoreCase(sourceDataStr) || "1".equals(sourceDataStr);
    }

    /**
     * <h3>obj 转 金额</h3>
     *
     * @param sourceData   源数据
     * @param roundingMode 进位模式
     * @return BigDecimal
     */
    public static BigDecimal toMoney(Object sourceData, RoundingMode roundingMode) {
        String sourceDataStr = toStringAndTrimLeadWhitespace(sourceData);
        if (SoStringUtil.isBlank(sourceDataStr)) {
            return null;
        }
        return new BigDecimal(sourceDataStr).setScale(2, roundingMode);
    }

    /**
     * <h3>obj 转 日期</h3>
     *
     * @param sourceData 源数据
     * @return BigDecimal
     */
    public static Date toDate(Object sourceData) {
        if (sourceData == null) {
            return null;
        }
        if (sourceData instanceof Date) {
            return reservedYmd((Date) sourceData);
        }
        if (sourceData instanceof Calendar) {
            return reservedYmd((Calendar) sourceData);
        }

        String sourceDataStr = toStringAndTrimLeadWhitespace(sourceData);
        if (SoStringUtil.isBlank(sourceDataStr)) {
            return null;
        }

        try {
            return new SimpleDateFormat(YYYY_MM_DD).parse(sourceDataStr);
        } catch (ParseException ignored) {
        }
        try {
            return new SimpleDateFormat(YYYYMMDD).parse(sourceDataStr);
        } catch (ParseException ignored) {
        }
        return null;
    }

    /**
     * <h3>转换成日期时间格式</h3>
     *
     * @param sourceData 源数据
     * @return 保留日期时间
     */
    public static Date toDateTime(Object sourceData) {
        if (sourceData == null) {
            return null;
        }
        if (sourceData instanceof Date) {
            return (Date) sourceData;
        }
        if (sourceData instanceof Calendar) {
            return ((Calendar) sourceData).getTime();
        }

        String sourceDataStr = toStringAndTrimLeadWhitespace(sourceData);
        if (SoStringUtil.isBlank(sourceDataStr)) {
            return null;
        }

        try {
            return new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_SSS).parse(sourceDataStr);
        } catch (ParseException ignored) {
        }
        try {
            return new SimpleDateFormat(YYYYMMDD_HHMMSSSSS).parse(sourceDataStr);
        } catch (ParseException ignored) {
        }
        try {
            return new SimpleDateFormat(YYYYMMDDHHMMSSSSS).parse(sourceDataStr);
        } catch (ParseException ignored) {
        }
        try {
            return new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).parse(sourceDataStr);
        } catch (ParseException ignored) {
        }
        try {
            return new SimpleDateFormat(YYYYMMDD_HHMMSS).parse(sourceDataStr);
        } catch (ParseException ignored) {
        }
        try {
            return new SimpleDateFormat(YYYYMMDDHHMMSS).parse(sourceDataStr);
        } catch (ParseException ignored) {
        }
        return toDate(sourceDataStr);
    }

    /**
     * <h3>格式化日期</h3>
     *
     * @param date   日期
     * @param format 格式
     * @return String
     */
    public static String toDateStr(Object date, String format) {
        if (SoObjectUtil.anyNull(date, format)) {
            return null;
        }
        Date sourceDate;
        if (date instanceof Date) {
            sourceDate = (Date) date;
        } else if (date instanceof Calendar) {
            sourceDate = ((Calendar) date).getTime();
        } else {
            sourceDate = toDateTime(date);
        }
        if (sourceDate == null) {
            return null;
        }

        return new SimpleDateFormat(format).format(sourceDate);
    }

    /**
     * <h3>只保留年月日</h3>
     *
     * @param date 日期
     * @return 年月日
     */
    public static Date reservedYmd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return reservedYmd(calendar);
    }

    /**
     * 只保留年月日
     *
     * @param calendar 日期
     * @return 年月日
     */
    public static Date reservedYmd(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
