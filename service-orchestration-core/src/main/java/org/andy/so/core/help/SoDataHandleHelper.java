package org.andy.so.core.help;

import org.andy.so.core.SoDataHandle;

/**
 * <h2>数据处理器工厂类，用于管理和执行处理器</h2>
 *
 * @author: andy
 */
public interface SoDataHandleHelper {

    /**
     * <h3>将数据处理器添加到 {@code handleMap} 中</h3>
     * map: type -> instance
     *
     * @param type     类型名称，不能为空，否则不添加
     * @param instance 处理器，不能为空，否则不添加
     */
    void addDataConvertHandle(String type, SoDataHandle<?, ?> instance);

    /**
     * <h3>将数据处理器添加到 {@code handleMap} 中，key 为 {@code instance#getHandleName()}</h3>
     *
     * @param instance 处理器实现
     */
    void addDataConvertHandle(SoDataHandle<?, ?> instance);

    /**
     * <h3>找到 type 对应的处理类，然后执行 doConvert 方法进行数据转换</h3>
     *
     * @param type       转换类型
     * @param sourceData 源数据
     * @param params     参数
     * @return 转换后的数据
     */
    Object doConvert(String type, Object sourceData, Object... params);
}
