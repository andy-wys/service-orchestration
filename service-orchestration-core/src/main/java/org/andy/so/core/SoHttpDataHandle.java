package org.andy.so.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h2>带有 HttpServletRequest 的数据处理器接口</h2>
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public interface SoHttpDataHandle<T, R> extends SoDataHandle<T, R> {

    /**
     * <h2>处理入口方法</h2>
     *
     * @param sourceData 原数据
     * @param params     参数
     * @return 处理结果
     */
    @Override
    default R doConvert(T sourceData, Object... params) {
        if (params == null) {
            return handle(sourceData, null, null);
        }
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        for (Object param : params) {
            if (param instanceof HttpServletRequest && request == null) {
                request = (HttpServletRequest) param;
                continue;
            }
            if (param instanceof HttpServletResponse && response == null) {
                response = (HttpServletResponse) param;
            }
        }
        return handle(sourceData, request, response);
    }

    /**
     * <h2>处理器实现方法</h2>
     *
     * @param sourceData 源数据
     * @param request    标准请求的 http request
     * @param response   标准请求的 http response
     * @return 转换都的数据
     */
    R handle(T sourceData, HttpServletRequest request, HttpServletResponse response);
}
