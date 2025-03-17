package org.andy.so.core.trace;

import java.util.UUID;

/**
 * <h2>链路跟踪适配器</h2>
 * 用户可根据自己的 trace 实现来定义该接口实现
 *
 * @author: andy
 */
public interface SoTraceAdapter {
    /**
     * <h3>trace 设置</h3>
     *
     * @param key   trace id
     * @param value trace value
     */
    void put(String key, String value);

    /**
     * <h3>trace 值获取</h3>
     *
     * @param key trace id
     * @return trace value
     */
    String get(String key);

    /**
     * <h3>删除 trace</h3>
     *
     * @param key trace id
     */
    void remove(String key);

    /**
     * <h3>清除 trace</h3>
     */
    void clear();

    /**
     * <h3>生成 trace id</h3>
     *
     * @return trace value
     */
    default String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
