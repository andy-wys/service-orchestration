package org.andy.so.core;

import org.andy.so.core.util.SoStringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * <h2>环境变量包装类</h2>
 * <b>该类的方法仅适用于当前框架使用，请勿在框架外的代码中使用</b>
 *
 * @author: andy
 */
@Component
public class SoEnvironmentAware implements EnvironmentAware {
    private static final Log log = LogFactory.getLog(SoEnvironmentAware.class);
    /**
     * 占位符前缀
     */
    private static final String PLACEHOLDER_PREFIX = "${";
    /**
     * 占位符后缀
     */
    private static final char PLACEHOLDER_SUFFIX = '}';
    /**
     * 环境配置
     */
    private static Environment environment;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        SoEnvironmentAware.environment = environment;
    }


    /**
     * <h2>将占位符替换为配置的值</h2>
     *
     * @param value 属性值
     * @return 用配置文件替换后的值
     */
    public static String replacePlaceholderValue(String value) {
        if (environment == null) {
            log.error("请将 " + SoEnvironmentAware.class.getName() + " 正确注入到容器中");
            return value;
        }
        if (SoStringUtil.isBlank(value)) {
            return value;
        }


        int startIndex = value.indexOf(PLACEHOLDER_PREFIX);
        if (startIndex == -1) {
            return value;
        }
        StringBuilder result = new StringBuilder(value);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result, startIndex);
            if (endIndex == -1) {
                return result.toString();
            }
            String placeholder = result.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
            String propVal = environment.getProperty(placeholder);
            if (propVal == null) {
                propVal = "";
            }
            result.replace(startIndex, endIndex + 1, propVal);
            startIndex = result.indexOf(PLACEHOLDER_PREFIX, startIndex + propVal.length());
        }
        return result.toString();
    }

    /**
     * <h2>查找占位符结束下标</h2>
     *
     * @param buf        原字符串
     * @param startIndex 开始查找下标
     * @return 结束占位符下标，未找到则返回 -1
     */
    private static int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        if (startIndex + 1 > buf.length()) {
            return -1;
        }
        for (int i = startIndex; i < buf.length(); i++) {
            if (PLACEHOLDER_SUFFIX == buf.charAt(i)) {
                return i;
            }
        }
        return -1;
    }
}

