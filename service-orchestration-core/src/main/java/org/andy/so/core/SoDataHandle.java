package org.andy.so.core;

/**
 * <h2>数据处理器定义</h2>
 * <b>xml 配置中使用的 dataHandle 实现类都必须实现该接口，并将其注入到 spring 容器中</b>
 *
 * @author: andy
 */
public interface SoDataHandle<T, R> {
    /**
     * <h2>指定转换器名称</h2>
     *
     * @return handle name
     */
    default String getHandleName() {
        return this.getClass().getSimpleName();
    }

    /**
     * <h2>转换处理方法</h2>
     *
     * @param sourceData 原数据
     * @param params     参数
     * @return 转换后的数据
     */
    R doConvert(T sourceData, Object... params);
}
