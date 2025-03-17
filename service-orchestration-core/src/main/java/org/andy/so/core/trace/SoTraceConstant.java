package org.andy.so.core.trace;

/**
 * trace 常量定义
 *
 * @author: andy
 */
public class SoTraceConstant {
    /**
     * 调用链路追踪 ID
     */
    public static String KEY_TRACE_ID = "traceId";
    /**
     * 标识 trace 是否为框架注入
     */
    public static String KEY_TRACE_OWNER = "SaTraceOwner";
    /**
     * 框架注入的 trace
     */
    public static String TRACE_OWNER_SA = "SA";
}
