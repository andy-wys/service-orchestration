package org.andy.so.core.schema.enums;

import java.util.Arrays;

/**
 * <h2>数据来源类型定义</h2>
 *
 * @author: andy
 */
public enum SoSourceFromTypeEnum {
    /**
     * 数据来源于请求数据，网络请求或方法参数
     */
    REQ,
    /**
     * 数据来源于响应数据，网络响应或方法返回值
     */
    RESP,
    /**
     * 网络请求头
     */
    HEADER,
    /**
     * 网络请求 cookie
     */
    COOKIE;

    /**
     * 根据名称匹配枚举类型
     *
     * @param name 名称，忽略大小写
     * @return 未匹配到则返回 null
     */
    public static SoSourceFromTypeEnum of(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
