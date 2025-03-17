package org.andy.so.core.trace;

import org.andy.so.core.util.SoStringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * <h2>trace 配置</h2>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public class SoTraceHelper {
    private static final Log log = LogFactory.getLog(SoTraceHelper.class);
    /**
     * 用户自定义的 trace 实现
     */
    private static SoTraceAdapter traceAdapter = null;
    /**
     * 默认的 trace MDC 实现
     */
    private static SoTraceAdapter defaultTraceAdapter = null;

    static {
        try {
            defaultTraceAdapter = new SoTraceMdcAdapter();
        } catch (ClassNotFoundException e) {
            log.error("未能初始化默认的 trace 适配器，可通过 SoTrace#setTraceAdapter 设置。" + e.getMessage());
        }
    }

    /**
     * 设置 trace 实现类
     *
     * @param traceAdapter trace 实现
     */
    public static void setTraceAdapter(SoTraceAdapter traceAdapter) {
        SoTraceHelper.traceAdapter = traceAdapter;
    }

    /**
     * <h3>trace 设置</h3>
     *
     * @param key   trace id
     * @param value trace value
     */
    public static void put(String key, String value) {
        if (SoStringUtil.isAnyBlank(key, value)) {
            return;
        }
        if (traceAdapter != null) {
            traceAdapter.put(key, value);
        } else if (defaultTraceAdapter != null) {
            defaultTraceAdapter.put(key, value);
        }
    }

    /**
     * <h3>trace 值获取</h3>
     *
     * @param key trace id
     * @return trace value
     */
    public static String get(String key) {
        if (SoStringUtil.isAnyBlank(key)) {
            return null;
        }
        if (traceAdapter != null) {
            return traceAdapter.get(key);
        } else if (defaultTraceAdapter != null) {
            return defaultTraceAdapter.get(key);
        }
        return null;
    }

    /**
     * <h3>删除 trace</h3>
     *
     * @param key trace id
     */
    public static void remove(String key) {
        if (SoStringUtil.isAnyBlank(key)) {
            return;
        }
        if (traceAdapter != null) {
            traceAdapter.remove(key);
        } else if (defaultTraceAdapter != null) {
            defaultTraceAdapter.remove(key);
        }
    }

    /**
     * <h3>清除 trace</h3>
     */
    public static void clear() {
        if (traceAdapter != null) {
            traceAdapter.clear();
        } else if (defaultTraceAdapter != null) {
            defaultTraceAdapter.clear();
        }
    }

    /**
     * <h3>生成 trace id</h3>
     *
     * @return trace value
     */
    public static String generateTraceId() {
        if (traceAdapter != null) {
            return traceAdapter.generateTraceId();
        } else if (defaultTraceAdapter != null) {
            return defaultTraceAdapter.generateTraceId();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    static class SoTraceMdcAdapter implements SoTraceAdapter {
        Class<?> mdcAdapterClass;

        SoTraceMdcAdapter() throws ClassNotFoundException {
            final String mdcClassName = "org.slf4j.MDC";
            final String basicMdcClassName = "org.slf4j.helpers.BasicMDCAdapter";
            final String log4jMdcClassName = "org.slf4j.impl.Log4jMDCAdapter";
            final String logbackMdcClassName = "ch.qos.logback.classic.util.LogbackMDCAdapter";

            try {
                mdcAdapterClass = Class.forName(mdcClassName);
            } catch (ClassNotFoundException ignored) {
            }

            if (mdcAdapterClass == null) {
                try {
                    mdcAdapterClass = Class.forName(basicMdcClassName);
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (mdcAdapterClass == null) {
                try {
                    mdcAdapterClass = Class.forName(log4jMdcClassName);
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (mdcAdapterClass == null) {
                try {
                    mdcAdapterClass = Class.forName(logbackMdcClassName);
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (mdcAdapterClass == null) {
                throw new ClassNotFoundException(
                        "未能构建 SoTraceMdcAdapter，因为无法找到以下任意的类："
                                + Arrays.toString(
                                new String[]{
                                        mdcClassName,
                                        basicMdcClassName,
                                        log4jMdcClassName,
                                        logbackMdcClassName}));
            } else {
                log.info("默认的 SoTraceAdapter 初始化成功，默认实现为：" + mdcAdapterClass.getName());
            }
        }

        @Override
        public void put(String key, String value) {
            try {
                Method method = mdcAdapterClass.getMethod("put", String.class, String.class);
                method.invoke(mdcAdapterClass, key, value);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                log.error("执行 SoTraceMdcAdapter#put 异常，" + e.getMessage());
            }
        }

        @Override
        public String get(String key) {
            try {
                Method method = mdcAdapterClass.getMethod("get", String.class);
                return (String) method.invoke(mdcAdapterClass, key);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                log.error("执行 SoTraceMdcAdapter#get 异常，" + e.getMessage());
            }
            return null;
        }

        @Override
        public void remove(String key) {
            try {
                Method method = mdcAdapterClass.getMethod("remove", String.class);
                method.invoke(mdcAdapterClass, key);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                log.error("执行 SoTraceMdcAdapter#remove 异常，" + e.getMessage());
            }
        }

        @Override
        public void clear() {
            try {
                Method method = mdcAdapterClass.getMethod("clear");
                method.invoke(mdcAdapterClass);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                log.error("执行 SoTraceMdcAdapter#clear 异常，" + e.getMessage());
            }
        }
    }
}
