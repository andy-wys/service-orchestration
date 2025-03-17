package org.andy.so.core.schema.enums;

import java.util.Arrays;

/**
 * <h2>HTTP/HTTPS 接口请求方法</h2>
 * 暂时只支持 http get 和 post，后续有需求再添加
 *
 * @author: andy
 */
public enum SoApiMethodEnum {
    /**
     * http get 请求
     */
    GET("GET"),
    /**
     * http post 请求
     */
    POST("POST"),
    ;
    /**
     * 请求方法
     */
    private final String method;

    /**
     * 构造方法
     *
     * @param method 请求方法
     */
    SoApiMethodEnum(String method) {
        this.method = method;
    }

    /**
     * 根据方法名称匹配枚举类型，大小写敏感
     *
     * @param method 方法名称
     * @return 未匹配到则返回 {@code null}
     */
    public static SoApiMethodEnum of(String method) {
        return Arrays.stream(values())
                .filter(e -> e.getMethod().equals(method))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取方法名称
     *
     * @return 方法名
     */
    public String getMethod() {
        return method;
    }
}
